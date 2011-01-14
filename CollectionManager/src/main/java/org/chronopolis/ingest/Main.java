/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import edu.umiacs.ace.json.JsonGateway;
import edu.umiacs.ace.json.PartnerSite;
import edu.umiacs.ace.json.PeerAuthenticator;
import edu.umiacs.ace.json.StatusBean;
import edu.umiacs.ace.json.StatusBean.CollectionBean;
import edu.umiacs.ace.json.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewListener;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.chronopolis.ingest.pkg.BagBuildListener;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackageListener;
import org.chronopolis.ingest.pkg.DelayedTransferStream;
import org.chronopolis.ingest.pkg.ManifestBuilder;
import org.chronopolis.ingest.pkg.ManifestFileWriter;
import org.chronopolis.ingest.pkg.PackageManager;

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
 *  3. send data, mark in bdb after finish.
 *  4. ask chron for final mfst of received items and compare
 *  5. remove local package or just update state?
 *
 * @author toaster
 */
public class Main implements Application {

//    private static final String PROVIDER = "cdl";
    @BXML
    private ListView ingestedListView;
    @BXML
    private ListView pendingListView;
    @BXML
    private Border detailBorder;
    @BXML
    private BagAssemblyPanel assemblyPanel;
    @BXML
    private ArchivedCollectionPanel archivePanel;
    private Window mainW;
    @BXML
    private Menu.Item saveBagBtn;
    @BXML
    private Menu.Item createBagBtn;
    @BXML
    private FileBrowserSheet digestBrowser;
    @BXML
    private CreateHoleyBagDialog createBagDialog;
    @BXML(id = "aboutDialog.buildLbl")
    private Label buildLbl;
    @BXML(id = "aboutDialog.dateLbl")
    private Label dateLbl;
    @BXML(id = "aboutDialog.img")
    private ImageView aboutImage;
    private static PartnerSite aceSite;
    private static PackageManager mgr;
    private static URL chronURL;
    // NCAR hack
    private static File defaultDir;
    private static String defaultURLPattern;
    private static String provider;
    private MenuHandler menuHandler = new MenuHandler.Adapter() {

        @Override
        public boolean configureContextMenu(Component cmpnt, Menu menu, int i, int y) {

            Menu.Section section = new Menu.Section();
            menu.getSections().add(section);

            Menu.Item addItem = new Menu.Item("New Bag");
            section.add(addItem);
            addItem.getButtonPressListeners().add(new ButtonPressListener() {

                public void buttonPressed(Button button) {
                    ChronPackage newPkg = new ChronPackage();
                    newPkg.setDigest("SHA-256");
                    mgr.getPackageList().add(newPkg);
                    pendingListView.setSelectedIndex(pendingListView.getListData().getLength() - 1);
                }
            });

            if (pendingListView.getItemAt(y) > -1) {
                final ChronPackage remPkg = mgr.getPackageList().get(pendingListView.getItemAt(y));
                Menu.Item removeItem = new Menu.Item("Remove");
                section.add(removeItem);
                removeItem.getButtonPressListeners().add(new ButtonPressListener() {

                    public void buttonPressed(Button button) {
                        Prompt.prompt(MessageType.QUESTION, "Remove bag " + remPkg.getName(), mainW, new SheetCloseListener() {

                            public void sheetClosed(Sheet sheet) {
                                if (sheet.getResult()) {
                                    Main.getPackageManager().getPackageList().remove(remPkg);
                                }

                            }
                        });
                    }
                });

                Menu.Item duplicateItem = new Menu.Item("Create Duplicate");
                duplicateItem.setTooltipText("Create a writable duplicate of this package");
                duplicateItem.getButtonPressListeners().add(new ButtonPressListener() {

                    public void buttonPressed(Button button) {
                        mgr.getPackageList().add(remPkg.clone());
                        pendingListView.setSelectedIndex(pendingListView.getListData().getLength() - 1);
                    }
                });
                section.add(duplicateItem);
            }

            return false;
        }
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DesktopApplicationContext.main(Main.class, args);
    }

    public static PartnerSite getAceSite() {
        return aceSite;
    }

    public static PackageManager getPackageManager() {
        return mgr;
    }

    public static URL getURL() {
        return chronURL;
    }

    public static String getDefaultURLPattern() {
        return defaultURLPattern;
    }

    public static String getProvider() {
        return provider;
    }

    public static File getDefaultDirectory() {
        if (defaultDir == null) {
            return new File(System.getProperty("user.home"));
        } else {
            return defaultDir;
        }
    }

