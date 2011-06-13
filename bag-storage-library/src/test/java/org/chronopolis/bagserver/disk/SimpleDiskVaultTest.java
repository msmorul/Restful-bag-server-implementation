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
package org.chronopolis.bagserver.disk;

import java.io.OutputStream;
import org.chronopolis.bagserver.BagEntry.State;
import java.io.File;
import java.io.StringReader;
import org.apache.log4j.BasicConfigurator;
import org.chronopolis.bagserver.BagEntry;
import org.chronopolis.bagserver.BagInfo;
import org.chronopolis.bagserver.BagInfoTest;
import org.chronopolis.bagserver.BagIt;
import org.chronopolis.bagserver.BagItTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author toaster
 */
public class SimpleDiskVaultTest {

    private SimpleDiskVault instance;
    private File dir;
    private static final String MANIFEST = "a4df   data/test";

    public SimpleDiskVaultTest() {
    }

    @BeforeClass
    public static void logconfig() {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        dir = new File("/tmp/bagtest" + System.currentTimeMillis());
        if (!dir.mkdir()) {
            throw new RuntimeException("cannor create " + dir);

        }
        instance = new SimpleDiskVault(dir);
    }

    @After
    public void tearDown() throws Exception {
        clearDirectory(dir);
    }

    private void clearDirectory(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                f.delete();
            }
            if (f.isDirectory()) {
                clearDirectory(f);
            }
        }
        dir.delete();
    }

    @Test
    public void testGetBag() throws Exception {
        System.out.println("testgetBag");
        String newIdentifier = "mynewbag";
        BagEntry be = instance.createNewBag(newIdentifier);
        assertNotNull(instance.getBag(newIdentifier));
    }

    @Test
    public void testGetBaglist() throws Exception {
        System.out.println("testgetBaglist");
        String newIdentifier = "mynewbag";
        assertEquals(0, instance.getBags().size());
        instance.createNewBag(newIdentifier);
        assertEquals(1, instance.getBags().size());
        instance.getBag(newIdentifier).delete();
        assertEquals(0, instance.getBags().size());

    }

    @Test
    public void testSetBagIt() throws Exception {
        System.out.println("testSetBagIt");
        String newIdentifier = "mynewbag";
        BagEntry be = instance.createNewBag(newIdentifier);
        be.setBagItInformation(BagIt.readFile(new StringReader(BagItTest.BAG_FILE)));
        assertTrue(new File(dir, "work/mynewbag/bagit.txt").isFile());
    }

    @Test
    public void testSetBagInfo() throws Exception {
        System.out.println("testSetBagInfo");
        String newIdentifier = "mynewbag";
        BagEntry be = instance.createNewBag(newIdentifier);
        be.setBagInfo(BagInfo.readInfo(new StringReader(BagInfoTest.BAGINFO_FILE)));
        assertTrue(new File(dir, "work/mynewbag/bag-info.txt").isFile());
    }

//    @Test
//    public void testIsComplete() throws Exception {
//        System.out.println("testisComplete");
//        String newIdentifier = "mynewbag";
//        BagEntry be = instance.createNewBag(newIdentifier);
//        assertFalse(be.isComplete());
//        be.setBagItInformation(BagIt.readFile(new StringReader(BagItTest.BAG_FILE)));
//        be.setBagInfo(BagInfo.readInfo(new StringReader(BagInfoTest.BAGINFO_FILE)));
//        assertFalse(be.isComplete());
//        OutputStream os = be.openTagOutputStream("manifest-test.txt");
//        os.write(MANIFEST.getBytes());
//        os.close();
//        assertFalse(be.isComplete());
//        os = be.openDataOutputStream("test");
//        os.write(MANIFEST.getBytes());
//        os.close();
//        assertTrue(be.isComplete());
//        os = be.openDataOutputStream("test2");
//        os.write(MANIFEST.getBytes());
//        os.close();
//        assertFalse(be.isComplete());
//    }

    @Test
    public void testCommit() {
        System.out.println("testcommit");
        String newIdentifier = "mynewbag";
        BagEntry be = instance.createNewBag(newIdentifier);
        be.commit();
        assertEquals(be, instance.getBag(newIdentifier));
        assertFalse(new File(dir, "work/" + newIdentifier).exists());
        assertTrue(new File(dir, "commited/" + newIdentifier).exists());
        assertEquals(State.COMMITTED, be.getBagState());
    }

    @Test
    public void equals() {
        System.out.println("testing equality");
        String id1 = "mynewbag";
        String id2 = "bag2";

        BagEntry be = instance.createNewBag(id1);
        BagEntry be2 = instance.createNewBag(id2);

        assertEquals(be, be);
        assertFalse(be.equals(be2));

    }

    /**
     * Test of createNewBag method, of class SimpleDiskVault.
     */
    @Test
    public void testCreateNewBag() {
        System.out.println("createNewBag");
        String newIdentifier = "mynewbag";
        String id2 = "../bag2";

        BagEntry be = instance.createNewBag(newIdentifier);
        assertEquals(newIdentifier, be.getIdentifier());
        assertEquals(newIdentifier, instance.createNewBag(newIdentifier).getIdentifier());
        assertEquals(be, instance.getBag(newIdentifier));
        assertEquals(1, instance.getBags().size());
        assertEquals(State.OPEN, be.getBagState());

        assertTrue(new File(dir, "work/" + newIdentifier).isDirectory());

        // create bag in bad local
        try {
            instance.createNewBag(id2);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDelete() {
        System.out.println("testDelete");
        String newIdentifier = "mynewbag";
        BagEntry be = instance.createNewBag(newIdentifier);
        assertTrue(be.delete());
        assertEquals(State.NONEXISTENT, be.getBagState());
        assertNull(instance.getBag(newIdentifier));
        assertFalse(new File(dir, "work/" + newIdentifier).exists());

        be = instance.createNewBag(newIdentifier);
        assertTrue(be.commit());
        assertTrue(be.delete());
        assertFalse(new File(dir, "committed/" + newIdentifier).exists());

    }

    /**
     * Test of getBagIdentifiers method, of class SimpleDiskVault.
     */
    @Test
    public void testGetBagIdentifiers() {
        System.out.println("getBagIdentifiers");
        assertNotNull(instance.getBags());
        assertEquals(0, instance.getBags().size());
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
