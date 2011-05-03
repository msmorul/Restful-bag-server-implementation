/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.bagserver;

/**
 *
 * @author toaster
 */
public class ValidationState {

    public enum Status
    {
        IN_PROGRESS, FAILED, SUCCESSFUL;
    }
    private int filesValidated;
    private String summary;
    //todo: manifest list

}
