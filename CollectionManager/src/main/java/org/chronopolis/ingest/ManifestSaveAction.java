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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.chronopolis.ingest.messages.SaveBagMessage;
import org.chronopolis.ingest.pkg.ManifestBuilder;
import org.chronopolis.ingest.pkg.ManifestFileWriter;

/**
 *
 * @author toaster
 */
public class ManifestSaveAction implements MessageBusListener<SaveBagMessage> {


    public ManifestSaveAction() {
    }

    public void messageSent(final SaveBagMessage message) {
       final ManifestBuilder builder = new ManifestBuilder(message.getPkg(), 0);
       
        BuildProgressDialog dialog = new BuildProgressDialog();

        File f = message.getSaveFile();
        final OutputStream os;
        try {
            os = new FileOutputStream(f);
            ManifestFileWriter writer = new ManifestFileWriter(os);
            builder.getBuildListeners().add(writer);
        } catch (IOException ioe) {
            Alert.alert(MessageType.ERROR, "Error opening manifest file", 
                    message.getDisplayWindow());
            return;
        }

        dialog.setBuilder(builder);
        dialog.open(message.getDisplayWindow(), new SheetCloseListener() {

            public void sheetClosed(Sheet sheet) {
                builder.cancel();
                try {
                    os.close();
                } catch (IOException e) {
                }
                if (sheet.getResult()) {
                    Alert.alert(MessageType.INFO, "Transfer Successful",
                            message.getDisplayWindow());
                } else {
                    Alert.alert(MessageType.ERROR, "Transfer Aborted",
                            message.getDisplayWindow());
                }

            }
        });

        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    builder.scanPackage();
                } catch (Exception ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
