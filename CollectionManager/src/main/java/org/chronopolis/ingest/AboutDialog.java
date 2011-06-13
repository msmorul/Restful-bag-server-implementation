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
import java.util.ResourceBundle;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;

/**
 *
 * @author toaster
 */
public class AboutDialog extends Dialog {

    @BXML
    private ImageView img;
    @BXML
    private Label buildLbl;
    @BXML
    private Label dateLbl;
    @BXML
    private Label url;

    public AboutDialog() {
        setPreferredWidth(350);
        setPreferredHeight(400);
        setTitle("About Chronopolis Data Manager");
        try {
            BXMLSerializer serializer = new BXMLSerializer();
            Component content = (Component) serializer.readObject(
                    AboutDialog.class, "aboutDialog.bxml");
            serializer.bind(this);
            setContent(content);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ResourceBundle bundle = java.util.ResourceBundle.getBundle("version");
        buildLbl.setText(bundle.getString("build.num"));
        dateLbl.setText(bundle.getString("build.date"));
        url.setText(Main.getGateway().getEndpoint().toString());
//         Pretty picture in the about box ;) Better than pemulis' twitter feed
        if (System.currentTimeMillis() % 2 == 1) {
            img.setImage(Main.class.getResource("trash.jpg"));
        } else {
            img.setImage(Main.class.getResource("barge.jpg"));
        }
    }
}
