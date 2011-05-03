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
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.chronopolis.bagserver.disk.SimpleDiskVault;
import org.chronopolis.restserver.BagServer;
import org.mortbay.component.LifeCycle;
import org.mortbay.component.LifeCycle.Listener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 *
 * @author toaster
 */
public class MainWindow extends Window implements Bindable {

    private static final Logger LOG = Logger.getLogger(MainWindow.class);
    private Server server;
    @BXML
    private PushButton startServer;
    @BXML
    private TextInput port;

    public void initialize(Map<String, Object> map, URL url, final Resources rsrcs) {

        startServer.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                try {
                    if (server != null) {
                        server.stop();
                        startServer.setButtonData("Start Server");
                        LOG.debug("Jetty server stopped");
                        return;
                    }
                } catch (Exception e) {

                    LOG.error("Error shutting down", e);
                    return;
                }
                startServer.setButtonData("Stop Server");
                int p = Integer.parseInt(port.getText());
                ServletHolder sh = new ServletHolder(ServletContainer.class);

                sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
                sh.setInitParameter("com.sun.jersey.config.property.packages", "org.chronopolis.restserver");

                LOG.trace("Starting jetty server");
                server = new Server(p);
                Context context = new Context(server, "/", Context.SESSIONS);
                context.addServlet(sh, "/*");
                context.getServletContext().setAttribute(BagServer.VAULT, new SimpleDiskVault(new File("/tmp/bagvault")));

                try {
                    server.start();
                } catch (Exception e) {
                    LOG.error("Error starting up", e);
                }

            }
        });
    }
}
