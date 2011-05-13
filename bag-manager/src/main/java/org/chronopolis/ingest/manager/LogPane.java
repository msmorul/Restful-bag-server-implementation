/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

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
                        printLog(message);
                    }
                });
            }
        });

        MessageBus.subscribe(JettyRequest.class, new MessageBusListener<JettyRequest>() {

            public void messageSent(final JettyRequest message) {
                ApplicationContext.queueCallback(new Runnable() {

                    @Override
                    public void run() {
                        printLog(message);
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

    private void printLog(LogMessage message) {
        String text = message.getLogLayout().format(message.getEvent());
        logTxt.getDocument().add(new Paragraph(text));
        if (message.getEvent().getThrowableInformation() != null) {
            StringBuilder sb = new StringBuilder();
            for (String s : message.getEvent().getThrowableInformation().getThrowableStrRep()) {
                sb.append("  ");
                sb.append(s);
                sb.append("\n");
            }
            logTxt.getDocument().add(new Paragraph(sb.toString()));
        }
    }

    private void printLog(JettyRequest message) {

        StringBuilder buf = new StringBuilder(160);
        Request request = message.getRequest();
        Response response = message.getResponse();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss ZZZ");


//        buf.append(request.getServerName());
//        buf.append(' ');


        String addr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);


        if (addr == null) {
            addr = request.getRemoteAddr();
        }

        buf.append(addr);
        buf.append(" - ");
        String user = request.getRemoteUser();
        buf.append((user == null) ? " - " : user);
        buf.append(" [");
        buf.append(dateFormat.format(request.getTimeStamp()));
        buf.append("] \"");
        buf.append(request.getMethod());
        buf.append(' ');
        buf.append(request.getUri());
        buf.append(' ');
        buf.append(request.getProtocol());
        buf.append("\" ");
        int status = response.getStatus();
        if (status <= 0) {
            status = 404;
        }
        buf.append((char) ('0' + ((status / 100) % 10)));
        buf.append((char) ('0' + ((status / 10) % 10)));
        buf.append((char) ('0' + (status % 10)));


        long responseLength = response.getContentCount();
        if (responseLength >= 0) {
            buf.append(' ');
            if (responseLength > 99999) {
                buf.append(Long.toString(responseLength));
            } else {
                if (responseLength > 9999) {
                    buf.append((char) ('0' + ((responseLength / 10000) % 10)));
                }
                if (responseLength > 999) {
                    buf.append((char) ('0' + ((responseLength / 1000) % 10)));
                }
                if (responseLength > 99) {
                    buf.append((char) ('0' + ((responseLength / 100) % 10)));
                }
                if (responseLength > 9) {
                    buf.append((char) ('0' + ((responseLength / 10) % 10)));
                }
                buf.append((char) ('0' + (responseLength) % 10));
            }
            buf.append(' ');
        } else {
            buf.append(" - ");
        }

//            String log =buf.toString();
        logTxt.getDocument().add(new Paragraph(buf.toString()));

//
//        String text = message.getLogLayout().format(message.getEvent());
//        logTxt.getDocument().add(new Paragraph(text));
//        if (message.getEvent().getThrowableInformation() != null) {
//            StringBuffer sb = new StringBuilder();
//            for (String s : message.getEvent().getThrowableInformation().getThrowableStrRep()) {
//                sb.append("  ");
//                sb.append(s);
//                sb.append("\n");
//            }
//            logTxt.getDocument().add(new Paragraph(sb.toString()));
//        }
    }
}
