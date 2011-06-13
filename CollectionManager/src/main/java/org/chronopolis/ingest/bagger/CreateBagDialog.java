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
package org.chronopolis.ingest.bagger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.chronopolis.ingest.bagger.BagModel.BagType;
import org.chronopolis.ingest.bagger.BagModel.IngestionType;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.apache.log4j.Logger;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;

/**
 *
 * @author toaster
 */
public class CreateBagDialog extends Dialog {

    private BagModel bagModel;
    private Accordion panelAccordion;
    private BasePanel verifyPane = new VerifyPane();
    private BasePanel choosePane = new ChooseBagPane();
    private BasePanel chooseLocalBag = new ChooseLocalBag();
    private BasePanel setUrlPattern = new SetUrlPattern();
    private BasePanel remotePane = new RemoteDestinationPane();
    private Map<ChronPackage, BagModel> modelCache = new HashMap<ChronPackage, BagModel>();
    private static final Logger LOG = Logger.getLogger(CreateBagDialog.class);
    private BagModelListener bagupdatelistener = new BagModelListener.Adaptor() {

        @Override
        public void ingestionTypeChanged(BagModel model, IngestionType oldType) {
            if (model.getIngestionType() == IngestionType.CHRONOPOLIS) {
                panelAccordion.getPanels().insert(remotePane, 1);
                panelAccordion.getPanels().remove(chooseLocalBag);
            } else {
                panelAccordion.getPanels().insert(chooseLocalBag, 1);
                panelAccordion.getPanels().remove(remotePane);
            }
            updateAccordion();
        }

        @Override
        public void bagTypeChanged(BagModel model, BagType oldType) {

            if (model.getBagType() == BagType.HOLEY) {
                panelAccordion.getPanels().insert(setUrlPattern,
                        panelAccordion.getPanels().getLength() - 1);
            } else {
                panelAccordion.getPanels().remove(setUrlPattern);
            }
            updateAccordion();
        }

        @Override
        public void urlPatternChanged(BagModel model, String oldPattern) {
            updateAccordion();
        }

        @Override
        public void saveFileChanged(BagModel model, File oldFile) {
            updateAccordion();
        }

        @Override
        public void chronopoligBagChanged(BagModel mode, String oldbagname) {

            updateAccordion();
        }
    };
    private AccordionSelectionListener accordionSelectionListener = new AccordionSelectionListener() {

        private int selectedIndex = -1;

        @Override
        public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
            this.selectedIndex = selectedIndex;

            // Enable the next panel or disable the previous panel so the
            // transition looks smoother
            if (selectedIndex != -1) {
                int previousSelectedIndex = accordion.getSelectedIndex();

                if (selectedIndex > previousSelectedIndex) {
                    accordion.getPanels().get(selectedIndex).setEnabled(true);
                } else {
                    accordion.getPanels().get(previousSelectedIndex).setEnabled(false);
                }

            }

            return Vote.APPROVE;
        }

