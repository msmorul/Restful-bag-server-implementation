/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.messages;

import org.apache.pivot.wtk.Window;
import org.chronopolis.ingest.bagger.BagModel;

/**
 *
 * @author toaster
 */
public class TransferBagMessage
{

    private BagModel transferModel;
    private Window parentWindow;

    public TransferBagMessage(BagModel transferModel, Window parentWindow) {
        this.transferModel = transferModel;
        this.parentWindow = parentWindow;
    }

    public BagModel getTransferModel() {
        return transferModel;
    }

    public Window getParentWindow() {
        return parentWindow;
    }
    

}
