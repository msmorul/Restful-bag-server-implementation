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

import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Accordion;
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
            if (mode.getChronopolisBag() == null || mode.getChronopolisBag().isEmpty()) {
                setErrorMessage("Enter Name of Bag on server");
                
            }
            updateFromModel();
        }
    };

    public RemoteDestinationPane() {
        super("remoteDestinationPane.bxml");
        Accordion.setHeaderData(this, "2. Choose Target Name");
        transferTxt.getTextInputContentListeners().add(txtListener);
    }

    private void updateNext() {
        BagModel model = getBagModel();
        if (model.getChronopolisBag() == null || model.getChronopolisBag().isEmpty()) {
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
        if (getBagModel().getChronopolisBag() == null || getBagModel().getChronopolisBag().isEmpty()) {
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
