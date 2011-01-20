/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import java.io.File;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackage.Statistics;

/**
 *
 * @author toaster
 */
public interface BagModelListener {

    public void chronPackageChanged(BagModel model, ChronPackage oldpackage);

    public void ingestionTypeChanged(BagModel mode, IngestionType oldType);

    public void bagTypeChanged(BagModel model, BagType oldType);

    public void urlPatternChanged(BagModel model, String oldPattern);

    public void saveFileChanged(BagModel model, File oldFile);

    public void chronopoligBagChanged(BagModel mode, String oldbagname);

    public void bagStatsChanged(BagModel model, ChronPackage.Statistics old);

    public class Adaptor implements BagModelListener
    {

        public void bagStatsChanged(BagModel model, Statistics old) {
        }

        public void chronPackageChanged(BagModel model, ChronPackage oldpackage) {
        }

        public void ingestionTypeChanged(BagModel mode, IngestionType oldType) {
        }

        public void bagTypeChanged(BagModel model, BagType oldType) {
        }

        public void urlPatternChanged(BagModel model, String oldPattern) {
        }

        public void saveFileChanged(BagModel model, File oldFile) {
        }

        public void chronopoligBagChanged(BagModel mode, String oldbagname) {
        }

    }
}
