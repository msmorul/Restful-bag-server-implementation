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
import java.io.IOException;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.MessageBus;
import org.apache.pivot.util.MessageBusListener;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.chronopolis.ingest.pkg.BagWriter;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.ChronPackageListener;

/**
 *
 * @author toaster
 */
public class BagAssemblyPanel extends Border {

    private ChronPackage workingBag;
    @BXML
    private ListView rootList;
    @BXML
    private TextInput nameText;
    @BXML
    private FileBrowserSheet browser;
    @BXML
    private PushButton addFilesBtn;
    @BXML
    private ListButton digestListBtn;
    @BXML
    private PushButton removeBtn;
    @BXML
    private PushButton removeFileBtn;
    private ChronPackageListener listener = new ChrListener();
    private ButtonPressListener removeItemListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            Prompt.prompt(MessageType.QUESTION, "Remove directory from bag " + rootList.getSelectedItem(), getWindow(), new SheetCloseListener() {

                public void sheetClosed(Sheet sheet) {
                    if (sheet.getResult()) {
                        workingBag.getRootList().remove((File) rootList.getSelectedItem());
                    }
                }
            });
        }
    };
    private ButtonPressListener addItemlistener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            browser.open(getWindow());
        }
    };
    private MenuHandler menuHandler = new MenuHandler.Adapter() {

        @Override
        public boolean configureContextMenu(Component cmpnt, Menu menu, int i, int y) {

            if (workingBag.isReadOnly()) {
                return false;
            }

            Menu.Section section = new Menu.Section();
            menu.getSections().add(section);

            Menu.Item addItem = new Menu.Item("Add Directory");
            section.add(addItem);
            addItem.getButtonPressListeners().add(addItemlistener);

            if (rootList.getItemAt(y) > -1) {
                Menu.Item removeItem = new Menu.Item("Remove");
                section.add(removeItem);
                removeItem.getButtonPressListeners().add(removeItemListener);
            }

            return false;
        }
    };

    public BagAssemblyPanel() {
        try {

            BXMLSerializer serializer = new BXMLSerializer();
            Component pkgPane = (Component) serializer.readObject(BagAssemblyPanel.class, "bagAssemblyPanel.bxml");

            serializer.bind(this);
            setContent(pkgPane);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        removeBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                Prompt.prompt(MessageType.QUESTION, "Remove local bag " + workingBag.getName(), button.getWindow(), new SheetCloseListener() {

                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            Main.getPackageManager().getPackageList().remove(workingBag);
                        }

                    }
                });
            }
        });

        rootList.getParent().setMenuHandler(menuHandler);

        rootList.getListViewSelectionListeners().add(new ListViewSelectionListener() {

            public void selectedItemChanged(ListView lv, Object previousSelectedItem) {
                removeFileBtn.setEnabled(lv.getSelectedItem() != null && !workingBag.isReadOnly());
            }

            public void selectedRangeAdded(ListView lv, int i, int i1) {
                removeFileBtn.setEnabled(lv.getSelectedItem() != null && !workingBag.isReadOnly());
            }

            public void selectedRangeRemoved(ListView lv, int i, int i1) {
                removeFileBtn.setEnabled(lv.getSelectedItem() != null && !workingBag.isReadOnly());
            }

            public void selectedRangesChanged(ListView lv, Sequence<Span> sqnc) {
                removeFileBtn.setEnabled(lv.getSelectedItem() != null && !workingBag.isReadOnly());
            }
        });

        removeFileBtn.getButtonPressListeners().add(removeItemListener);

        rootList.setItemRenderer(new ListViewItemRenderer() {

            @Override
            public void render(Object item, int index, ListView listView, boolean selected, boolean checked, boolean highlighted, boolean disabled) {
                if (item instanceof File) {
                    super.render(((File) item).getName(), index, listView, selected, checked, highlighted, disabled);
                } else {
                    super.render(item, index, listView, selected, checked, highlighted, disabled);
                }

            }
        });

        nameText.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {

            @Override
            public void textChanged(TextInput ti) {
                if (workingBag != null) {
                    workingBag.setName(ti.getText());
                    workingBag.getMetadataMap().put(BagWriter.INFO_INTERNAL_SENDER_IDENTIFIER, ti.getText());
                }
            }
        });

        browser.setRootDirectory(Main.getDefaultDirectory());
        browser.getSheetStateListeners().add(new SheetStateListener.Adapter() {

            @Override
            public void sheetClosed(Sheet sheet) {
                if (sheet.getResult() && workingBag != null) {
                    workingBag.getRootList().add(browser.getSelectedFile());
                }
            }
        });

        MessageBus.subscribe(ChronPackage.class, new MessageBusListener<ChronPackage>() {

            public void messageSent(ChronPackage t) {
                setWorkingBag(t);
            }
        });

        digestListBtn.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

            @Override
            public void selectedItemChanged(ListButton lb, Object previousSelectedItem) {
                String s = (String) lb.getSelectedItem();
                workingBag.setDigest(s);
            }
//
//            public void selectedIndexChanged(ListButton lb, int i) {
//                if (i > -1) {
//
//                }
//            }
        });
    }

    public void setWorkingBag(ChronPackage workingBag) {

        if (this.workingBag != null) {
            this.workingBag.getChronPackageListeners().remove(listener);
        }
        rootList.clearSelection();

        this.workingBag = workingBag;
        workingBag.getChronPackageListeners().add(listener);
        if (workingBag.getName() != null) {
            nameText.setText(workingBag.getName());
        } else {
            nameText.setText("");
        }

        digestListBtn.setSelectedItem(workingBag.getDigest());

        updateState();
        rootList.setListData(workingBag.getRootList());
        removeFileBtn.setEnabled(false);

    }

    public ChronPackage getWorkingBag() {
        return workingBag;
    }

    private void updateState() {
        digestListBtn.setEnabled(!workingBag.isReadOnly());
        nameText.setEnabled(!workingBag.isReadOnly());
        addFilesBtn.setEnabled(!workingBag.isReadOnly());
        removeFileBtn.setEnabled(!workingBag.isReadOnly());
    }

    private class ChrListener extends ChronPackageListener.Adapter {

        @Override
        public void readOnlyChanged(ChronPackage pkg, boolean old) {
            updateState();
        }

        @Override
        public void nameChanged(ChronPackage pkg, String oldname) {
        }
    }
}
