/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.manager;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author toaster
 */
public class LogMessage {

    private Layout logLayout;
    private LoggingEvent event;

    public LoggingEvent getEvent() {
        return event;
    }

    public Layout getLogLayout() {
        return logLayout;
    }

    public LogMessage(Layout logLayout, LoggingEvent event) {
        this.logLayout = logLayout;
        this.event = event;
    }
}
