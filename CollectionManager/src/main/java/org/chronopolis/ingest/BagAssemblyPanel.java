/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
