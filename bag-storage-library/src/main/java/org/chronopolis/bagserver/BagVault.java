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
