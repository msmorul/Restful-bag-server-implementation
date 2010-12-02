/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;

/**
 *
 * @author toaster
 */
public class ChronPackage {

    private String name = "";
    private String digest = "SHA-256";
    private List<File> rootList = new ArrayList();
    private List<ChronPackageListener> listeners = new ArrayList<ChronPackageListener>();
    private Map<String,String> metadataMap = new HashMap<String, String>();
    private boolean readOnly = false;

    public String getName() {
        return name;
    }

    public void setReadOnly(boolean readOnly) {
        boolean old = this.readOnly;
        this.readOnly = readOnly;
        for (ChronPackageListener l : listeners) {
            l.readOnlyChanged(this, old);
        }
    }

    /**
     * Return the first file to be written in this bag.
     * 
     * @return first data file (ie, data/somedir/file4.txt)
     */
    public String findFirstFile()
    {
        if (rootList.isEmpty())
            return null;
        
        return trollForFirst(rootList.get(0));
    }

    private String trollForFirst(File dir)
    {
        
        File firstDir = null;
        for (File f : dir.listFiles())
        {
            if (f.isFile())
                return dir.getName() + "/" + f.getName();
            else if (firstDir == null && f.isDirectory())
                firstDir = f;
        }
        if (firstDir == null)
            return dir.getName();
        
        return dir.getName() + "/" + trollForFirst(firstDir);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getDigest() {
        return digest;
    }

    public String getBagFormattedDigest()
    {
        return digest.toLowerCase().replaceAll("[^a-z0-9]","");
    }

    public void setDigest(String digest) {
        String old = this.digest;
        this.digest = digest;
        for (ChronPackageListener l : listeners )
        {
            l.digestChanged(this, old);
        }
    }
    public void setName(String name) {
        String old = this.name;
        this.name = name;
        for (ChronPackageListener p : listeners) {
            p.nameChanged(this, old);
        }
    }

    public Map<String, String> getMetadataMap() {
        return metadataMap;
    }

    
    public List<File> getRootList() {
        return rootList;
    }

    public List<ChronPackageListener> getChronPackageListeners() {
        return listeners;
    }
}
