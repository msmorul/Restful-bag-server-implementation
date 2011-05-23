/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

/**
 *
 * @author toaster
 */
public interface BagCheckListener {

    public void missingBagInfo(BagChecker checker);

    public void missingBagIt(BagChecker checker);

    public void threadEnding(BagChecker checker);

    public class Adapter implements BagCheckListener {

        public void threadEnding(BagChecker checker) {
        }

        public void missingBagIt(BagChecker checker) {
        }

        public void missingBagInfo(BagChecker checker) {
        }
    }
}
