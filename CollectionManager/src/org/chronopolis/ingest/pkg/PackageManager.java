/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.pkg;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.collections.Sequence;

/**
 *
 * @author toaster
 */
public class PackageManager {

    private List<ChronPackage> pkgList = new WrappedList();
    private PackageListener pkgListener = new PackageListener();
    private Preferences prefs;
    private String TOTAL_PACKAGES = "totalpackages";
    private String PREFIX_NAME = "name-";
    private String PREFIX_DIGEST = "digest-";
    private String PREFIX_DIRS = "directories-";
    private String PREFIX_READ = "readonly-";

    public PackageManager() {
        pkgList.getListListeners().add(new SaveListener());
        List<ChronPackage> tmplist = new ArrayList<ChronPackage>();
        prefs = Preferences.userNodeForPackage(this.getClass());


        int totPackages = prefs.getInt(TOTAL_PACKAGES, 0);

        for (int i = 0; i < totPackages; i++) {

            ChronPackage loadPkg = new ChronPackage();
            loadPkg.setName(prefs.get(PREFIX_NAME + i, ""));
            loadPkg.setDigest(prefs.get(PREFIX_DIGEST + i, "SHA-256"));
            loadPkg.setReadOnly(prefs.getBoolean(PREFIX_READ + i, false));
            String dirs = prefs.get(PREFIX_DIRS + i, null);
            if (dirs != null) {
                for (String d : dirs.split(",")) {
                    loadPkg.getRootList().add(new File(d));
                }
            }
            tmplist.add(loadPkg);
        }

        for (ChronPackage pkg : tmplist) {
            pkgList.add(pkg);
        }
    }

    private void writeList() throws BackingStoreException {
        prefs.clear();
        prefs.putInt(TOTAL_PACKAGES, pkgList.getLength());
        for (int i = 0; i < pkgList.getLength(); i++) {
            writePackage(i, pkgList.get(i));
        }
        prefs.flush();

    }

    private void writePackage(int index, ChronPackage p) throws BackingStoreException {
        prefs.put(PREFIX_NAME + index, p.getName());
        prefs.put(PREFIX_DIGEST + index, p.getDigest());
        prefs.putBoolean(PREFIX_READ + index, p.isReadOnly());

        if (p.getRootList().getLength() > 0) {
            String dirList = p.getRootList().get(0).getAbsolutePath();
            for (int i = 1; i < p.getRootList().getLength(); i++) {
                dirList += "," + p.getRootList().get(i).getAbsolutePath();
            }
            prefs.put(PREFIX_DIRS + index, dirList);
        }
        else
        {
            prefs.remove(PREFIX_DIRS + index);
        }
        prefs.flush();
    }

    public List<ChronPackage> getPackageList() {
        return pkgList;
    }

    private class SaveListener extends ListListener.Adapter<ChronPackage> {

        @Override
        public void itemInserted(List<ChronPackage> list, int index) {
            ChronPackage newPkg = list.get(index);
            newPkg.getChronPackageListeners().add(pkgListener);
            newPkg.getRootList().getListListeners().add(new PkgRootListener(newPkg));

            try {
                writeList();
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void listCleared(List<ChronPackage> list) {
            try {
                writeList();
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void itemsRemoved(List<ChronPackage> list, int i, Sequence<ChronPackage> sqnc) {
            for (int j = 0; j < sqnc.getLength(); j++) {
                sqnc.get(j).getChronPackageListeners().remove(pkgListener);
                sqnc.get(j).getRootList().getListListeners().remove(new PkgRootListener(sqnc.get(j)));
            }
            try {
                writeList();
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void itemUpdated(List<ChronPackage> list, int i, ChronPackage t) {
            list.get(i).getChronPackageListeners().remove(pkgListener);
            list.get(i).getRootList().getListListeners().remove(new PkgRootListener(list.get(i)));

            t.getChronPackageListeners().add(pkgListener);
            t.getRootList().getListListeners().add(new PkgRootListener(t));

            try {
                writeList();
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class PkgMetadataListener extends MapListener.Adapter<String, String> {
        
    }

    private class PkgRootListener extends ListListener.Adapter<File> {

        private ChronPackage pkg;

        public PkgRootListener(ChronPackage pkg) {
            this.pkg = pkg;
        }

        @Override
        public void itemInserted(List<File> list, int index) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void itemUpdated(List<File> list, int index, File previousItem) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void listCleared(List<File> list) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void itemsRemoved(List<File> list, int index, Sequence<File> items) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof PkgRootListener && ((PkgRootListener) obj).pkg == pkg);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 43 * hash + (this.pkg != null ? this.pkg.hashCode() : 0);
            return hash;
        }
    }

    private class PackageListener implements ChronPackageListener {

        public void nameChanged(ChronPackage pkg, String oldname) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        public void readOnlyChanged(ChronPackage pkg, boolean old) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }

        public void digestChanged(ChronPackage pkg, String oldDigest) {
            try {
                writePackage(pkgList.indexOf(pkg), pkg);
            } catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class WrappedList extends ArrayList<ChronPackage> {

        @Override
        public void clear() {
            for (ChronPackage c : this) {
                c.getChronPackageListeners().remove(pkgListener);
                c.getRootList().getListListeners().remove(new PkgRootListener(c));
            }
            super.clear();
        }
    }
}
