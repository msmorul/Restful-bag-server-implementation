/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
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
public class BagItTest {

    private static final String VERSION = "0.96";
    private static final String ENCODING = "UTF-8";
    private static final String BAG_FILE = "BagIt-Version: " + VERSION + "\nTag-File-Character-Encoding: " + ENCODING + "\n";
    private static final String BAD_BAG_FILE = "BagIts-Version: 0.96\nTag-File-Character-Encoding: UTF-8\n";

    public BagItTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readFile method, of class BagIt.
     */
    @Test
    public void testReadFile() throws Exception {
        System.out.println("readFile");
        Reader is = new StringReader(BAG_FILE);
        BagIt result = BagIt.readFile(is);

        assertNotNull(result);
        assertEquals(VERSION, result.getVersion());
        assertEquals(ENCODING, result.getEncoding());

        try {
            BagIt.readFile(new StringReader(BAD_BAG_FILE));
            fail("Bad input should have io exceptioned");
        } catch (IOException e) {
        }

    }

    /**
     * Test of writeFile method, of class BagIt.
     */
    @Test
    public void testWriteFile() throws Exception {
        System.out.println("writeFile");
        BagIt result = BagIt.readFile(new StringReader(BAG_FILE));
        StringWriter sw = new StringWriter();
        result.writeFile(sw);
        assertEquals(BAG_FILE, sw.toString());

        sw = new StringWriter();
        new BagIt(ENCODING + " ", VERSION).writeFile(sw);
        assertEquals(BAG_FILE, sw.toString());
    }
}
