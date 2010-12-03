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
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.AccordionSelectionListener;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.ApplicationContextMessageListener;
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
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ButtonData;
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtk.validation.Validator;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.chronopolis.ingest.pkg.ChronPackage;
import org.chronopolis.ingest.pkg.URLUTF8Encoder;

/**
 *
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
    @WTKX
    private Label vrfyDestLbl;
    @WTKX
    private Label vrfyTypeLbl;
    @WTKX
    private Label vrfyPatternLbl;
    @WTKX
    private Label vrfyLocationLbl;
    @WTKX
    private Label vrfyPatternHdr;
    @WTKX
    private PushButton okBtn;
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
    private ButtonPressListener pane1ButtonListener = new ButtonPressListener() {

        public void buttonPressed(Button button) {
            pane1Message.setText("");
//                System.out.println(outputGroup.getSelection());
            if (outputGroup.getSelection() == null) {
                pane1Message.setText("Please Choose Destination");
            } else if (typeGroup.getSelection() == null) {
                pane1Message.setText("Please Choose Bag Type");
            } else {

                accordion.setSelectedIndex(accordion.getSelectedIndex() + 1);
                String dest = (outputGroup.getSelection() == localBtn ? "Local Bag" : "Chronopolis");
                vrfyDestLbl.setText(dest);
                if (typeGroup.getSelection() == holeyBtn) {
                    vrfyTypeLbl.setText("Holey Bag");
                    vrfyPatternLbl.setVisible(true);
                    vrfyPatternHdr.setVisible(true);
                } else {
                    vrfyTypeLbl.setText("Filled Bag");
                    vrfyPatternLbl.setVisible(false);
                    vrfyPatternHdr.setVisible(false);
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
                    System.out.println(code + " " + c.getResponseMessage());
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
        File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
        saveFileLbl.setText(bagFile.getAbsolutePath());
        pane2bNextBtn.getButtonPressListeners().add(pane2bButtonListener);

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
        if (workingPackage == null)
            return;
        
        String txt = urlTxt.getText();
        if (Strings.isEmpty(txt)) {
            sampleUrlLbl.setText("Add URL to create fetch file");
        }
        String newTxt = txt.replaceAll("\\{b\\}", workingPackage.getName());
        if (workingPackage.findFirstFile() == null) {
            newTxt = newTxt.replaceAll("\\{d\\}", "").replaceAll("\\{r\\}", "");
        } else {
            newTxt = newTxt.replaceAll("\\{d\\}", "data" + "/" + workingPackage.findFirstFile()).replaceAll("\\{r\\}", workingPackage.findFirstFile());
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
        updateSampleUrl();
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
