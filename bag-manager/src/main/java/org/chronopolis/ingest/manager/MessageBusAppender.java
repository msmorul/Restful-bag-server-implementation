/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.manager;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.pivot.util.MessageBus;

/**
 * log4j appender which will place all directed messaged into the messagebus for
 * pivot to consume.
 * 
 * @author toaster
 */
public class MessageBusAppender extends AppenderSkeleton {
    
    @Override
    protected void append(LoggingEvent event) {
        MessageBus.sendMessage(new LogMessage(layout, event));
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    public void close() {
       //nop
    }

}
