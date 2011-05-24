/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
