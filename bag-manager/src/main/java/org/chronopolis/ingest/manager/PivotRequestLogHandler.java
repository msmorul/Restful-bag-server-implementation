/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.ingest.manager;

import org.apache.pivot.util.MessageBus;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RequestLog;
import org.mortbay.jetty.Response;

/**
 *
 * @author toaster
 */
public class PivotRequestLogHandler extends AbstractLifeCycle implements RequestLog{

    public void log(Request request, Response response) {
        MessageBus.sendMessage(new JettyRequest(request, response));
    }


}
