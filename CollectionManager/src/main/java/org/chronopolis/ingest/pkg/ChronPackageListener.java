/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.pkg;

/**
 *
 * @author toaster
 */
public interface ChronPackageListener {

    public void nameChanged(ChronPackage pkg, String oldname);
    public void readOnlyChanged(ChronPackage pkg, boolean old);
    public void digestChanged(ChronPackage pkg, String oldDigest);
    
    public class Adapter implements ChronPackageListener
    {

        public void nameChanged(ChronPackage pkg, String oldname) {
        }

        public void readOnlyChanged(ChronPackage pkg, boolean old) {
        }

        public void digestChanged(ChronPackage pkg, String oldDigest) {
        }

    }
}
