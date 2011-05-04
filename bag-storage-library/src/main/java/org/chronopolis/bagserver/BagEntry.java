/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author toaster
 */
public interface BagEntry {

    public enum State {

        COMMITTED, NONEXISTENT, OPEN
    }

    public String getIdentifier();

    public State getBagState();

    /**
     * Test bag for completeness, not integrity
     * 
     * @return true if manifests all contain same files and all files listed manifests are present
     */
    public boolean isComplete();

    /**
     * Mark a file as committed.
     *
     * @return
     */
    public boolean commit();

    public boolean delete();

    public boolean setBagItInformation(BagIt bagIt);

    public boolean setBagInfo(BagInfo baginfo);
    //TODO: handle extra tag files

    public InputStream openTagStream(String tagItem) throws IllegalArgumentException;

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
