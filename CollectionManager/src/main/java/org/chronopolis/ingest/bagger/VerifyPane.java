/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import edu.umiacs.ace.json.Strings;
import java.io.File;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;
import org.chronopolis.ingest.pkg.BagWriter;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.UrlFormatter;
import org.apache.log4j.Logger;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.chronopolis.ingest.Util;

/**
 *
 * @author toaster
 */
public class VerifyPane extends BasePanel {

    private static final Logger LOG = Logger.getLogger(VerifyPane.class);
    @BXML
    private Label vrfyDestLbl;
    @BXML
    private Label vrfyTypeLbl;
    @BXML
    private Label vrfyPatternLbl;
    @BXML
    private Label vrfyLocationLbl;
    @BXML
    private Label vrfyPatternHdr;
    @BXML
    private Label vrfyFilesLbl;
    @BXML
    private Label vrfyDirectoriesLbl;
    @BXML
    private Label vrfySizeLbl;
    @BXML
    private Label vrfyUnreadableLbl;
    @BXML
    private ListView vrfyUnreadableList;
    @BXML
    private Border vrfyUnreadablePane;
    @BXML
    private TableView metadataTable;
    @BXML
    private TextInput vrfyFetchTxt;
    @BXML
    private Label vrfyFetchLbl;
    @BXML
    private TextInput vrfyManifestTxt;
    @BXML
    private TableView vrfyDirectoryTable;
    private BagModelListener listener = new MyListener();
    private List<MetadataPair> metadataTableModel = new ArrayList<MetadataPair>();
    private Boolean runVerify;
    private StatisticsStatusSheet statusSheet = new StatisticsStatusSheet();
    private ButtonPressListener statsSheetCloseListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            runVerify = false;
        }
    };
    private Task<ChronPackage.Statistics> updateTask = new Task<ChronPackage.Statistics>() {

        @Override
        public ChronPackage.Statistics execute() throws TaskExecutionException {
            try {
                return getBagModel().getChronPackage().createStatistics(
                        new ChronPackage.AbortScanNotifier() {

                            public boolean aborted() {
                                return !runVerify;
                            }
                        });
            } finally {
                runVerify = false;
            }
        }
    };
    private TaskListener<ChronPackage.Statistics> taskListener = new TaskListener<ChronPackage.Statistics>() {

        public void taskExecuted(Task<ChronPackage.Statistics> task) {
            ChronPackage.Statistics stats = task.getResult();
            if (stats != null) {
                vrfyFilesLbl.setText(Long.toString(stats.getFiles()));
                vrfyDirectoriesLbl.setText(Long.toString(stats.getDirectories()));
                vrfySizeLbl.setText(Util.formatSize(stats.getSize()));
                vrfyUnreadableLbl.setText(Long.toString(stats.getUnreadable()));
                vrfyUnreadablePane.setVisible(stats.getUnreadable() > 0);
                vrfyUnreadableList.setListData(stats.getUnreadableFiles());
            } else {
                vrfyFilesLbl.setText("scan aborted");
                vrfyDirectoriesLbl.setText("scan aborted");
                vrfySizeLbl.setText("scan aborted");
                vrfyUnreadableLbl.setText("scan aborted");
                vrfyUnreadablePane.setVisible(false);
            }
            setErrorMessage(null);
            statusSheet.close(true);
        }

        public void executeFailed(Task<ChronPackage.Statistics> task) {
            setErrorMessage("Reading Package Failed");
            if (!runVerify) {

                Alert.alert(MessageType.ERROR, "Could not scan bag", VerifyPane.this.getWindow());
            }
            statusSheet.close(true);
        }
    };
    private ComponentListener openListener = new ComponentListener.Adapter() {

        @Override
        public void visibleChanged(Component component) {

            if (component.isVisible() && runVerify) {

                statusSheet.open(VerifyPane.this.getWindow(), new SheetCloseListener() {

                    public void sheetClosed(Sheet sheet) {

                        if (!sheet.getResult()) {
                            runVerify = false;
                        }
                    }
                });
                updateTask.execute(taskListener);
            }
        }
    };

    public VerifyPane() {
        super("verifyPane.bxml");
        Accordion.setHeaderData(this, "Verify Bag Contents");
        metadataTable.setTableData(metadataTableModel);
        getComponentListeners().add(openListener);
        statusSheet.getCloseButtonPressListeners().add(statsSheetCloseListener);
    }

    @Override
    protected void modelChanged(BagModel old) {
        if (old != null) {
            old.getModelListenerList().remove(listener);
        }
        BagModel model = getBagModel();
        if (model != null) {
            model.getModelListenerList().add(listener);
        }

        runVerify = true;
        updateBagType(model);
        updateUrl(model);
        updateLocation(model);
        updateIngestionType(model);
        updateMetadata(model);
        updateDirectoryList(model);
    }

    public Vote isComplete() {
        return Vote.APPROVE;
    }

    private void updateDirectoryList(BagModel model) {
        ChronPackage chronPackage = model.getChronPackage();

        vrfyManifestTxt.setText("9a9a-" + chronPackage.getDigest()
                + "-digest-9a9a  data/" + chronPackage.findRelativeFirstFile());

        List<MetadataPair> fileTableModel = new ArrayList<MetadataPair>();

        for (File f : chronPackage.getRootList()) {
            MetadataPair mp = new MetadataPair(f.getName(), f.getPath());
            fileTableModel.add(mp);
        }

        vrfyDirectoryTable.setTableData(fileTableModel);
    }

    private void updateBagType(BagModel model) {
        if (model == null || model.getBagType() == null) {
            vrfyTypeLbl.setText("None Selected");
        } else if (model.getBagType() == BagType.HOLEY) {
            vrfyTypeLbl.setText("Holey Bag");
            vrfyPatternLbl.setVisible(true);
            vrfyPatternHdr.setVisible(true);
            vrfyFetchTxt.setVisible(true);
            vrfyFetchLbl.setVisible(true);

        } else if (model.getBagType() == BagType.FILLED) {
            vrfyTypeLbl.setText("Filled Bag");
            vrfyPatternLbl.setVisible(false);
            vrfyPatternHdr.setVisible(false);
            vrfyFetchTxt.setVisible(false);
            vrfyFetchLbl.setVisible(false);
        }
    }

    private void updateUrl(BagModel model) {
        vrfyPatternLbl.setText("");
        vrfyFetchTxt.setText("");

        if (model != null && !Strings.isEmpty(model.getUrlPattern())) {

            UrlFormatter fmt = new UrlFormatter(model.getChronPackage(),
                    model.getUrlPattern());
            String firstFile = model.getChronPackage().findRelativeFirstFile();
            vrfyPatternLbl.setText(model.getUrlPattern());
            if (firstFile != null) {
                vrfyFetchTxt.setText(fmt.format(firstFile) + "  "
                        + model.getChronPackage().findFirstFile().length()
                        + "  data/" + firstFile);
            }
        }
    }

    private void updateIngestionType(BagModel model) {
        if (model == null || model.getIngestionType() != null) {
            vrfyDestLbl.setText(model.getIngestionType().getDescription());
        } else {
            vrfyDestLbl.setText("None Selected");
        }
    }

    private void updateLocation(BagModel model) {
        vrfyLocationLbl.setText("");
        if (model != null) {
            if (model.getIngestionType() == IngestionType.LOCAL
                    && model.getSaveFile() != null) {
                vrfyLocationLbl.setText(model.getSaveFile().getPath());
            } else if (model.getIngestionType() == IngestionType.CHRONOPOLIS
                    && model.getChronopolisBag() != null) {
                vrfyLocationLbl.setText(model.getChronopolisBag());
            }
        }
    }

    private void updateMetadata(BagModel model) {
        metadataTableModel.clear();
        for (String key : model.getChronPackage().getMetadataMap()) {
            String value = model.getChronPackage().getMetadataMap().get(key);
            metadataTableModel.add(new MetadataPair(key, value));
        }

        metadataTableModel.add(new MetadataPair(BagWriter.INFO_BAGGING_DATE,
                "Calculated on transfer"));
        metadataTableModel.add(new MetadataPair(BagWriter.INFO_PAYLOAD_OXUM,
                "Calculated on transfer"));
        metadataTableModel.add(new MetadataPair(BagWriter.INFO_BAG_SIZE,
                "Calculated on transfer"));
    }

    private class MyListener implements BagModelListener {

        public void chronPackageChanged(BagModel model, ChronPackage oldpackage) {
            //TODO: Should we deep-bind listeners to chron package items
            updateMetadata(model);
            updateDirectoryList(model);
        }

        public void ingestionTypeChanged(BagModel model, IngestionType oldType) {
            updateIngestionType(model);
            updateLocation(model);
        }

        public void bagTypeChanged(BagModel model, BagType oldType) {
            updateBagType(model);
        }

        public void urlPatternChanged(BagModel model, String oldPattern) {
            updateUrl(model);
        }

        public void saveFileChanged(BagModel model, File oldFile) {
            updateLocation(model);
        }

        public void chronopoligBagChanged(BagModel model, String oldbagname) {
            updateLocation(model);
        }
    }
}
