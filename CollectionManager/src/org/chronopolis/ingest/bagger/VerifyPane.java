/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import edu.umiacs.ace.json.Strings;
import java.io.File;
import java.io.IOException;
import java.sql.BatchUpdateException;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;
import org.chronopolis.ingest.pkg.ChronPackage;

/**
 *
 * @author toaster
 */
public class VerifyPane extends Border {

    @WTKX
    private Label vrfyDestLbl;
    @WTKX
    private Label vrfyTypeLbl;
    @WTKX
    private Label vrfyPatternLbl;
    @WTKX
    private Label vrfyLocationLbl;
    @WTKX
    private Label vrfyPatternHdr;
    @WTKX
    private Label vrfyFilesLbl;
    @WTKX
    private Label vrfyDirectoriesLbl;
    @WTKX
    private Label vrfySizeLbl;
    @WTKX
    private Label vrfyUnreadableLbl;
    @WTKX
    private ListView vrfyUnreadableList;
    @WTKX
    private Border vrfyUnreadablePane;
    @WTKX
    private PushButton okBtn;
    @WTKX
    private TableView metadataTable;
    @WTKX
    private TextInput vrfyFetchTxt;
    @WTKX
    private Label vrfyFetchLbl;
    @WTKX
    private TextInput vrfyManifestTxt;
    @WTKX
    private TableView vrfyDirectoryTable;
    private BagModel model;
    private BagModelListener listener = new MyListener();

    public VerifyPane() {

        try {

            WTKXSerializer serializer = new WTKXSerializer();
            Component pkgPane = (Component) serializer.readObject(this, "verifyPane.wtkx");

            serializer.bind(this);
            setContent(pkgPane);

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

    private void updateBagType(BagModel model) {
        if (model == null || model.getBagType() == null) {
            vrfyTypeLbl.setText("None Selected");
        } else if (model.getBagType() == BagType.HOLEY) {
            vrfyTypeLbl.setText("Holey Bag");
            vrfyPatternLbl.setVisible(true);
            vrfyPatternHdr.setVisible(true);
            vrfyFetchTxt.setVisible(true);
            vrfyFetchLbl.setVisible(true);

        } else if (model.getBagType() == BagType.FILLED)
        {
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
        } else {
            vrfyPatternLbl.setText(model.getUrlPattern());
        }
    }

    private void updateIngestionType(BagModel model) {
        if (model == null || model.getIngestionType() != null) {
            vrfyDestLbl.setText(model.getIngestionType().getDescription());
        } else {
            vrfyDestLbl.setText("None Selected");
        }
    }

    private class MyListener implements BagModelListener {

        public void chronPackageChanged(BagModel model, ChronPackage oldpackage) {
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
