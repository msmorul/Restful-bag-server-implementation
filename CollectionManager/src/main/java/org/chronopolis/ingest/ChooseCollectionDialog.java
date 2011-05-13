/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.content.ButtonDataRenderer;

/**
 *
 * @author toaster
 */
public class ChooseCollectionDialog extends Dialog {

    @BXML
    private ListButton collectionListButton;
    @BXML
    private PushButton okBtn;
    @BXML
    private PushButton cancelBtn;

    public ChooseCollectionDialog() {
        try {

            BXMLSerializer serializer = new BXMLSerializer();
            Component mainW = (Component) serializer.readObject(ChooseCollectionDialog.class, "chooseCollectionDialog.bxml");
            serializer.bind(this);
            setContent(mainW);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        collectionListButton.setItemRenderer(new CollectionBeanRenderer());
        collectionListButton.setDataRenderer(new CollectionButtonRenderer());
        setTitle("Choose Collection");
        setPreferredSize(350, 100);
    }

    public ListenerList<ButtonPressListener> getAcceptButtonPressListeners()
    {
        return okBtn.getButtonPressListeners();
    }

    public ListenerList<ButtonPressListener> getCancelButtonPressListeners()
    {
        return cancelBtn.getButtonPressListeners();
    }

    public void setCancelButtonData(ButtonData data) {
        cancelBtn.setButtonData(data);
    }

    public void setAcceptButtonData(ButtonData data) {
        okBtn.setButtonData(data);
    }

//    public void setListData(List<CollectionBean> collectionList) {
//        collectionListButton.setListData(collectionList);
//        if (collectionList.getLength() > 0) {
//            collectionListButton.setSelectedIndex(0);
//        }
//    }

    private class CollectionButtonRenderer extends ButtonDataRenderer {

        @Override
        public void render(Object data, Button button, boolean highlighted) {
//            if (data instanceof CollectionBean) {
//                super.render(((CollectionBean) data).getName(), button, highlighted);
//            } else {
                super.render(data, button, highlighted);
//            }
        }
    }
}
