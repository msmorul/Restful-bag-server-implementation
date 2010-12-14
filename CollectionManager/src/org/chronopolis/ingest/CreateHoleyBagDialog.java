/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest;

import edu.umiacs.ace.json.StatusBean.CollectionBean;
import edu.umiacs.ace.json.Strings;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ApplicationContextMessageListener;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.chronopolis.ingest.bagger.BagModel;
import org.chronopolis.ingest.pkg.BagWriter;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.URLUTF8Encoder;

/**
 * TODO: each pane should be its own class
 * @author toaster
 */
public class CreateHoleyBagDialog extends Dialog {

    private ChronPackage workingPackage;
    @WTKX
    private Accordion accordion;
    // pane1
    @WTKX
    private PushButton pane1NextBtn;
    @WTKX
    private Label pane1Message;
    @WTKX
    private ButtonGroup outputGroup;
    @WTKX
    private ButtonGroup typeGroup;
    @WTKX
    private PushButton holeyBtn;
    @WTKX
    private PushButton localBtn;
    @WTKX
    private PushButton chronopolisBtn;
    // pane 2a chron
//    @WTKX
    private ListButton collectionListButton = new ListButton();
    @WTKX
    private TextInput transferTxt;
    @WTKX
    private PushButton pane2aNextBtn;
    @WTKX
    private Label pane2aMessage;
    // pane 2b local
    @WTKX
    private FileBrowser browser;
    @WTKX
    private PushButton pane2bNextBtn;
    @WTKX
    private Label pane2bMessage;
    @WTKX
    private TextInput bagfileTxt;
    @WTKX
    private Label saveFileLbl;
    // pane 3 holey bag
    @WTKX
    private TextInput sampleUrlLbl;
    @WTKX
    private TextInput urlTxt;
    @WTKX
    private PushButton pane3NextBtn;
    @WTKX
    private Label pane3Message;
    @WTKX
    private PushButton testUrlBtn;
    // pane 4 verify
//    @WTKX
//    private Label vrfyDestLbl;
//    @WTKX
//    private Label vrfyTypeLbl;
//    @WTKX
//    private Label vrfyPatternLbl;
//    @WTKX
//    private Label vrfyLocationLbl;
//    @WTKX
//    private Label vrfyPatternHdr;
//    @WTKX
//    private Label vrfyFilesLbl;
//    @WTKX
//    private Label vrfyDirectoriesLbl;
//    @WTKX
//    private Label vrfySizeLbl;
//    @WTKX
//    private Label vrfyUnreadableLbl;
//    @WTKX
//    private ListView vrfyUnreadableList;
//    @WTKX
//    private Border vrfyUnreadablePane;
//    @WTKX
//    private PushButton okBtn;
//    @WTKX
//    private TableView metadataTable;
//    @WTKX
//    private TextInput vrfyFetchTxt;
//    @WTKX
//    private Label vrfyFetchLbl;
//    @WTKX
//    private TextInput vrfyManifestTxt;
//    @WTKX
//    private TableView vrfyDirectoryTable;
//    @WTKX
//    private PushButton verifyPreviousBtn;
    private boolean isVerify = false;
    private long totalSize;
    private BagModel model;
    ///
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
    private Task<ChronPackage.Statistics> updateTask = new Task<ChronPackage.Statistics>() {

        @Override
        public ChronPackage.Statistics execute() throws TaskExecutionException {
            try {
                return workingPackage.createStatistics(
                        new ChronPackage.AbortScanNotifier() {

                            public boolean aborted() {
                                return !isVerify;
                            }
                        });
            } finally {
                isVerify = false;
            }
        }
    };
    //TODO: Clean this entire crap pile up.
    private AccordionSelectionListener verifyUpdateAccordionListener = new AccordionSelectionListener.Adapter() {

        @Override
        public Vote previewSelectedIndexChange(Accordion acrdn, int i) {
            okBtn.setEnabled(false);

            if (i == (acrdn.getPanels().getLength() - 1)) {
                if (isVerify) {
                    return Vote.APPROVE;
                }

                isVerify = true;
                //TODO: Make this a new components
                final Sheet statusSheet = new Sheet();
                statusSheet.setPreferredHeight(175);
                statusSheet.setPreferredWidth(325);
                ActivityIndicator idc = new ActivityIndicator();
                idc.setPreferredWidth(96);
                idc.setPreferredHeight(96);
                idc.setActive(true);
                BoxPane bp = new BoxPane();
                bp.getStyles().put("horizontalAlignment", "center");
                bp.add(idc);
                bp.setOrientation(Orientation.VERTICAL);
                bp.add(new Label("Scanning Bag..."));
                PushButton pb = new PushButton("Skip");
                pb.getButtonPressListeners().add(new ButtonPressListener() {

                    public void buttonPressed(Button button) {
                        isVerify = false;
                    }
                });
                bp.add(pb);
                statusSheet.setContent(bp);

                TaskListener<ChronPackage.Statistics> taskListener = new TaskListener<ChronPackage.Statistics>() {

                    public void taskExecuted(Task<ChronPackage.Statistics> task) {
                        ChronPackage.Statistics stats = task.getResult();
                        if (stats != null) {
                            vrfyFilesLbl.setText(Long.toString(stats.getFiles()));
                            vrfyDirectoriesLbl.setText(Long.toString(stats.getDirectories()));
                            vrfySizeLbl.setText(Util.formatSize(stats.getSize()));
                            totalSize = stats.getSize();
                            vrfyUnreadableLbl.setText(Long.toString(stats.getUnreadable()));
                            vrfyUnreadablePane.setVisible(stats.getUnreadable() > 0);
                            vrfyUnreadableList.setListData(stats.getUnreadableFiles());
                        } else {
                            vrfyFilesLbl.setText("scan aborted");
                            vrfyDirectoriesLbl.setText("scan aborted");
                            vrfySizeLbl.setText("scan aborted");
                            totalSize = 0;
                            vrfyUnreadableLbl.setText("scan aborted");
                            vrfyUnreadablePane.setVisible(false);
                        }
                        okBtn.setEnabled(true);
                        statusSheet.close(true);
                    }

                    public void executeFailed(Task<ChronPackage.Statistics> task) {
                        okBtn.setEnabled(false);
                        if (!isVerify) {
                            Alert.alert(MessageType.ERROR, "Count not scan bag", accordion.getDisplay());
                        }
                        statusSheet.close(true);
                    }
                };

                statusSheet.open(acrdn.getWindow(), new SheetCloseListener() {

                    public void sheetClosed(Sheet sheet) {

                        if (!sheet.getResult()) {
                            isVerify = false;
                        }
                    }
                });

                updateTask.execute(taskListener);
            } else {
                isVerify = false;
            }

            return Vote.APPROVE;
        }
    };
    private ButtonPressListener pane1ButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            pane1Message.setText("");
            if (outputGroup.getSelection() == null) {
                pane1Message.setText("Please Choose Destination");
            } else if (typeGroup.getSelection() == null) {
                pane1Message.setText("Please Choose Bag Type");
            } else {

                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
                String dest = (outputGroup.getSelection() == localBtn ? "Local Bag" : "Chronopolis");
                vrfyDestLbl.setText(dest);
                vrfyManifestTxt.setText("9a9a-" + workingPackage.getDigest() + "-digest-9a9a  data/" + workingPackage.findRelativeFirstFile());
                if (typeGroup.getSelection() == holeyBtn) {
                    model.setBagType(BagModel.BagType.HOLEY);
                } else {
                    model.setBagType(BagModel.BagType.FILLED);
                }
            }
        }
    };
    private ButtonPressListener pane2aButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            pane2aMessage.setText("");
