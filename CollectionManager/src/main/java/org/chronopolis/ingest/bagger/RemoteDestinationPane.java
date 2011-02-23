/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import edu.umiacs.ace.json.Strings;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;

/**
 *
 * @author toaster
 */
public class RemoteDestinationPane extends BasePanel {

    @BXML
    private TextInput transferTxt;
    private TextInputContentListener txtListener = new TextInputContentListener.Adapter() {

        @Override
        public void textChanged(TextInput textInput) {
            if (textInput.getText() != null && !textInput.getText().equals(getBagModel().getChronopolisBag()));
            getBagModel().setChronopolisBag(textInput.getText());
        }
    };
    private BagModelListener bagListener = new BagModelListener.Adaptor() {

        @Override
        public void chronopoligBagChanged(BagModel mode, String oldbagname) {
            if (Strings.isEmpty(mode.getChronopolisBag())) {
                setErrorMessage("Enter Name for Chronopolis bag");

                
            }
            updateFromModel();
        }
    };

    public RemoteDestinationPane() {
        super("remoteDestinationPane.bxml");
        setTitle("2. Choose Chronopolis Collection");
        transferTxt.getTextInputContentListeners().add(txtListener);
    }

    private void updateNext() {
        BagModel model = getBagModel();
        if (Strings.isEmpty(model.getChronopolisBag())) {
            setErrorMessage("Please enter a deposit name for this bag");
        } else {
            setErrorMessage("");
        }
    }

    @Override
    protected void modelChanged(BagModel old) {
        if (old != null) {
            old.getModelListenerList().remove(bagListener);
        }
        if (getBagModel() != null) {
            BagModel model = getBagModel();
            model.getModelListenerList().add(bagListener);
        }
        updateFromModel();
    }

    @Override
    public Vote isComplete() {
        if (Strings.isEmpty(getBagModel().getChronopolisBag())) {
            return Vote.DENY;
        } else {
            return Vote.APPROVE;
        }
    }

    private void updateFromModel() {
        updateNext();
        BagModel model = getBagModel();
        if (model == null) {
            transferTxt.setText("");
        } else if (model.getChronopolisBag() != null) {
            transferTxt.setText(model.getChronopolisBag());
        } else if (model.getChronPackage() != null && model.getChronPackage().getName() != null) {
            transferTxt.setText(model.getChronPackage().getName());
        } else {
            transferTxt.setText("");
        }
    }
}