        @Override
        public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
            if (reason == Vote.DENY
                    && selectedIndex != -1) {
                Component panel = accordion.getPanels().get(selectedIndex);
                panel.setEnabled(!panel.isEnabled());
            }
        }

        @Override
        public void selectedIndexChanged(Accordion accordion, int previousSelection) {
            updateAccordion();
        }
    };
    private MessageBusListener<ChronPackage> messagebuslistener = new MessageBusListener<ChronPackage>() {

        public void messageSent(ChronPackage message) {
            LOG.trace("Setting new model in choose bag pane " + message);

            if (message != null) {
                if (!modelCache.containsKey(message)) {
                    BagModel bm = new BagModel();
                    bm.setChronPackage(message);
                    modelCache.put(message, bm);
                }
                setBagModel(modelCache.get(message));
            }

        }
    };
    private ButtonPressListener prevButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            if (panelAccordion.getSelectedIndex() - 1 > -1);
            panelAccordion.setSelectedIndex(panelAccordion.getSelectedIndex() - 1);
        }
    };
    private ButtonPressListener nextButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            if (panelAccordion.getSelectedIndex() + 1 < panelAccordion.getPanels().getLength());
            panelAccordion.setSelectedIndex(panelAccordion.getSelectedIndex() + 1);
        }
    };
    private ButtonPressListener finishBagButtonPressListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            CreateBagDialog.this.close(true);
        }
    };

    public CreateBagDialog() {

        panelAccordion = new Accordion();
        panelAccordion.getAccordionSelectionListeners().add(accordionSelectionListener);

        choosePane.getNextButtonPressListeners().add(nextButtonListener);
        panelAccordion.getPanels().add(choosePane);

        verifyPane.getNextButtonPressListeners().add(finishBagButtonPressListener);
        verifyPane.getPreviousButtonPressListeners().add(prevButtonListener);
        panelAccordion.getPanels().add(verifyPane);

        chooseLocalBag.getNextButtonPressListeners().add(nextButtonListener);
        chooseLocalBag.getPreviousButtonPressListeners().add(prevButtonListener);

        remotePane.getNextButtonPressListeners().add(nextButtonListener);
        remotePane.getPreviousButtonPressListeners().add(prevButtonListener);

        setUrlPattern.getNextButtonPressListeners().add(nextButtonListener);
        setUrlPattern.getPreviousButtonPressListeners().add(prevButtonListener);

        MessageBus.subscribe(ChronPackage.class, messagebuslistener);

        setTitle("Create Bag");
        setPreferredSize(500, 550);
        setContent(panelAccordion);
    }

    @Override
    public void open(Display display, Window owner, DialogCloseListener dialogCloseListener) {
        super.open(display, owner, dialogCloseListener);
        panelAccordion.setSelectedIndex(0);
    }

    public void setBagModel(BagModel bagModel) {
        if (this.bagModel != null) {
            this.bagModel.getModelListenerList().remove(bagupdatelistener);
        }
        panelAccordion.getPanels().remove(chooseLocalBag);
        panelAccordion.getPanels().remove(remotePane);
        panelAccordion.getPanels().remove(setUrlPattern);

        this.bagModel = bagModel;
        if (bagModel != null) {
            bagModel.getModelListenerList().add(bagupdatelistener);
            if (bagModel.getBagType() == BagType.HOLEY) {
                panelAccordion.getPanels().insert(setUrlPattern, 1);
            }

            if (bagModel.getIngestionType() == IngestionType.CHRONOPOLIS) {
                panelAccordion.getPanels().insert(remotePane, 1);
            } else if (bagModel.getIngestionType() == IngestionType.LOCAL) {
                panelAccordion.getPanels().insert(chooseLocalBag, 1);
            }
        }
        choosePane.setBagModel(bagModel);
        verifyPane.setBagModel(bagModel);
        setUrlPattern.setBagModel(bagModel);
        chooseLocalBag.setBagModel(bagModel);
        remotePane.setBagModel(bagModel);

        panelAccordion.setSelectedIndex(0);
        updateAccordion();

    }

    public BagModel getBagModel() {
        return bagModel;
    }

    private void updateAccordion() {
        int index = panelAccordion.getSelectedIndex();
        int next = index + 1;
        if (index > -1 && index < (panelAccordion.getPanels().getLength() - 1)) {
            BasePanel v = (BasePanel) panelAccordion.getPanels().get(index);
            LOG.trace("Updating accordion, next panel enabled: " + v.isComplete()
                    + " curr:" + index + "/" + panelAccordion.getPanels().getLength());
            if (v.isComplete() == Vote.APPROVE) {
                panelAccordion.getPanels().get(next).setEnabled(true);
            } else {
                panelAccordion.getPanels().get(next).setEnabled(false);
            }
        }
        for (int i = next + 1; i < panelAccordion.getPanels().getLength(); i++) {
            LOG.trace("Disabling: " + panelAccordion.getPanels().get(i).getClass().getSimpleName());
            panelAccordion.getPanels().get(i).setEnabled(false);
        }
    }
}