    public void startup(Display dspl, Map<String, String> map) throws Exception {
        provider = System.getProperty("jnlp.provider");
        String url = System.getProperty("jnlp.ingest.url");
        defaultURLPattern = System.getProperty("jnlp.urlpattern");
        if (System.getProperty("jnlp.defaultdir") != null && new File(System.getProperty("jnlp.defaultdir")).isDirectory()) {
            defaultDir = new File(System.getProperty("jnlp.defaultdir"));
        }
        System.out.println("system url: " + url);
        if (!Strings.isEmpty(url)) {
            chronURL = new URL(url);
        }
        if (Strings.isEmpty(defaultURLPattern))
        {
            defaultURLPattern = "http://your_webserver_here.com/bags/{b}/{d}";
        }

        //TODO: load packages
        mgr = new PackageManager();

        // configure cron connection
        PeerAuthenticator pa = new PeerAuthenticator();
        Authenticator.setDefault(pa);

        final PartnerSite umiacs = new PartnerSite();
//        umiacs.setRemoteURL("http://chron-monitor.umiacs.umd.edu:8080/ace-am");
        umiacs.setRemoteURL("http://chronopolis.sdsc.edu:8080/Ace-am");
        umiacs.setUser("browse");
        umiacs.setPass("browse");
        pa.addSite(umiacs);
        aceSite = umiacs;

        // build app
        BXMLSerializer serializer = new BXMLSerializer();
        mainW = (Window) serializer.readObject(Main.class, "applicationWindow.bxml");
        serializer.bind(this);

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
        pendingListView.getParent().setMenuHandler(menuHandler);
        pendingListView.setListData(mgr.getPackageList());
        pendingListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {

            @Override
            public void selectedRangesChanged(ListView lv, Sequence<Span> sqnc) {
                if (pendingListView.getSelectedItem() != null) {
                    MessageBus.sendMessage(pendingListView.getSelectedItem());
//                    createbagBtn.setEnabled(!((ChronPackage) pendingListView.getSelectedItem()).isReadOnly());
                    saveBagBtn.setEnabled(true);
                    createBagBtn.setEnabled(true);
                } else {
//                    transferBtn.setEnabled(false);
                    saveBagBtn.setEnabled(false);
                    createBagBtn.setEnabled(false);
                }
                detailBorder.setContent(assemblyPanel);
                ingestedListView.clearSelection();
            }
        });

