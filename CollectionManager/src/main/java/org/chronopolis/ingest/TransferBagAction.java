/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPOutputStream;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.chronopolis.ingest.bagger.BagModel;
import org.chronopolis.ingest.messages.TransferBagMessage;
import org.chronopolis.ingest.pkg.BagBuildListener;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackage.Statistics;
import org.chronopolis.ingest.pkg.DelayedTransferStream;
import org.chronopolis.ingest.pkg.ManifestBuilder;
import org.apache.log4j.Logger;

/**
 *
 * @author toaster
 */
public final class TransferBagAction implements MessageBusListener<TransferBagMessage> {

    private static final Logger LOG = Logger.getLogger(TransferBagAction.class);

    public void messageSent(final TransferBagMessage message) {
        final BagModel model = message.getTransferModel();
        final ChronPackage workingPackage = message.getTransferModel().getChronPackage();
        Statistics stats = model.getBagStats();

        final ManifestBuilder builder = new ManifestBuilder(workingPackage, stats.getSize());
        final BuildProgressDialog dialog = new BuildProgressDialog();

        final OutputStream os;
        final HttpURLConnection connection;

        if (model.getIngestionType() == BagModel.IngestionType.LOCAL) {
            File f = model.getSaveFile();

            try {
                os = new GZIPOutputStream(new FileOutputStream(f));
                connection = null;
            } catch (IOException ioe) {
                LOG.error("Ioexception opening local bag",ioe);
                Alert.alert(MessageType.ERROR, "Error opening bag file", message.getParentWindow());
                return;
            }
        } else {
            //Open Chron input stream
            try {
                URL newURL = new URL(Main.getURL() + "/" + model.getChronopolisBag());
                if (model.getBagType() == BagModel.BagType.HOLEY) {
                    connection = null;
                    os = new DelayedTransferStream(newURL);
                } else {
                    connection = (HttpURLConnection) newURL.openConnection();
                    connection.setChunkedStreamingMode(32768);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("PUT");
                    LOG.trace("Opening url: " + newURL);
                    os = new GZIPOutputStream(connection.getOutputStream());
                }

            } catch (IOException ioe) {
                LOG.error("Error opening chron connection",ioe);
                Alert.alert(MessageType.ERROR, "Error opening chronopolis connection: ("
                        + ioe.getClass().getName() + ") " + ioe.getMessage(), message.getParentWindow());
                return;
            }
        }

        boolean isHoley = (model.getBagType() == BagModel.BagType.HOLEY);
        final boolean isLocal = (model.getIngestionType() == BagModel.IngestionType.LOCAL);
        BagBuildListener writer = new BagBuildListener(workingPackage, os, isHoley);
        writer.setCloseOutput(true);
        if (isHoley) {
            writer.setUrlPattern(model.getUrlPattern());
        }

        builder.getBuildListeners().add(writer);

        dialog.setBuilder(builder);
        dialog.open(message.getParentWindow(), new SheetCloseListener() {

            public void sheetClosed(Sheet sheet) {
                builder.cancel();
                if (os instanceof DelayedTransferStream) {
                    DelayedTransferStream dts = (DelayedTransferStream) os;
                    if (dts.getResponseCode() < 200 || dts.getResponseCode() > 299) {
                        Alert.alert(MessageType.ERROR, "Transfer error HTTP/" + dts.getResponseCode() + " " + dts.getResponseMessage(), message.getParentWindow());
                        return;
                    }
                } else if (connection != null) {
                    try {
                        if (connection.getResponseCode() < 200 || connection.getResponseCode() > 299) {
                            Alert.alert(MessageType.ERROR, "Transfer error HTTP/" + connection.getResponseCode() + " " + connection.getResponseMessage(), message.getParentWindow());
                            return;
                        }
                    } catch (IOException ioe) {
                        LOG.error("unexpected error",ioe);
                    }
                    connection.disconnect();

                }

                if (sheet.getResult()) {
                    if (isLocal) {
                        Alert.alert(MessageType.INFO, "New Bag Created: " + model.getSaveFile().getPath(), message.getParentWindow());
                    } else {
                        Alert.alert(MessageType.INFO, "Chronopolis Transfer Successful", message.getParentWindow());
                    }
                    if (!isLocal) {
                        workingPackage.setReadOnly(true);
                    }
                } else {
                    Alert.alert(MessageType.ERROR, "Aborted!", message.getParentWindow());

                }
            }
        });
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    builder.scanPackage();
                } catch (Exception ioe) {
                    dialog.close();
                    ioe.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
