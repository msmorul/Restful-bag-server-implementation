/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.servlet;

import java.io.File;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 * @author toaster
 */
public class DropBoxConfigurationContext implements ServletContextListener {
    public static String PARAM_BOX = "directory";

    public void contextInitialized(ServletContextEvent sce) {
        String directory = sce.getServletContext().getInitParameter(PARAM_BOX);
        if (directory == null || directory.length() == 0)
            throw new IllegalArgumentException("Bad directory");
        File dir = new File(directory);
        if (!dir.canWrite() || !dir.isDirectory())
        {
            throw new IllegalArgumentException("Bad directory " + dir);
        }

        sce.getServletContext().setAttribute(PARAM_BOX, dir);
    }

    public void contextDestroyed(ServletContextEvent sce) {

    }
}