        pendingListView.setItemRenderer(new ListViewItemRenderer() {

            @Override
            public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
                if (item instanceof ChronPackage) {
                    ChronPackage cp = (ChronPackage) item;
                    if (Strings.isEmpty(cp.getName())) {
                        super.render("<Unnamed>", index, listView, selected, checked, highlighted, disabled);

                    } else {
                        super.render(cp.getName(), index, listView, selected, checked, highlighted, disabled);
                    }

                } else {
                    super.render(item, index, listView, selected, checked, highlighted, disabled);
                }
            }
        });

        digestBrowser.setRootDirectory(getDefaultDirectory());
        digestBrowser.getSheetStateListeners().add(new SaveBagListener());

        ingestedListView.setItemRenderer(new CollectionBeanRenderer());
        ingestedListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {

            @Override
            public void selectedRangesChanged(ListView lv, Sequence<Span> sqnc) {

                if (ingestedListView.getSelectedItem() != null) {
                    MessageBus.sendMessage(ingestedListView.getSelectedItem());
                }
                detailBorder.setContent(archivePanel);
                pendingListView.clearSelection();
                saveBagBtn.setEnabled(false);
                createBagBtn.setEnabled(true);
            }
        });

        ApplicationContext.queueCallback(new Runnable() {

            public void run() {
                List<CollectionBean> list = updateCollectionList(umiacs);
                ingestedListView.setListData(list);
                createBagDialog.setCollectionListData(list);
            }
        });

        createBagDialog.getAcceptButtonPressListeners().add(new TansferBagListener());
        mainW.open(dspl);

        ResourceBundle bundle = java.util.ResourceBundle.getBundle("version");
        buildLbl.setText(bundle.getString("build.num"));
        dateLbl.setText(bundle.getString("build.date"));
        // Pretty picture in the about box ;) Better than pemulis' twitter feed
        if (System.currentTimeMillis() % 2 == 1) {
            aboutImage.setImage(Main.class.getResource("trash.jpg"));
        } else {
            aboutImage.setImage(Main.class.getResource("barge.jpg"));
        }
    }

    private List<CollectionBean> updateCollectionList(PartnerSite site) {
        List<CollectionBean> cbList = new ArrayList<CollectionBean>();
        try {
            JsonGateway gateway = JsonGateway.getGateway();
            StatusBean sb = gateway.getStatusBean(site);

            if (sb == null) {
                Alert.alert("Could not contact Chronopolis", mainW);
            } else {

                for (CollectionBean cb : sb.getCollections()) {
                    if (provider.equals(cb.getGroup())) {
                        cbList.add(cb);
                    }
                }
            }
            return cbList;
        } catch (Exception e) {

            Alert.alert("Error reading collections " + e.getMessage(), mainW);
            return null;
        }
    }

    public boolean shutdown(boolean bln) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    private class SaveBagListener extends SheetStateListener.Adapter {

        private ChronPackage workingPackage;

        public SaveBagListener() {
            MessageBus.subscribe(ChronPackage.class, new MessageBusListener<ChronPackage>() {

                public void messageSent(ChronPackage t) {
                    workingPackage = t;
                }
            });
        }

        @Override
        public void sheetClosed(Sheet sheet) {
            if (!sheet.getResult()) {
                return;
            }
            final ManifestBuilder builder = new ManifestBuilder(workingPackage,0);
            BuildProgressDialog dialog = new BuildProgressDialog();

            File f = digestBrowser.getSelectedFile();
            final OutputStream os;
            try {
                os = new FileOutputStream(f);
                ManifestFileWriter writer = new ManifestFileWriter(os);
                builder.getBuildListeners().add(writer);
            } catch (IOException ioe) {
                Alert.alert(MessageType.ERROR, "Error opening manifest file", mainW);
                return;
            }

            dialog.setBuilder(builder);
            dialog.open(mainW.getWindow(), new SheetCloseListener() {

                public void sheetClosed(Sheet sheet) {
                    builder.cancel();
                    try {
                        os.close();
                    } catch (IOException e) {
                    }
                    if (sheet.getResult()) {
                        Alert.alert(MessageType.INFO, "Transfer Successful", mainW);
                    } else {
                        Alert.alert(MessageType.ERROR, "Transfer Aborted", mainW);
                    }

                }
            });

            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        builder.scanPackage();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    //TODO: yuck, clean this up, move xfer parts to new class
    class TansferBagListener implements ButtonPressListener {

        private ChronPackage workingPackage;

        public TansferBagListener() {
            MessageBus.subscribe(ChronPackage.class, new MessageBusListener<ChronPackage>() {

                public void messageSent(ChronPackage t) {
                    workingPackage = t;
                }
            });
        }

        public void buttonPressed(final Button button) {
            final ManifestBuilder builder = new ManifestBuilder(workingPackage, createBagDialog.getTotalSize());
            final BuildProgressDialog dialog = new BuildProgressDialog();

            final OutputStream os;
            final HttpURLConnection connection;

            if (createBagDialog.isLocal()) {
                File f = createBagDialog.getBagFile();

                try {
                    os = new GZIPOutputStream(new FileOutputStream(f));

                    connection = null;
                } catch (IOException ioe) {
                    Alert.alert(MessageType.ERROR, "Error opening bag file", mainW);
                    return;
                }
            } else {
                //Open Chron input stream
                try {
                    URL newURL = new URL(chronURL + "/" + createBagDialog.getBagName());
                    if (createBagDialog.isHoley()) {
                        connection = null;
                        os = new DelayedTransferStream(newURL);
                    } else {
                        connection = (HttpURLConnection) newURL.openConnection();
                        connection.setChunkedStreamingMode(32768);
                        connection.setDoOutput(true);
                        connection.setRequestMethod("PUT");
//                    System.out.println("opening " + newURL);
                        os = new GZIPOutputStream(connection.getOutputStream());
                    }

                } catch (IOException ioe) {
                    Alert.alert(MessageType.ERROR, "Error opening chronopolis connection: ("
                            + ioe.getClass().getName() + ") " + ioe.getMessage(), mainW);
                    return;
                }
            }

            BagBuildListener writer = new BagBuildListener(workingPackage, os, createBagDialog.isHoley());
            writer.setCloseOutput(true);
            if (createBagDialog.isHoley()) {
                writer.setUrlPattern(createBagDialog.getUrlPattern());
            }

            builder.getBuildListeners().add(writer);

            dialog.setBuilder(builder);
            dialog.open(mainW, new SheetCloseListener() {

                public void sheetClosed(Sheet sheet) {
                    builder.cancel();
                    if (os instanceof DelayedTransferStream) {
                        DelayedTransferStream dts = (DelayedTransferStream) os;
                        if (dts.getResponseCode() < 200 || dts.getResponseCode() > 299) {
                            Alert.alert(MessageType.ERROR, "Transfer error HTTP/" + dts.getResponseCode() + " " + dts.getResponseMessage(), mainW);
                            return;
                        }
                    } else if (connection != null) {
                        try {
                            if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
                                Alert.alert(MessageType.ERROR, "Transfer error HTTP/" + connection.getResponseCode() + " " + connection.getResponseMessage(), mainW);
                                return;
                            }
                        } catch (IOException ioe) {
                        }
                        connection.disconnect();

                    }

                    if (sheet.getResult()) {
                        if (createBagDialog.isLocal()) {
                            Alert.alert(MessageType.INFO, "New Bag Created: " + createBagDialog.getBagFile().getPath(), mainW);
                        } else {
                            Alert.alert(MessageType.INFO, "Chronopolis Transfer Successful", mainW);
                        }
                        if (!createBagDialog.isLocal()) {
                            workingPackage.setReadOnly(true);
                        }
                    } else {
                        Alert.alert(MessageType.ERROR, "Aborted!", mainW);

                    }
                }
            });
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        builder.scanPackage();
                    } catch (Exception ioe) {
                        dialog.close();
                        ioe.printStackTrace();
                    }
                }
            });
            thread.start();

        }
    }
}
