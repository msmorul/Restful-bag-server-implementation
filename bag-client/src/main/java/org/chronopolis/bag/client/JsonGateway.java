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
package org.chronopolis.bag.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author toaster
 */
public class JsonGateway {

    private static final Logger LOG = Logger.getLogger(JsonGateway.class);
    private final ObjectMapper mapper;
    private URL endpoint;

    public JsonGateway(URL endpoint) {
        this.endpoint = endpoint;
        mapper = new ObjectMapper();
        DeserializationConfig cfg = mapper.getDeserializationConfig();
    }

    public URL getEndpoint() {
        return endpoint;
    }

    public Manifest getManifest(String identifier) {
        try {
            URL u = new URL(endpoint + "/" + identifier + "/manifest");
            return mapper.readValue(u, Manifest.class);
        } catch (IOException ex) {
            LOG.error(ex);
            if (ex instanceof FileNotFoundException) {
                return null;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }

    public BagList listBags() throws IOException {
//        URL u = new URL(endpoint);
        return mapper.readValue(endpoint, BagList.class);
    }
}
