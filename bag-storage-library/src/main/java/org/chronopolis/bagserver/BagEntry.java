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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 
 * @author toaster
 */
public interface BagEntry {

    public enum State {

        COMMITTED, NONEXISTENT, OPEN
    }

    public List<String> listTagFiles();
    
    public String getIdentifier();

    public State getBagState();

    /**
     * Test bag for completeness, not integrity
     * 
     * @return true if manifests all contain same files and all files listed manifests are present
     */
//    public boolean isComplete();

    /**
     * Mark a file as committed.
     *
     * @return
     */
    public boolean commit();

    public boolean delete();

    public boolean setBagItInformation(BagIt bagIt);

    public boolean setBagInfo(BagInfo baginfo);

    public BagInfo getBagInfo();

    public BagIt getBagIt();

    /**
     * List all data files in this collection
     * @Return list of data files relative to bag root.
     * 
     */
    public List<String> listDataFiles();

    public InputStream openTagInputStream(String tagItem) throws IllegalArgumentException;
    public OutputStream openTagOutputStream(String tagItem) throws IllegalArgumentException;

    /**
     * Return a data file from this bag.
     * @param fileIdentifier path to data file relative to data directory. identifier will NOT contain 'data'
     * @return file stream, or null if no such file
     * @throws IllegalArgumentException if identifier is not a valid name
     */
    public OutputStream openDataOutputStream(String fileIdentifier) throws IllegalArgumentException;

    /**
     * 
     * @param fileIdentifier path to data file relative to data directory. identifier will NOT contain 'data'
     * @return file stream, or null if no such file
     * @throws IllegalArgumentException if identifier is not a valid name 
     */
    public InputStream openDataInputStream(String fileIdentifier) throws IllegalArgumentException;
}
