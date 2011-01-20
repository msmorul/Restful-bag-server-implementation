/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
//         Pretty picture in the about box ;) Better than pemulis' twitter feed
        if (System.currentTimeMillis() % 2 == 1) {
            img.setImage(Main.class.getResource("trash.jpg"));
        } else {
            img.setImage(Main.class.getResource("barge.jpg"));
        }
    }
}
