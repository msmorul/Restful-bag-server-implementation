/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
