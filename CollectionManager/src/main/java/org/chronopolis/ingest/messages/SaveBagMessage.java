/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.messages;

import java.io.File;
import org.apache.pivot.wtk.Window;
import org.chronopolis.ingest.pkg.ChronPackage;

/**
 *
 * @author toaster
 */
public final class SaveBagMessage {

    private ChronPackage pkg;
    private File saveFile;
    private Window displayWindow;

    public SaveBagMessage(ChronPackage pkg, File saveFile, Window displayWindow) {
        this.pkg = pkg;
        this.saveFile = saveFile;
        this.displayWindow = displayWindow;
    }

    public ChronPackage getPkg() {
        return pkg;
    }

    public File getSaveFile() {
        return saveFile;
    }

    public Window getDisplayWindow() {
        return displayWindow;
    }

}
