/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Logger;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.chronopolis.bagserver.BagEntry;
import org.chronopolis.bagserver.BagVault;
import org.chronopolis.bagserver.disk.SimpleDiskVault;
import org.chronopolis.restserver.BagServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * @author toaster
 */
public class MainWindow extends Frame implements Bindable {

    private static final Logger LOG = Logger.getLogger(MainWindow.class);
    private Server server;
    @BXML
    private Menu.Item startServerBtn;
    @BXML
    private Menu.Item stopServerBtn;
    @BXML
    private TextInput port;
    @BXML
    private Menu.Item directoryBtn;
    @BXML
    private FileBrowserSheet fileBrowserSheet;
    @BXML
    private Label fileLabel;
    @BXML
    private ListView bagList;
    @BXML
    private Menu.Item refreshBtn;
    private File baseDir;
    private BagVault vault;

    private void refreshBags() {
        if (vault == null)
            return;
        List l = new ArrayList<BagEntry>();
        for (BagEntry be : vault.getBags()) {
            l.add(be);
        }
        bagList.setListData(l);
    }

    public void initialize(Map<String, Object> map, URL url, final Resources rsrcs) {
        bagList.setItemRenderer(new BagListRenderer());

        refreshBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                refreshBags();
            }
        });

        directoryBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                fileBrowserSheet.setMode(FileBrowserSheet.Mode.SAVE_TO);
                fileBrowserSheet.open(MainWindow.this, new SheetCloseListener() {

                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            baseDir = fileBrowserSheet.getSelectedFile();
                            fileLabel.setText(baseDir.getPath());
                            vault = new SimpleDiskVault(baseDir);
                            refreshBags();

                        }
                    }
                });
            }
        });

        stopServerBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                try {
                    if (server != null) {
                        server.stop();
                        startServerBtn.setEnabled(true);
                        stopServerBtn.setEnabled(false);
                        port.setEnabled(true);
                        LOG.debug("Jetty server stopped");
                        return;
                    }
                } catch (Exception e) {
                    startServerBtn.setEnabled(false);
                    stopServerBtn.setEnabled(true);
                    port.setEnabled(false);

                    LOG.error("Error shutting down", e);
                    return;
                }
            }
        });

        startServerBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                if (baseDir == null) {
                    Alert.alert("Choose bag directory before starting server", MainWindow.this);
                }
                startServerBtn.setEnabled(false);
                stopServerBtn.setEnabled(true);
                port.setEnabled(false);

                int p = Integer.parseInt(port.getText());
                ServletHolder sh = new ServletHolder(ServletContainer.class);

                sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
                sh.setInitParameter("com.sun.jersey.config.property.packages", "org.chronopolis.restserver");

                LOG.trace("Starting jetty server");
                server = new Server(p);
                Context context = new Context(server, "/", Context.SESSIONS);
                context.addServlet(sh, "/*");
                context.getServletContext().setAttribute(BagServer.VAULT, vault);

                try {
                    server.start();
                } catch (Exception e) {
                    startServerBtn.setEnabled(true);
                    stopServerBtn.setEnabled(false);
                    port.setEnabled(true);

                    LOG.error("Error starting up", e);
                }

            }
        });
    }
}
