/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.PushButton;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;

/**
 *
 * @author toaster
 */
public class ChooseBagPane extends BasePanel {

    private BagModelListener chooseListener = new ChooseListener();
    @BXML
    private PushButton chronopolisBtn;
    @BXML
    private PushButton localBtn;
    @BXML
    private PushButton holeyBtn;
    @BXML
    private PushButton completeBtn;
    private ButtonPressListener localBtnListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            getBagModel().setIngestionType(IngestionType.LOCAL);
        }
    };
    private ButtonPressListener chronopolisBtnListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            getBagModel().setIngestionType(IngestionType.CHRONOPOLIS);
        }
    };
    private ButtonPressListener holeyBtnListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            getBagModel().setBagType(BagType.HOLEY);
        }
    };
    private ButtonPressListener completeBtnListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            getBagModel().setBagType(BagType.FILLED);
        }
    };

    public ChooseBagPane() {
        super("chooseBagPane.bxml");

        Accordion.setHeaderData(this, "1. Choose Bag Type");
        chronopolisBtn.getButtonPressListeners().add(chronopolisBtnListener);
        localBtn.getButtonPressListeners().add(localBtnListener);
        holeyBtn.getButtonPressListeners().add(holeyBtnListener);
        completeBtn.getButtonPressListeners().add(completeBtnListener);
    }

    private void updateNext() {
        BagModel model = getBagModel();
        if (model.getBagType() == null) {
            setErrorMessage("Please select bag type");
        } else if (model.getIngestionType() == null) {
            setErrorMessage("Please select filled or holey bag");
        } else {
            setErrorMessage("");
        }
    }

    @Override
    protected void modelChanged(BagModel old) {
        if (old != null) {
            old.getModelListenerList().remove(chooseListener);
        }
        if (getBagModel() != null) {
            BagModel model = getBagModel();
            model.getModelListenerList().add(chooseListener);
            // reset components to model
            chronopolisBtn.setSelected(model.getIngestionType() == IngestionType.CHRONOPOLIS);
            localBtn.setSelected(model.getIngestionType() == IngestionType.LOCAL);
            holeyBtn.setSelected(model.getBagType() == BagType.HOLEY);
            completeBtn.setSelected(model.getBagType() == BagType.FILLED);
            updateNext();
        }
    }

    public Vote isComplete() {
        if (getBagModel() == null) {
            return Vote.DENY;
        }

        if (getBagModel().getBagType() != null && getBagModel().getIngestionType() != null) {
            return Vote.APPROVE;
        } else {
            return Vote.DENY;
        }
    }

    private class ChooseListener extends BagModelListener.Adaptor {

        @Override
        public void ingestionTypeChanged(BagModel model, IngestionType oldType) {
            chronopolisBtn.setSelected(model.getIngestionType() == IngestionType.CHRONOPOLIS);
            localBtn.setSelected(model.getIngestionType() == IngestionType.LOCAL);
            updateNext();
        }

        @Override
        public void bagTypeChanged(BagModel model, BagType oldType) {
            holeyBtn.setSelected(model.getBagType() == BagType.HOLEY);
            completeBtn.setSelected(model.getBagType() == BagType.FILLED);
            updateNext();
        }
    }
}
