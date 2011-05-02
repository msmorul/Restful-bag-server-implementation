/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

/**
 *
 * @author toaster
 */
public class Main implements Application {

    public static final String SERVER = "jetty.server";

    public static void main(String[] args) {
        DesktopApplicationContext.main(Main.class, args);
    }

    public void startup(Display dspl, Map<String, String> map) throws Exception {
        Window mainW;

        // build app
        BXMLSerializer serializer = new BXMLSerializer();
        mainW = (Window) serializer.readObject(Main.class, "applicationWindow.bxml");
        serializer.bind(this);


        mainW.open(dspl);
    }

    public boolean shutdown(boolean bln) throws Exception {
        return false;
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
