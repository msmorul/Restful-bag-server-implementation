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
    public static final String BAG_FILE = "BagIt-Version: " + VERSION + "\nTag-File-Character-Encoding: " + ENCODING + "\n";
    public static final String BAD_BAG_FILE = "BagIts-Version: 0.96\nTag-File-Character-Encoding: UTF-8\n";

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
