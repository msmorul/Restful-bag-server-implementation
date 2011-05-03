/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver.disk;

import org.chronopolis.bagserver.BagEntry.State;
import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.chronopolis.bagserver.BagEntry;
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
