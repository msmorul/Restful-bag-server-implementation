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

import java.net.URI;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.chronopolis.ingest.bagger.BagModel;
import org.chronopolis.ingest.messages.TransferBagMessage;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackage.Statistics;
import org.chronopolis.ingest.pkg.ManifestBuilder;
import org.apache.log4j.Logger;
import org.chronopolis.ingest.pkg.BagServerBuildListener;

/**
 *
 * @author toaster
 */
public final class BagServerTransfer implements MessageBusListener<TransferBagMessage> {

    private static final Logger LOG = Logger.getLogger(BagServerTransfer.class);

    public void messageSent(final TransferBagMessage message) {

        final BagModel model = message.getTransferModel();
        final boolean isLocal = (model.getIngestionType() == BagModel.IngestionType.LOCAL);
        if (isLocal) {
            return;
        }
        final ChronPackage workingPackage = message.getTransferModel().getChronPackage();
        Statistics stats = model.getBagStats();

        final ManifestBuilder builder = new ManifestBuilder(workingPackage, stats.getSize());
        final BuildProgressDialog dialog = new BuildProgressDialog();

        boolean isHoley = (model.getBagType() == BagModel.BagType.HOLEY);


        BagServerBuildListener writer = new BagServerBuildListener(URI.create("http://localhost:7878/bags"), isHoley, model.getChronopolisBag());
        if (isHoley) {
            writer.setUrlPattern(model.getUrlPattern());
        }

        builder.getBuildListeners().add(writer);

        dialog.setBuilder(builder);
        dialog.open(message.getParentWindow(), new SheetCloseListener() {

            public void sheetClosed(Sheet sheet) {
                builder.cancel();

                if (sheet.getResult()) {
                    Alert.alert(MessageType.INFO, "Chronopolis Transfer Successful", message.getParentWindow());
                    workingPackage.setReadOnly(true);
//                } else {
//                    Alert.alert(MessageType.ERROR, "Aborted! ", message.getParentWindow());
                }
            }
        });
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    builder.scanPackage();
                } catch (Exception ioe) {
                    dialog.close();
                    Alert.alert(MessageType.ERROR, "Aborted! " + ioe.getMessage(), message.getParentWindow());

                    LOG.error(ioe);
                }
            }
        });
        thread.start();
    }
}
