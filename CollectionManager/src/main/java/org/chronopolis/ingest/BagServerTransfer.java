/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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


        BagServerBuildListener writer = new BagServerBuildListener(URI.create("http://localhost:7878/bags"), isHoley,model.getChronopolisBag());
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
                    LOG.error(ioe);
                }
            }
        });
        thread.start();
    }
}
