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
package org.chronopolis.ingest.bagger;

import java.io.File;
import org.apache.pivot.util.ListenerList;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.apache.log4j.Logger;
import org.chronopolis.ingest.pkg.ChronPackage.Statistics;
import org.chronopolis.ingest.pkg.ChronPackageListener;

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
    private Statistics bagStats = null;
    private static final Logger LOG = Logger.getLogger(BagModel.class);
    private ChronPackageListener pkgListener = new ChronPackageListener.Adapter() {

        @Override
        public void nameChanged(ChronPackage pkg, String oldname) {
            setChronopolisBag(pkg.getName());
        }
    };

    public ListenerList<BagModelListener> getModelListenerList() {
        return listenerList;
    }

    public Statistics getBagStats() {
        return bagStats;
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

    public void setBagStats(Statistics bagStats) {
        LOG.debug("BagModel/stats: " + bagStats.getSize());
        Statistics old = this.bagStats;
        this.bagStats = bagStats;
        listenerList.bagStatsChanged(this, old);
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
        if (old != null) {
            old.getChronPackageListeners().remove(pkgListener);
        }
        if (chronPackage != null) {
            chronPackage.getChronPackageListeners().add(pkgListener);
        }
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

        void bagStatsChanged(BagModel model, Statistics old) {
            if (model.getBagStats() != null
                    && !model.getBagStats().equals(old)) {
                for (BagModelListener l : listenerList) {
                    l.bagStatsChanged(model, old);
                }
            }
        }

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
