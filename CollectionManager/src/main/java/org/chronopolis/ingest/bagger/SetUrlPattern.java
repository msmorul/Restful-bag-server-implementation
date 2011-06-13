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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.chronopolis.ingest.Main;
import org.chronopolis.ingest.pkg.UrlFormatter;

/**
 *
 * @author toaster
 */
public class SetUrlPattern extends BasePanel {

    @BXML
    private TextInput urlTxt;
    @BXML
    private TextInput sampleUrlLbl;
    @BXML
    private PushButton testUrlBtn;
    @BXML
    private PushButton resetUrlBtn;
    private BagModelListener bagListener = new BagModelListener.Adaptor() {

        @Override
        public void urlPatternChanged(BagModel model, String oldPattern) {
            if (!urlTxt.getText().equals(model.getUrlPattern())) {
                urlTxt.setText(model.getUrlPattern());
            }
            UrlFormatter uf = new UrlFormatter(model.getChronPackage(), model.getUrlPattern());
            String sf = model.getChronPackage().findRelativeFirstFile();
            if (sf != null) {
                sampleUrlLbl.setText(uf.format(sf));
            }
            updateNext();
        }

        @Override
        public void chronopoligBagChanged(BagModel mode, String oldbagname) {
            updateFromModel();
        }
    };
    private ButtonPressListener resetUrlButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            getBagModel().setUrlPattern(Main.getDefaultURLPattern());
        }
    };
    private TextInputContentListener urlTxtListener = new TextInputContentListener.Adapter() {

        @Override
        public void textChanged(TextInput textInput) {
            BagModel model = getBagModel();
            model.setUrlPattern(textInput.getText());
            updateNext();
        }
    };
    private ButtonPressListener testUrlButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            String url = sampleUrlLbl.getText();
            if (!url.startsWith("http")) {
                Alert.alert("Only http(s) URL's can be tested", button.getWindow());
                return;
            }
            try {
                URL u = new URL(url);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setConnectTimeout(2000);
                try {
                    if (c.getResponseCode() < 200 || c.getResponseCode() > 299) {
                        Alert.alert(MessageType.ERROR, "Transfer error HTTP/"
                                + c.getResponseCode() + " " + c.getResponseMessage(), button.getWindow());
                        c.disconnect();
                        return;
                    }
                } catch (IOException ioe) {
                    Alert.alert(MessageType.ERROR, "Cannot retrieve url (" + ioe.getClass().getSimpleName() + ") " + ioe.getMessage(), button.getWindow());
                    return;
                }
                c.disconnect();

                Alert.alert(MessageType.INFO, "Data URL is good!", button.getWindow());
            } catch (IOException e) {
                Alert.alert(MessageType.ERROR, "Cannot retrieve url (" + e.getClass().getSimpleName() + ") " + e.getMessage(), button.getWindow());
                return;
            } finally {
                updateNext();
            }
        }
    };

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        updateFromModel();
    }

    public SetUrlPattern() {
        super("setUrlPattern.bxml");
        Accordion.setHeaderData(this, "3. Set URL Pattern");
        testUrlBtn.getButtonPressListeners().add(testUrlButtonListener);
        urlTxt.getTextInputContentListeners().add(urlTxtListener);
        resetUrlBtn.getButtonPressListeners().add(resetUrlButtonListener);
    }

    public Vote isComplete() {
        if (getBagModel().getUrlPattern() == null || getBagModel().getUrlPattern().isEmpty()) {
            return Vote.APPROVE;
        } else {
            return Vote.DENY;
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
            if (getBagModel().getUrlPattern() == null || getBagModel().getUrlPattern().isEmpty()) {
                model.setUrlPattern(Main.getDefaultURLPattern());
            }
        }
        updateFromModel();

    }

    private void updateNext() {
        if (getBagModel().getUrlPattern() == null || getBagModel().getUrlPattern().isEmpty()) {
            setErrorMessage("Please set and test URL pattern");
        } else {
            setErrorMessage("");
        }
    }

    private void updateFromModel() {
        BagModel model = getBagModel();
        if (model != null) {
            if (!urlTxt.getText().equals(model.getUrlPattern())) {
                urlTxt.setText(getBagModel().getUrlPattern());
            }
            UrlFormatter uf = new UrlFormatter(model.getChronPackage(), model.getUrlPattern());
            String sf = model.getChronPackage().findRelativeFirstFile();
            if (sf != null) {
                sampleUrlLbl.setText(uf.format(sf));
            }
        }

        updateNext();
    }
}
