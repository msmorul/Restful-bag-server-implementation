/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import edu.umiacs.ace.json.Strings;
import java.io.File;
import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.content.ButtonData;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;
import org.chronopolis.ingest.pkg.BagWriter;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.UrlFormatter;

/**
 *
 * @author toaster
 */
public class VerifyPane extends Border {

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
    private PushButton okBtn;
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
    private BagModel model;
    private BagModelListener listener = new MyListener();
    private List<MetadataPair> metadataTableModel = new ArrayList<MetadataPair>();

    public VerifyPane() {

        try {

            BXMLSerializer serializer = new BXMLSerializer();
            Component pkgPane = (Component) serializer.readObject(
                    VerifyPane.class, "verifyPane.bxml");
            serializer.bind(this);
            setContent(pkgPane);
            metadataTable.setTableData(metadataTableModel);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setModel(BagModel model) {
        if (this.model != null) {
            this.model.getModelListenerList().remove(listener);
        }
        this.model = model;
        if (this.model != null) {
            this.model.getModelListenerList().add(listener);
        }
        updateBagType(model);
        updateUrl(model);
    }

    public ListenerList<ButtonPressListener> getAcceptButtonPressListeners() {
        return okBtn.getButtonPressListeners();
    }

    public void setAcceptButtonData(ButtonData data) {
        okBtn.setButtonData(data);
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
        if (model == null || Strings.isEmpty(model.getUrlPattern())) {
            vrfyPatternLbl.setText("");
            vrfyFetchTxt.setText("");
        } else {
            UrlFormatter fmt = new UrlFormatter(model.getChronPackage(),
                    model.getUrlPattern());
            String firstFile = model.getChronPackage().findRelativeFirstFile();
            vrfyPatternLbl.setText(model.getUrlPattern());
            vrfyFetchTxt.setText(fmt.format(firstFile) + "  "
                    + model.getChronPackage().findFirstFile().length()
                    + "  data/" + firstFile);
        }
    }

    private void updateIngestionType(BagModel model) {
        if (model == null || model.getIngestionType() != null) {
            vrfyDestLbl.setText(model.getIngestionType().getDescription());
        } else {
            vrfyDestLbl.setText("None Selected");
        }
    }
    private MapListener metadatalistener = new MapListener.Adapter<String,String>() {

        @Override
        public void mapCleared(Map map) {
            metadataTableModel.clear();
            metadataTableModel.add(new MetadataPair(BagWriter.INFO_BAGGING_DATE,
                    "Calculated on transfer"));
            metadataTableModel.add(new MetadataPair(BagWriter.INFO_PAYLOAD_OXUM,
                    "Calculated on transfer"));
            metadataTableModel.add(new MetadataPair(BagWriter.INFO_BAG_SIZE,
                    "Calculated on transfer"));
        }

        @Override
        public void valueAdded(Map<String,String> map, String key) {
            metadataTableModel.add(new MetadataPair(key, map.get(key)));
        }

        @Override
        public void valueRemoved(Map map, String key, String value) {
            metadataTableModel.remove(new MetadataPair(key, value));
        }

        @Override
        public void valueUpdated(Map<String,String> map, String key,
                String previousValue) {
            MetadataPair mp = new MetadataPair(key, previousValue);
            int location = metadataTableModel.indexOf(mp);
            MetadataPair regPair = metadataTableModel.get(location);
            regPair.setValue(map.get(key));
        }
    };

    private class MyListener implements BagModelListener {

        public void chronPackageChanged(BagModel model, ChronPackage oldpackage) {
            if (oldpackage != null) {
                oldpackage.getMetadataMap().getMapListeners().remove(metadatalistener);
            }
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

            model.getChronPackage().getMetadataMap().getMapListeners().add(metadatalistener);
        }

        public void ingestionTypeChanged(BagModel model, IngestionType oldType) {
            updateIngestionType(model);
        }

        public void bagTypeChanged(BagModel model, BagType oldType) {
            updateBagType(model);
        }

        public void urlPatternChanged(BagModel model, String oldPattern) {
            updateUrl(model);
        }

        public void saveFileChanged(BagModel model, File oldFile) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void chronopoligBagChanged(BagModel mode, String oldbagname) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
