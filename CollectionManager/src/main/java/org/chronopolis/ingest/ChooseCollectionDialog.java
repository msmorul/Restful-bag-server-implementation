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

        collectionListButton.setItemRenderer(new BagBeanRenderer());
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
