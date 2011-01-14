/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.bagger;

import java.io.IOException;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;

/**
 *
 * @author toaster
 */
public class ChooseBagPane extends Border{

    public ChooseBagPane() {
         try {

            BXMLSerializer serializer = new BXMLSerializer();
            Component pkgPane = (Component) serializer.readObject(
                    VerifyPane.class, "chooseBagPane.bxml");
            serializer.bind(this);
            setContent(pkgPane);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
