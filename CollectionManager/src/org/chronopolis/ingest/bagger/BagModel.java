/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import java.io.File;
import org.apache.pivot.util.ListenerList;
import org.chronopolis.ingest.pkg.ChronPackage;

/**
 *
 * @author toaster
 */
public class BagModel {

    public enum IngestionType {

        LOCAL("Local Bag"), CHRONOPOLIS("Chronopolis");
        private String desc;

        IngestionType(String desc) {
            this.desc = desc;
        }

        public String getDescription() {
            return desc;
        }
    }

    public enum BagType {

        HOLEY("Holey Bag"), FILLED("Filled bag");
        private String desc;

        BagType(String desc) {
            this.desc = desc;
        }

        public String getDescription() {
            return desc;
        }
    }
    private IngestionType ingestionType;
    private BagType bagType;
    private ChronPackage chronPackage;
    private String urlPattern;
    private File saveFile;
    private String chronopolisBag;
    private BagModelListenerList listenerList = new BagModelListenerList();

    public ListenerList<BagModelListener> getModelListenerList() {
        return listenerList;
    }

    public String getChronopolisBag() {
        return chronopolisBag;
    }

    public void setChronopolisBag(String chronopolisBag) {
        String old = this.chronopolisBag;
        this.chronopolisBag = chronopolisBag;
        for (BagModelListener l : listenerList) {
            l.chronopoligBagChanged(this, old);
        }
    }

    public IngestionType getIngestionType() {
        return ingestionType;
    }

    public void setIngestionType(IngestionType ingestionType) {
        IngestionType old = this.ingestionType;
        this.ingestionType = ingestionType;
        for (BagModelListener l : listenerList) {
            l.ingestionTypeChanged(this, old);
        }
    }

    public BagType getBagType() {
        return bagType;
    }

    public void setBagType(BagType bagType) {
        BagType old = this.bagType;
        this.bagType = bagType;
        for (BagModelListener l : listenerList) {
            l.bagTypeChanged(this, bagType);
        }
    }

    public void setChronPackage(ChronPackage chronPackage) {
        ChronPackage old = this.chronPackage;
        this.chronPackage = chronPackage;
        for (BagModelListener l : listenerList) {
            l.chronPackageChanged(this, old);
        }

    }

    public ChronPackage getChronPackage() {
        return chronPackage;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setSaveFile(File saveFile) {
        File old = this.saveFile;
        this.saveFile = saveFile;
        for (BagModelListener l : listenerList) {
            l.saveFileChanged(this, old);
        }
    }

    public void setUrlPattern(String urlPattern) {
        String old = this.urlPattern;
        this.urlPattern = urlPattern;
        listenerList.urlPatternChanged(this, old);
    }

    public class BagModelListenerList extends ListenerList<BagModelListener> {

        void urlPatternChanged(BagModel model, String old) {
            for (BagModelListener l : this) {
                l.urlPatternChanged(model, old);
            }
        }
    }
}
