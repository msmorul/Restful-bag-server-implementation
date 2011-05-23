/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.bagserver;

import java.security.MessageDigest;
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
public class DigestEnumTest {

    public DigestEnumTest() {
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
     * Test of valueOfManifest method, of class DigestEnum.
     */
    @Test
    public void testValueOfManifest() {
        System.out.println("valueOfManifest");
        String digestname = "manifest-md5.txt";
        DigestEnum expResult = DigestEnum.MD5;
        DigestEnum result = DigestEnum.valueOfManifest(digestname);
        assertEquals(expResult, result);
        digestname = "manifest-sha256.txt";
         expResult = DigestEnum.SHA256;
         result = DigestEnum.valueOfManifest(digestname);
        assertEquals(expResult, result);
    }

    /**
     * Test of createDigest method, of class DigestEnum.
     */
    @Test
    public void testCreateDigest() {
        System.out.println("createDigest");
        DigestEnum instance = DigestEnum.MD5;
        assertNotNull(instance.createDigest());
        instance = DigestEnum.SHA256;
        assertNotNull(instance.createDigest());
        // TODO review the generated test code and remove the default call to fail.
    }

}