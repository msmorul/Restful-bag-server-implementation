/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author toaster
 */
@Provider
public class ManifestMessageWriter implements MessageBodyWriter {

    private JsonFactory jsonFactory = new JsonFactory();
    private Class clazz = ManifestBuilder.class;

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return clazz.isAssignableFrom(type);
    }

    @Override
    public long getSize(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object t, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders,
            OutputStream os) throws IOException, WebApplicationException {
        JsonGenerator jg;
        jg = jsonFactory.createJsonGenerator(os, JsonEncoding.UTF8);
        ManifestBuilder dto = (ManifestBuilder) t;
        jg.writeStartObject();

        // tag file writing
        if (!dto.getTagFiles().isEmpty()) {
            jg.writeArrayFieldStart("tag");
            for (String tagFile : dto.getTagFiles()) {
                jg.writeStartObject();
                jg.writeStringField("path", tagFile);
                jg.writeEndObject();
            }
            jg.writeEndArray();
        }

        // manifest
        if (!dto.getPayloadPaths().isEmpty()) {
            jg.writeArrayFieldStart("payload");
            for (String path : dto.getPayloadPaths()) {
                jg.writeStartObject();
                //checksum
                jg.writeObjectFieldStart("checksum");
                for (String alg : dto.listAlgorithms()) {
                    String dig;
                    if ((dig = dto.getDigest(path, alg)) != null) {
                        jg.writeStringField(alg, dig);
                    }
                }
                jg.writeEndObject();
                //path
                jg.writeStringField("path", path);
                jg.writeEndObject();
            }
            jg.writeEndArray();
        }

        // end object
        jg.writeEndObject();
        jg.close();

    }
}
