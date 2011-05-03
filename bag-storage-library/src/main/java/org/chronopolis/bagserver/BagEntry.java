/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

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

    public boolean commit();

    public boolean delete();

    public boolean setBagItInformation(BagIt bagIt);

    public boolean setBagInfo(BagInfo baginfo);

}
