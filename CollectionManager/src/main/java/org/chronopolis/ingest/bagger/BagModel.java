/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import java.io.File;
import org.apache.pivot.util.ListenerList;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.apache.log4j.Logger;

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
    private static final Logger LOG = Logger.getLogger(BagModel.class);

    public ListenerList<BagModelListener> getModelListenerList() {
        return listenerList;
    }

    public String getChronopolisBag() {
        return chronopolisBag;
    }

    public BagType getBagType() {
        return bagType;
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

    public IngestionType getIngestionType() {
        return ingestionType;
    }

    public void setChronopolisBag(String chronopolisBag) {
        LOG.trace("BagModel/chronopolisBag: " + chronopolisBag);
        String old = this.chronopolisBag;
        this.chronopolisBag = chronopolisBag;
        listenerList.chronopolisBagChanged(this, old);
    }

    public void setIngestionType(IngestionType ingestionType) {
        LOG.trace("BagModel/ingestionType: " + ingestionType);
        IngestionType old = this.ingestionType;
        this.ingestionType = ingestionType;
        listenerList.ingestionTypeChanged(this, old);
    }

    public void setBagType(BagType bagType) {
        LOG.trace("BagModel/bagType: " + bagType);
        BagType old = this.bagType;
        this.bagType = bagType;
        listenerList.bagTypeChanged(this, old);
    }

    public void setChronPackage(ChronPackage chronPackage) {
        LOG.trace("BagModel/chronPacakge: " + chronPackage);
        ChronPackage old = this.chronPackage;
        this.chronPackage = chronPackage;
        listenerList.chronPackageChanged(this, old);
    }

    public void setSaveFile(File saveFile) {
        LOG.trace("BagModel/saveFile: " + saveFile);
        File old = this.saveFile;
        this.saveFile = saveFile;
        listenerList.saveFileChanged(this, old);
    }

    public void setUrlPattern(String urlPattern) {
        LOG.trace("BagModel/urlPattern: " + urlPattern);
        String old = this.urlPattern;
        this.urlPattern = urlPattern;
        listenerList.urlPatternChanged(this, old);
    }

    public class BagModelListenerList extends ListenerList<BagModelListener> {

        void chronopolisBagChanged(BagModel model, String old) {
            if (model.getChronopolisBag() != null
                    && !model.getChronopolisBag().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.chronopoligBagChanged(model, old);
                }
            }
        }

        void ingestionTypeChanged(BagModel model, IngestionType old) {
            if (model.getIngestionType() != null
                    && !model.getIngestionType().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.ingestionTypeChanged(model, old);
                }
            }
        }

        void bagTypeChanged(BagModel model, BagType old) {
            if (model.getBagType() != null 
                    && !model.getBagType().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.bagTypeChanged(model, old);
                }
            }
        }

        void chronPackageChanged(BagModel model, ChronPackage old) {
            if (model.getChronPackage() != null
                    && !model.getChronPackage().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.chronPackageChanged(model, old);
                }
            }
        }

        void saveFileChanged(BagModel model, File old) {
            if (model.getSaveFile() != null
                    && !model.getSaveFile().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.saveFileChanged(model, old);
                }
            }
        }

        void urlPatternChanged(BagModel model, String old) {
            if (model.getUrlPattern() != null
                    && !model.getUrlPattern().equals(old)) {
                for (BagModelListener l : this) {
                    l.urlPatternChanged(model, old);
                }
            }
        }
    }
}
