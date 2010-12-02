/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import edu.umiacs.ace.json.StatusBean.CollectionBean;
import java.io.IOException;
import org.apache.pivot.collections.List;
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
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 *
 * @author toaster
 */
public class ChooseCollectionDialog extends Dialog {

    @WTKX
    private ListButton collectionListButton;
    @WTKX
    private PushButton okBtn;
    @WTKX
    private PushButton cancelBtn;

    public ChooseCollectionDialog() {
        try {

            WTKXSerializer serializer = new WTKXSerializer();
            Component mainW = (Component) serializer.readObject(this, "chooseCollectionDialog.wtkx");
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

    public void setListData(List<CollectionBean> collectionList) {
        collectionListButton.setListData(collectionList);
        if (collectionList.getLength() > 0) {
            collectionListButton.setSelectedIndex(0);
        }
    }

    private class CollectionButtonRenderer extends ButtonDataRenderer {

        @Override
        public void render(Object data, Button button, boolean highlighted) {
            if (data instanceof CollectionBean) {
                super.render(((CollectionBean) data).getName(), button, highlighted);
            } else {
                super.render(data, button, highlighted);
            }
        }
    }
}
