/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import java.io.File;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;

/**
 *
 * @author toaster
 */
public class ChooseLocalBag extends BasePanel {

    @BXML
    private FileBrowser browser;
    @BXML
    private TextInput bagfileTxt;
    @BXML
    private Label saveFileLbl;
    private FileBrowserListener browserLabelUpdateListener = new FileBrowserListener.Adapter() {

        @Override
        public void rootDirectoryChanged(FileBrowser fb, File file) {
            File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
            getBagModel().setSaveFile(bagFile);
        }
    };
    private BagModelListener bagListener = new BagModelListener.Adaptor() {

        @Override
        public void saveFileChanged(BagModel model, File oldFile) {
            updateFromModel();
        }
    };
    private TextInputContentListener bagNameListener = new TextInputContentListener.Adapter() {

        @Override
        public void textChanged(TextInput textInput) {
            File bagFile = new File(browser.getRootDirectory(), textInput.getText());
            getBagModel().setSaveFile(bagFile);
        }
    };

    private void updateFromModel() {
        BagModel model = getBagModel();
        if (model != null && model.getSaveFile() != null) {
            saveFileLbl.setText(model.getSaveFile().getPath());
            if (!browser.getRootDirectory().equals(model.getSaveFile().getParentFile())) {
                browser.setRootDirectory(model.getSaveFile().getParentFile());
            }
            if (!bagfileTxt.getText().equals(model.getSaveFile().getName())) {
                bagfileTxt.setText(model.getSaveFile().getName());
            }
        }
        updateNext();
    }

    @Override
    protected void modelChanged(BagModel old) {
        if (old != null) {
            old.getModelListenerList().remove(bagListener);
        }
        if (getBagModel() != null) {
            BagModel model = getBagModel();
            model.getModelListenerList().add(bagListener);
            if (model.getSaveFile() == null) {
                model.setSaveFile(new File(browser.getRootDirectory(),
                        model.getChronPackage().getName() + ".tgz"));

            }
        }
        updateFromModel();
    }

    public ChooseLocalBag() {
        super("chooseLocalBag.bxml");
        Accordion.setHeaderData(this, "2. Choose Bag Location");
        browser.getFileBrowserListeners().add(browserLabelUpdateListener);
        bagfileTxt.getTextInputContentListeners().add(bagNameListener);

    }

    private void updateNext() {
        if (getBagModel() == null || getBagModel().getSaveFile() == null
                || !getBagModel().getSaveFile().getParentFile().canWrite()) {
            setErrorMessage("Cannot write to selected file " + getBagModel().getSaveFile().getParentFile().canWrite());
        } else {
            setErrorMessage("");
            
        }
    }

    public Vote isComplete() {
        if (getBagModel() != null && getBagModel().getSaveFile() != null
                && getBagModel().getSaveFile().getParentFile().canWrite()) {
            return Vote.APPROVE;
        } else {
            return Vote.DENY;
        }
    }
}
