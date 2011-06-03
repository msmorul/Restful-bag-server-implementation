/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.bagserver;

import java.io.IOException;

/**
 *
 * @author toaster
 */
public interface BagCheckListener {

    public void badBagLine(BagChecker checker, String file, String line);

    public void missingBagInfo(BagChecker checker);

    public void missingBagIt(BagChecker checker);

    public void threadEnding(BagChecker checker);
    public void missingFile(BagChecker checker, String file);
    public void errorReadingFile(BagChecker checker, String file, IOException e);

    public void missingDigest(BagChecker checker, String file);

    public void corruptFile(BagChecker checker, String file, String expected, String seen);

    public class Adapter implements BagCheckListener {

        public void missingDigest(BagChecker checker, String file) {
        }

        public void errorReadingFile(BagChecker checker, String file, IOException e) {
        }

        public void corruptFile(BagChecker checker, String file, String expected, String seen) {
        }

        public void missingFile(BagChecker checker, String file) {
        }

        public void badBagLine(BagChecker checker, String file, String line) {
        }

        public void threadEnding(BagChecker checker) {
        }

        public void missingBagIt(BagChecker checker) {
        }

        public void missingBagInfo(BagChecker checker) {
        }
    }
}
