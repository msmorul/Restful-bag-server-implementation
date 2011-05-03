package org.chronopolis.bagserver;

import java.util.List;

/**
 * Interface which provides access to a group of bags. This closely follows
 * 
 * @author toaster
 */
public interface BagVault {

    /**
     *
     * @param identifier identifier of bag to retrieve
     * @return bagentry if a bag with identifier exists, null otherwise
     */
    public BagEntry getBag(String identifier);

    /**
     * Return a list of all bag identifiers
     * @return
     */
    public List<BagEntry> getBags();

    /**
     * Create a new bag with the specified identifier
     * 
     * @param newIdentifier
     * @return entry of newly created bag
     */
    public BagEntry createNewBag(String newIdentifier);

    /**
     * test to see if a bag exists, must return true if the bag has been created
     * regardless of commit state
     *
     * @param identifier
     * @return true if bag exists
     */
    public boolean bagExists(String identifier);
//    /**
//     * Remove a bag from the server
//     *
//     * @param identifier bag to remove
//     * @return true if bag was deleted, false otherwise
//     * @throws IllegalStateException if bag cannot be removed due to open files
//     */
//    boolean deleteBag(String identifier) throws IllegalStateException;
//
//    boolean commit(String identifier);
//    public void validate(String identifier);
}
