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
package org.chronopolis.bag.client;

import java.net.URL;
import java.io.File;
import org.chronopolis.bagserver.BagVault;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.log4j.BasicConfigurator;
import org.chronopolis.bagserver.NullDiskVault;
import org.chronopolis.restserver.BagServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toaster
 */
public class JsonGatewayTest {

    private static Server server;
    private static final int port = 7878;
//    private static File vFile;
    private JsonGateway instance;

    public JsonGatewayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
        BagVault vault = new NullDiskVault();

        ServletHolder sh = new ServletHolder(ServletContainer.class);

        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "org.chronopolis.restserver");
        sh.setInitParameter(BagServer.VAULT, "vault1");

        server = new Server(port);

        // add jersey endpoint
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(sh, "/*");
        context.getServletContext().setAttribute("vault1", vault);
        server.start();


    }

//    private static void clearDirectory(File dir) {
//        for (File f : dir.listFiles()) {
//            if (f.isFile()) {
//                f.delete();
//            }
//            if (f.isDirectory()) {
//                clearDirectory(f);
//            }
//        }
//        dir.delete();
//    }
    @AfterClass
    public static void tearDownClass() throws Exception {
        server.stop();
    }

    @Before
    public void setUp() throws Exception {
        instance = new JsonGateway(new URL("http://localhost:7878/bags"));

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of listBags method, of class JsonGateway.
     */
    @Test
    public void testListBags() throws Exception {
        System.out.println("listBags");
        BagList bl = instance.listBags();
        assertNotNull(bl);
        assertNotNull(bl.getObjects());

        bl = instance.listBags();
        System.out.println(bl);
        assertEquals(1, bl.getObjects().size());
    }

    @Test
    public void testManifest() throws Exception {
        System.out.println("listmanifest");

        assertNull(instance.getManifest("newb"));

        Manifest m = instance.getManifest("cbag");
        System.out.println("manifest " + m);
        assertNotNull(m.getPayload());

    }
}
