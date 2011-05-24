/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
