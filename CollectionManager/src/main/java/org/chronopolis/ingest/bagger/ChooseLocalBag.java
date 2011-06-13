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
import org.apache.pivot.beans.BXML;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;

/**
 *
 * @author toaster
 */
public class ChooseLocalBag extends BasePanel {

    @BXML
    private FileBrowser browser;
    @BXML
    private TextInput bagfileTxt;
    @BXML
    private Label saveFileLbl;
    private FileBrowserListener browserLabelUpdateListener = new FileBrowserListener.Adapter() {

        @Override
        public void rootDirectoryChanged(FileBrowser fb, File file) {
            File bagFile = new File(browser.getRootDirectory(), bagfileTxt.getText());
            getBagModel().setSaveFile(bagFile);
        }
    };
    private BagModelListener bagListener = new BagModelListener.Adaptor() {

        @Override
        public void saveFileChanged(BagModel model, File oldFile) {
            updateFromModel();
        }
    };
    private TextInputContentListener bagNameListener = new TextInputContentListener.Adapter() {

        @Override
        public void textChanged(TextInput textInput) {
            File bagFile = new File(browser.getRootDirectory(), textInput.getText());
            getBagModel().setSaveFile(bagFile);
        }
    };

    private void updateFromModel() {
        BagModel model = getBagModel();
        if (model != null && model.getSaveFile() != null) {
            saveFileLbl.setText(model.getSaveFile().getPath());
            if (!browser.getRootDirectory().equals(model.getSaveFile().getParentFile())) {
                browser.setRootDirectory(model.getSaveFile().getParentFile());
            }
            if (!bagfileTxt.getText().equals(model.getSaveFile().getName())) {
                bagfileTxt.setText(model.getSaveFile().getName());
            }
        }
        updateNext();
    }

    @Override
    protected void modelChanged(BagModel old) {
        if (old != null) {
            old.getModelListenerList().remove(bagListener);
        }
        if (getBagModel() != null) {
            BagModel model = getBagModel();
            model.getModelListenerList().add(bagListener);
            if (model.getSaveFile() == null) {
                model.setSaveFile(new File(browser.getRootDirectory(),
                        model.getChronPackage().getName() + ".tgz"));

            }
        }
        updateFromModel();
    }

    public ChooseLocalBag() {
        super("chooseLocalBag.bxml");
        Accordion.setHeaderData(this, "2. Choose Bag Location");
        browser.getFileBrowserListeners().add(browserLabelUpdateListener);
        bagfileTxt.getTextInputContentListeners().add(bagNameListener);

    }

    private void updateNext() {
        if (getBagModel() == null || getBagModel().getSaveFile() == null
                || !getBagModel().getSaveFile().getParentFile().canWrite()) {
            setErrorMessage("Cannot write to selected file " + getBagModel().getSaveFile().getParentFile().canWrite());
        } else {
            setErrorMessage("");
            
        }
    }

    public Vote isComplete() {
        if (getBagModel() != null && getBagModel().getSaveFile() != null
                && getBagModel().getSaveFile().getParentFile().canWrite()) {
            return Vote.APPROVE;
        } else {
            return Vote.DENY;
        }
    }
}
