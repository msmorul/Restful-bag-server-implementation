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

import java.net.URL;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.apache.log4j.Logger;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DialogStateListener;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.SheetCloseListener;
import org.chronopolis.ingest.bagger.CreateBagDialog;
import org.chronopolis.ingest.messages.SaveBagMessage;
import org.chronopolis.ingest.messages.TransferBagMessage;

/**
 *
 * @author toaster
 */
public class ApplicationWindow extends Window implements Bindable {

    private static final Logger LOG = Logger.getLogger(ApplicationWindow.class);
    @BXML
    private ListView ingestedListView;
    @BXML
    private BagAssemblyPanel assemblyPanel;
    @BXML
    private Border detailBorder;
    @BXML
    private ListView pendingListView;
    @BXML
    private Menu.Item saveBagBtn;
    @BXML
    private Menu.Item createBagBtn;
    @BXML
    private ArchivedCollectionPanel archivePanel;
    @BXML
    private FileBrowserSheet digestBrowser;
    @BXML
    private CreateBagDialog createBagDialog;
    private MenuHandler menuHandler = new MenuHandler.Adapter() {

        @Override
        public boolean configureContextMenu(Component cmpnt, Menu menu, int i, int y) {

            Menu.Section section = new Menu.Section();
            menu.getSections().add(section);

            Menu.Item addItem = new Menu.Item("New Bag");
            section.add(addItem);
            addItem.getButtonPressListeners().add(new ButtonPressListener() {

                public void buttonPressed(Button button) {
                    ChronPackage newPkg = new ChronPackage();
                    newPkg.setDigest("SHA-256");
                    Main.getPackageManager().getPackageList().add(newPkg);
                    pendingListView.setSelectedIndex(pendingListView.getListData().getLength() - 1);
                }
            });

            if (pendingListView.getItemAt(y) > -1) {
                final ChronPackage remPkg = Main.getPackageManager().getPackageList().get(pendingListView.getItemAt(y));
                Menu.Item removeItem = new Menu.Item("Remove");
                section.add(removeItem);
                removeItem.getButtonPressListeners().add(new ButtonPressListener() {

                    public void buttonPressed(Button button) {
                        Prompt.prompt(MessageType.QUESTION, "Remove bag " + remPkg.getName(), ApplicationWindow.this, new SheetCloseListener() {

                            public void sheetClosed(Sheet sheet) {
                                if (sheet.getResult()) {
                                    Main.getPackageManager().getPackageList().remove(remPkg);
                                }

                            }
                        });
                    }
                });

                Menu.Item duplicateItem = new Menu.Item("Create Duplicate");
                duplicateItem.setTooltipText("Create a writable duplicate of this package");
                duplicateItem.getButtonPressListeners().add(new ButtonPressListener() {

                    public void buttonPressed(Button button) {
                        Main.getPackageManager().getPackageList().add(remPkg.clone());
                        pendingListView.setSelectedIndex(pendingListView.getListData().getLength() - 1);
                    }
                });
                section.add(duplicateItem);
            }

            return false;
        }
    };
    private ListViewItemRenderer pendingListRenderer = new ListViewItemRenderer() {

        @Override
        public void render(Object item, int index, ListView listView,
                boolean selected, boolean checked, boolean highlighted, boolean disabled) {
            if (item instanceof ChronPackage) {
                ChronPackage cp = (ChronPackage) item;
                if (cp.getName() == null || cp.getName().isEmpty()) {
                    super.render("<Unnamed>", index, listView, selected,
                            checked, highlighted, disabled);

                } else {
                    super.render(cp.getName(), index, listView, selected,
                            checked, highlighted, disabled);
                }

            } else {
                super.render(item, index, listView, selected, checked,
                        highlighted, disabled);
            }
        }
    };
    private ListViewSelectionListener pendingListSelectionListener = new ListViewSelectionListener.Adapter() {

        @Override
        public void selectedRangesChanged(ListView lv, Sequence<Span> sqnc) {
            if (pendingListView.getSelectedItem() != null) {
                MessageBus.sendMessage(pendingListView.getSelectedItem());
//                    createbagBtn.setEnabled(!((ChronPackage) pendingListView.getSelectedItem()).isReadOnly());

                saveBagBtn.setEnabled(true);
                createBagBtn.setEnabled(true);
            } else {
//                    transferBtn.setEnabled(false);
                saveBagBtn.setEnabled(false);
                createBagBtn.setEnabled(false);
            }
            detailBorder.setContent(assemblyPanel);
            ingestedListView.clearSelection();
        }
    };
    private ListViewSelectionListener ingestListSelectionListener = new ListViewSelectionListener.Adapter() {

        @Override
        public void selectedRangesChanged(ListView lv, Sequence<Span> sqnc) {

            if (ingestedListView.getSelectedItem() != null) {
                MessageBus.sendMessage(ingestedListView.getSelectedItem());
            }
            detailBorder.setContent(archivePanel);
            pendingListView.clearSelection();
            saveBagBtn.setEnabled(false);
            createBagBtn.setEnabled(true);
        }
    };

    public ApplicationWindow() {
    }

    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        pendingListView.getListViewSelectionListeners().add(pendingListSelectionListener);
        pendingListView.setItemRenderer(pendingListRenderer);
        pendingListView.getParent().setMenuHandler(menuHandler);

        ingestedListView.getListViewSelectionListeners().add(ingestListSelectionListener);
        ingestedListView.setItemRenderer(new BagBeanRenderer());
        digestBrowser.setRootDirectory(Main.getDefaultDirectory());
        digestBrowser.getSheetStateListeners().add(new SheetStateListener.Adapter() {

            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult()) {
                    LOG.debug("Sending package save message ");
                    SaveBagMessage msg = new SaveBagMessage(
                            (ChronPackage) pendingListView.getSelectedItem(),
                            digestBrowser.getSelectedFile(),
                            ApplicationWindow.this);
                    MessageBus.sendMessage(msg);
                }
            }
        });

        createBagDialog.getDialogStateListeners().add(new DialogStateListener.Adapter() {

            @Override
            public void dialogClosed(Dialog dialog, boolean modal) {
                if (dialog.getResult()) {
                    TransferBagMessage msg = new TransferBagMessage(
                            createBagDialog.getBagModel(), ApplicationWindow.this);
                    MessageBus.sendMessage(msg);
                }
            }
        });
    }
}
