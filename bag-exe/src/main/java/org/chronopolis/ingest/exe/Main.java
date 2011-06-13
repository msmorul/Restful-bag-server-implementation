/*
 * Copyright (c) 2007-2011, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * Neither the name of the University of Maryland nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ACE Components were written in the ADAPT Project at the University of
 * Maryland Institute for Advanced Computer Study.
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
