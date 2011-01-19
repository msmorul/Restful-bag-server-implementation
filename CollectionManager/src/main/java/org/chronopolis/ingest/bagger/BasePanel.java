/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.PushButton;
import org.apache.log4j.Logger;
import org.apache.pivot.wtk.Label;

/**
 *
 * @author toaster
 */
public abstract class BasePanel extends Border {

    private static final Logger LOG = Logger.getLogger(BasePanel.class);
    @BXML
    protected PushButton nextBtn;
    @BXML
    protected PushButton previousBtn;
    @BXML
    private Label errorMessage;
    private BagModel bagModel;

    public BasePanel(String bxmlFile) {
        try {
            BXMLSerializer serializer = new BXMLSerializer();
            Component pkgPane = (Component) serializer.readObject(
                    BasePanel.class, bxmlFile);
            serializer.bind(this);

            nextBtn = (PushButton) serializer.getNamespace().get("nextBtn");
            previousBtn = (PushButton) serializer.getNamespace().get("previousBtn");
            errorMessage = (Label) serializer.getNamespace().get("errorMessage");
            setContent(pkgPane);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final BagModel getBagModel() {
        return bagModel;
    }

    public final void setBagModel(BagModel bagModel) {
        BagModel old = this.bagModel;
        this.bagModel = bagModel;
        modelChanged(old);
    }

    protected final void setErrorMessage(String message) {
        if (message == null) {
            message = "";
        }
        if (nextBtn != null) {
            if ("".equals(message)) {

                nextBtn.setEnabled(true);

            } else {
                nextBtn.setEnabled(false);
            }
        }

        if (errorMessage != null) {
            errorMessage.setText(message);
        }
    }

    /**
     * Overriding classes can use this to receive notification when a bag changes
     * getBagModel will return the new version.
     *
     * @param old previous bagmodel value
     */
    protected void modelChanged(BagModel old) {
    }

    public abstract Vote isComplete();

    public final boolean hasPreviousButton() {
        return previousBtn != null;
    }

    public final boolean hasNextButton() {
        return nextBtn != null;
    }

    public final ListenerList<ButtonPressListener> getNextButtonPressListeners() {

        if (nextBtn == null) {
            return null;
        }
        return nextBtn.getButtonPressListeners();
    }

    public final ListenerList<ButtonPressListener> getPreviousButtonPressListeners() {
        if (previousBtn == null) {
            return null;
        }
        return previousBtn.getButtonPressListeners();
    }
}
