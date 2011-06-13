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
