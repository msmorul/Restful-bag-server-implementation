/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import java.io.IOException;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextPane;
import org.apache.pivot.wtk.text.Document;
import org.apache.pivot.wtk.text.Paragraph;

/**
 *
 * @author toaster
 */
public class LogPane extends Border {

    @BXML
    private TextPane logTxt;
    @BXML
    private PushButton clearBtn;

    public LogPane() {
        Component c;
        BXMLSerializer serializer = new BXMLSerializer();
        try {
            c = (Component) serializer.readObject(LogPane.class, "logPane.bxml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        serializer.bind(this);
        logTxt.setDocument(new Document());

        MessageBus.subscribe(LogMessage.class, new MessageBusListener<LogMessage>() {

            public void messageSent(final LogMessage message) {
                ApplicationContext.queueCallback(new Runnable() {

                    @Override
                    public void run() {
                        String text = message.getLogLayout().format(message.getEvent());
                        logTxt.getDocument().add(new Paragraph(text));
                    }
                });
            }
        });

        clearBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                logTxt.setDocument(new Document());
            }
        });
        setContent(c);
    }
}