//                if (collectionListButton.getSelectedItem() == null) {
            if (Strings.isEmpty(transferTxt.getText())) {
                pane2aMessage.setText("Please select collection or enter bag name");
            } else {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
                vrfyLocationLbl.setText(transferTxt.getText());
            }
        }
    };
    private FileBrowserListener browserLabelUpdateListener = new FileBrowserListener.Adapter() {

        @Override
        public void rootDirectoryChanged(FileBrowser fb, File file) {
            File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
            saveFileLbl.setText(bagFile.getAbsolutePath());
        }
    };
    private ButtonPressListener pane2bButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            pane2bMessage.setText("");
            File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
            if (!browser.getRootDirectory().canWrite()) {
                pane2bMessage.setText("Cannot write to selected directory");
            } else {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
                vrfyLocationLbl.setText(bagFile.getPath());
            }
        }
    };
    private ButtonPressListener pane3ButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            pane3Message.setText("");
            if (urlTxt.getText() != null) {
                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
                vrfyPatternLbl.setText(urlTxt.getText());
                vrfyFetchTxt.setText(sampleUrlLbl.getText() + "  " + workingPackage.findFirstFile().length() + "  data/" + workingPackage.findRelativeFirstFile());
            } else {
                pane3Message.setText("Please enter a URL base for this holey bag");

            }
        }
    };
    private ButtonPressListener testUrlButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            String url = sampleUrlLbl.getText();
            if (!url.startsWith("http")) {
                Alert.alert("Only http(s) URL's can be tested", button.getWindow());
                return;
            }
            try {
                URL u = new URL(url);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setConnectTimeout(2000);
                try {
                    int code = c.getResponseCode();
                    if (c.getResponseCode() < 200 || c.getResponseCode() > 299) {
                        Alert.alert(MessageType.ERROR, "Transfer error HTTP/"
                                + c.getResponseCode() + " " + c.getResponseMessage(), button.getWindow());
                        c.disconnect();
                        return;
                    }
                } catch (IOException ioe) {
                    Alert.alert(MessageType.ERROR, "Cannot retrieve url (" + ioe.getClass().getSimpleName() + ") " + ioe.getMessage(), button.getWindow());
                    return;
                }
                c.disconnect();
                Alert.alert(MessageType.INFO, "Data URL is good!", button.getWindow());
            } catch (IOException e) {
                Alert.alert(MessageType.ERROR, "Cannot retrieve url (" + e.getClass().getSimpleName() + ") " + e.getMessage(), button.getWindow());
                return;
            }
        }
    };

    public CreateHoleyBagDialog() {
        try {

            WTKXSerializer serializer = new WTKXSerializer();
            Component mainW = (Component) serializer.readObject(this, "createHoleyBagDialog.wtkx");
            serializer.bind(this);
            setContent(mainW);

            ApplicationContext.subscribe(ChronPackage.class, new ApplicationContextMessageListener<ChronPackage>() {

                public void messageSent(ChronPackage t) {
                    workingPackage = t;
                }
            });
            updateAccordion();

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        accordion.getAccordionSelectionListeners().add(accordionSelectionListener);
        accordion.getAccordionSelectionListeners().add(verifyUpdateAccordionListener);
        urlTxt.getTextInputTextListeners().add(new TextInputTextListener() {

            public void textChanged(TextInput ti) {

                updateSampleUrl();
            }
        });

        // pane 1 configuration
        chronopolisBtn.setEnabled(Main.getURL() != null);
        pane1NextBtn.getButtonPressListeners().add(pane1ButtonListener);

        // pane 2a configuration
        collectionListButton.setItemRenderer(new CollectionBeanRenderer());
        collectionListButton.setDataRenderer(new CollectionButtonRenderer());
        pane2aNextBtn.getButtonPressListeners().add(pane2aButtonListener);

        // pane 2b configuration
        browser.setRootDirectory(Main.getDefaultDirectory());
        browser.setDisabledFileFilter(new Filter<File>() {

            public boolean include(File t) {
                return !(t.isDirectory());
            }
        });

        browser.getFileBrowserListeners().add(browserLabelUpdateListener);
//        File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
//        saveFileLbl.setText(bagFile.getAbsolutePath());
        pane2bNextBtn.getButtonPressListeners().add(pane2bButtonListener);
        bagfileTxt.getTextInputTextListeners().add(new TextInputTextListener() {

            public void textChanged(TextInput ti) {
                if (browser.getRootDirectory() != null && bagfileTxt.getText() != null) {

                    File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
                    saveFileLbl.setText(bagFile.getAbsolutePath());
                }
            }
        });

        // pane 3 config
        urlTxt.setText(Main.getDefaultURLPattern());
        pane3NextBtn.getButtonPressListeners().add(pane3ButtonListener);
        testUrlBtn.getButtonPressListeners().add(testUrlButtonListener);

        // fnial

        okBtn.getButtonPressListeners().add(new ButtonPressListener() {

            public void buttonPressed(Button button) {
                close(true);
            }
        });

        setTitle("Create Bag");
        setPreferredSize(500, 550);
    }

    private void updateSampleUrl() {
        if (workingPackage == null) {
            return;
        }

        String txt = urlTxt.getText();
        if (Strings.isEmpty(txt)) {
            sampleUrlLbl.setText("Add URL to create fetch file");
        }
        String newTxt = txt.replaceAll("\\{b\\}", workingPackage.getName());

        if (workingPackage.findFirstFile() == null) {
            newTxt = newTxt.replaceAll("\\{d\\}", "").replaceAll("\\{r\\}", "");
        } else {
            newTxt = newTxt.replaceAll("\\{d\\}", "data" + "/" + workingPackage.findRelativeFirstFile()).replaceAll("\\{r\\}", workingPackage.findRelativeFirstFile());
        }
        sampleUrlLbl.setText(URLUTF8Encoder.encode(newTxt));

    }

    @Override
    public void open(Display display, Window owner, DialogCloseListener dialogCloseListener) {
        super.open(display, owner, dialogCloseListener);
        accordion.setSelectedIndex(0);
        if (workingPackage.getName() != null && workingPackage.getName().length() > 0) {
            bagfileTxt.setText(workingPackage.getName() + ".tgz");
            transferTxt.setText(workingPackage.getName() + ".tgz");
        } else {
            transferTxt.setText("newbag.tgz");
            bagfileTxt.setText("newbag.tgz");
        }
        updateDirectoryView();
        updateSampleUrl();
        updateMetadataView();
    }

    private void updateDirectoryView() {
        List<MetadataEntry> entries = new ArrayList<MetadataEntry>();

        for (File f : workingPackage.getRootList()) {
            entries.add(new MetadataEntry(f.getName(), f.getAbsolutePath()));
        }
        vrfyDirectoryTable.setTableData(entries);

    }

    private void updateMetadataView() {
        List<MetadataEntry> entries = new ArrayList<MetadataEntry>();
        for (String key : workingPackage.getMetadataMap()) {
            entries.add(new MetadataEntry(key, workingPackage.getMetadataMap().get(key)));
        }
        entries.add(new MetadataEntry(BagWriter.INFO_BAGGING_DATE, "Calculated on transfer"));
        entries.add(new MetadataEntry(BagWriter.INFO_PAYLOAD_OXUM,"Calculated on transfer"));
        entries.add(new MetadataEntry(BagWriter.INFO_BAG_SIZE,"Calculated on transfer"));
        metadataTable.setTableData(entries);
    }

    public class MetadataEntry {

        private String key;
        private String value;

        public MetadataEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private void updateAccordion() {
        int selectedIndex = accordion.getSelectedIndex();

        Sequence<Component> panels = accordion.getPanels();
        for (int i = 0, n = panels.getLength(); i < n; i++) {
            panels.get(i).setEnabled(i <= selectedIndex);
        }
    }

    public void setCollectionListData(List<CollectionBean> collectionList) {
        collectionListButton.setListData(collectionList);
        if (collectionList.getLength() > 0) {
            collectionListButton.setSelectedIndex(0);
        }
    }

    public String getBagName() {
        if (chronopolisBtn.isSelected()) {
            return transferTxt.getText();
        } else {
            return bagfileTxt.getText();
        }
    }

    public boolean isLocal() {
        return localBtn.isSelected();
    }

    public boolean isHoley() {
        return holeyBtn.isSelected();
    }

    public String getUrlPattern() {
        return urlTxt.getText();
    }

    public File getBagFile() {
        return new File(browser.getRootDirectory(), bagfileTxt.getText());
    }

    public ListenerList<ButtonPressListener> getAcceptButtonPressListeners() {
        return okBtn.getButtonPressListeners();
    }

    public void setAcceptButtonData(ButtonData data) {
        okBtn.setButtonData(data);
    }

    public long getTotalSize() {
        return totalSize;
    }

    private class CollectionButtonRenderer extends ButtonDataRenderer {

        @Override
        public void render(Object data, Button button, boolean highlighted) {
            if (data instanceof CollectionBean) {
                super.render(((CollectionBean) data).getName(), button, highlighted);
            } else {
                super.render(data, button, highlighted);
            }
        }
    }
}
