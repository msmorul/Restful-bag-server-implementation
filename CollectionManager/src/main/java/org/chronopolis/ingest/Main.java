/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewListener;
import org.apache.pivot.wtk.Window;
import org.chronopolis.ingest.messages.SaveBagMessage;
import org.chronopolis.ingest.messages.TransferBagMessage;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackageListener;
import org.chronopolis.ingest.pkg.PackageManager;
import org.apache.log4j.Logger;
import org.apache.pivot.collections.adapter.ListAdapter;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.MessageType;
import org.chronopolis.bag.client.BagBean;
import org.chronopolis.bag.client.BagList;
import org.chronopolis.bag.client.JsonGateway;

/**
 * Package storage: bdb digest & path
 *  - default sha-256 digests
 * Transmittion, store state in bdb, ack each file, 
 * Manifest export - allow generation of bagit-mfst
 *
 * ingest steps:
 *  1. create bagit/checkm manifest,
 *  2. transmit manifest
 *   - mark package transmt-only
 *  3. send data, mark r/o finish.
 *  4. ask chron for final mfst of received items and compare (TODO)
 *
 * Execute-time parameters:
 *  jnlp.provider - short name of data provider to use
 *  jnlp.ingest.url - target url for bag catcher webapp
 *  jnlp.urlpattern - default url pattern to use
 *
 * @author toaster
 */
public class Main implements Application {

    private static final Logger LOG = Logger.getLogger(Main.class);
    @BXML
    private ListView ingestedListView;
    @BXML
    private ListView pendingListView;
    private Window mainW;
    private static JsonGateway gateway;
    private static PackageManager mgr;
    // Default settings
    private static File defaultDir;
    private static String defaultURLPattern;
    private static final String PARAM_PROVIDER = "jnlp.provider";
    private static final String PARAM_INGEST_URL = "jnlp.ingest.url";
    private static final String PARAM_URL_PATTERN = "jnlp.urlpattern";
    private static final String PARAM_DEFAULT_DIR = "jnlp.defaultdir";
    private static final String DEFAULT_INGEST = "http://localhost:7878/bags";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DesktopApplicationContext.main(Main.class, args);
    }

    public static JsonGateway getGateway() {
        return gateway;
    }

    public static PackageManager getPackageManager() {
        return mgr;
    }

    public static String getDefaultURLPattern() {
        return defaultURLPattern;
    }

    public static File getDefaultDirectory() {
        return defaultDir;
    }

    public void startup(Display dspl, Map<String, String> map) throws Exception {

        // set ingestion url
        String url = System.getProperty(PARAM_INGEST_URL);
        URL endpointURL;
        if (url != null && !url.isEmpty()) {
            endpointURL = new URL(url);
        } else {
            endpointURL = new URL(DEFAULT_INGEST);
        }
        LOG.info("Ingest URL: " + endpointURL);
        gateway = new JsonGateway(endpointURL);

        // set starting directory for browse windows
        String defaultDirectory = System.getProperty(PARAM_DEFAULT_DIR);
        if (defaultDirectory != null && new File(defaultDirectory).isDirectory()) {
            defaultDir = new File(defaultDirectory);
        } else {
            defaultDir = new File(System.getProperty("user.home"));
        }
        LOG.info("Working directory: " + defaultDir);

        // Set default url pattern
        defaultURLPattern = System.getProperty(PARAM_URL_PATTERN);
        if (defaultURLPattern == null || defaultURLPattern.isEmpty()) {
            defaultURLPattern = "http://your_webserver_here.com/bags/{b}/{d}";
        }
        LOG.info("Default URL Pattern: " + defaultURLPattern);

        mgr = new PackageManager();

        // configure cron connection
//        PeerAuthenticator pa = new PeerAuthenticator();
//        Authenticator.setDefault(pa);

//        aceSite = new PartnerSite();
//        aceSite.setRemoteURL("http://chron-monitor.umiacs.umd.edu:8080/ace-am");
//        aceSite.setUser("browse");
//        aceSite.setPass("browse");
//        pa.addSite(aceSite);

        // build app
        BXMLSerializer serializer = new BXMLSerializer();
        mainW = (Window) serializer.readObject(Main.class, "applicationWindow.bxml");
        serializer.bind(this);

        //TODO: Clean this shithole up
        pendingListView.getListViewListeners().add(new ListViewListener.Adapter() {

            private BagWatcher listener = new BagWatcher();
            private ChronWatcher watcher = new ChronWatcher();

            class ChronWatcher extends ChronPackageListener.Adapter {

                @Override
                public void nameChanged(ChronPackage pkg, String oldname) {
                    pendingListView.repaint();
                }

                @Override
                public void readOnlyChanged(ChronPackage pkg, boolean old) {
                    pendingListView.repaint();
                }
            }

            class BagWatcher extends ListListener.Adapter {

                @Override
                public void itemInserted(List list, int i) {
                    if (list.get(i) instanceof ChronPackage) {
                        ((ChronPackage) list.get(i)).getChronPackageListeners().add(watcher);
                    }
                }

                @Override
                public void itemsRemoved(List list, int i, Sequence sqnc) {
                    for (int j = 0; j < sqnc.getLength(); j++) {
                        if (sqnc.get(j) instanceof ChronPackage) {
                            ((ChronPackage) sqnc.get(j)).getChronPackageListeners().remove(watcher);
                        }
                    }
                }

                @Override
                public void itemUpdated(List nl, int i, Object prev) {
                    if (prev instanceof ChronPackage) {
                        ChronPackage t = (ChronPackage) prev;
                        t.getChronPackageListeners().remove(watcher);
                        ((ChronPackage) nl.get(i)).getChronPackageListeners().add(watcher);
                    }
                }
            }

            @Override
            public void listDataChanged(ListView listView, List<?> previousListData) {
                previousListData.getListListeners().remove(listener);
                listView.getListData().getListListeners().add(listener);
            }
        });

        pendingListView.setListData(mgr.getPackageList());

        MessageBus.subscribe(SaveBagMessage.class, new ManifestSaveAction());
        MessageBus.subscribe(TransferBagMessage.class, new TransferBagAction());
        MessageBus.subscribe(TransferBagMessage.class, new BagServerTransfer());


        ApplicationContext.queueCallback(new Runnable() {

            public void run() {
                try
                {
                BagList bl = getGateway().listBags();
                ingestedListView.setListData(new ListAdapter<BagBean>(bl.getObjects()));
                }
                catch (IOException e)
                {
                    LOG.error("Error reading server bags ",e);
                    Alert.alert(MessageType.ERROR, "Error loading ingested list", mainW);
                }
            }
        });

        mainW.open(dspl);

    }

    public boolean shutdown(boolean bln) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
