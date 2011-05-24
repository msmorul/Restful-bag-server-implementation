/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.exe;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
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
public class Main {

    public static void main(String[] args) throws Exception {

        BasicConfigurator.configure();
        ServerArgs settings = new ServerArgs();
        JCommander cmd = new JCommander(settings);


        if (settings.isUsage()) {
            cmd.usage();
            return;
        }

        try
        {
        cmd.parse(args);
        }
        catch (ParameterException e)
        {
            System.out.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }

        File dir = settings.getDirectory();
        if (!dir.isDirectory() && !dir.mkdirs()) {
            System.err.println("Cannot create bag directory: " + dir);
            System.exit(1);
        }
        BagVault vault = new SimpleDiskVault(dir);

        ServletHolder sh = new ServletHolder(ServletContainer.class);

        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.chronopolis.restserver");
        sh.setInitParameter(BagServer.VAULT, "vault1");
        System.out.println("Starting jetty server");
        Server server = new Server(settings.getPort());

        // add jersey endpoint
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(sh, "/*");
        context.getServletContext().setAttribute("vault1", vault);
        server.start();
        server.join();
    }
}
