/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.ingest.manager;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

/**
 *
 * @author toaster
 */
public class JettyRequest {

    private Request request;
    private Response response;

    public JettyRequest(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
