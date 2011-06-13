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
package org.chronopolis.bagserver;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
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
public class BagInfoTest {

    public static final String BAGINFO_FILE = "Bagging-Date: 2011-04-13\nBag-Size: 139 MB\n";
    public static final String BAGINFO_FILE2 = "Bagging-Date: 2011-04-13\nBag-Size: 139\n  MB\n";

    public BagInfoTest() {
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
     * Test of getBagAttributes method, of class BagInfo.
     */
//    @Test
//    public void testGetBagAttributes() {
//        System.out.println("getBagAttributes");
//        BagInfo instance = null;
//        Map expResult = null;
//        Map result = instance.getBagAttributes();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
////        fail("The test case is a prototype.");
//    }
    /**
     * Test of setBagAttribute method, of class BagInfo.
     */
//    @Test
//    public void testSetBagAttribute() {
//        System.out.println("setBagAttribute");
//        String attribute = "";
//        String value = "";
//        BagInfo instance = null;
//        String expResult = "";
//        String result = instance.setBagAttribute(attribute, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of removeAttribute method, of class BagInfo.
     */
//    @Test
//    public void testRemoveAttribute() {
//        System.out.println("removeAttribute");
//        String attribute = "";
//        BagInfo instance = null;
//        String expResult = "";
//        String result = instance.removeAttribute(attribute);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getAttribute method, of class BagInfo.
     */
//    @Test
//    public void testGetAttribute() {
//        System.out.println("getAttribute");
//        String attribute = "";
//        BagInfo instance = null;
//        String expResult = "";
//        String result = instance.getAttribute(attribute);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of readInfo method, of class BagInfo.
     */
    @Test
    public void testReadInfo() throws Exception {
        System.out.println("readInfo");
        Reader r = new StringReader(BAGINFO_FILE);

        BagInfo bi = BagInfo.readInfo(r);
        assertEquals("2011-04-13", bi.getAttribute("Bagging-Date"));
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    /**
     * Test of writeInfo method, of class BagInfo.
     */
//    @Test
//    public void testWriteInfo() throws Exception {
//        System.out.println("writeInfo");
//        Writer w = null;
//        BagInfo instance = null;
//        instance.writeInfo(w);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
