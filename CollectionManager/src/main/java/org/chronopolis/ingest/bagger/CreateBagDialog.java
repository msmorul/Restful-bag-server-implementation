/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.bagger;

import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.Dialog;

/**
 *
 * TODO: add listener for bagmodel changes
 * @author toaster
 */
public class CreateBagDialog extends Dialog {

    private BagModel bagModel;
    private Accordion panelAccordion;
    private VerifyPane verifyPane = new VerifyPane();
    private ChooseBagPane choosePane = new ChooseBagPane();

    public CreateBagDialog() {

        panelAccordion = new Accordion();

        panelAccordion.add(choosePane);
        panelAccordion.add(verifyPane);

        setContent(panelAccordion);
    }

    public void setBagModel(BagModel bagModel) {
        this.bagModel = bagModel;
        verifyPane.setModel(bagModel);
    }

    public BagModel getBagModel() {
        return bagModel;
    }
}
