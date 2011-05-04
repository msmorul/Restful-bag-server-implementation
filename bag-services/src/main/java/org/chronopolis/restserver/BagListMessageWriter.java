/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.chronopolis.bagserver.BagEntry;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author toaster
 */
@Provider
public class BagListMessageWriter implements MessageBodyWriter {

    private static final Logger LOG = Logger.getLogger(BagListMessageWriter.class);
    private JsonFactory jsonFactory = new JsonFactory();
//    private Class clazz = ArrayList.class;

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        boolean isWritable = false;
        LOG.debug("Type: " + type + " generics " + (genericType instanceof ParameterizedType));
        if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] actualArgs = (paramType.getActualTypeArguments());
            isWritable = (actualArgs.length == 1 && actualArgs[0].equals(BagEntry.class));
        }

        return isWritable && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
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
        List<BagEntry> entrylist = (List<BagEntry>) t;
        jg.writeStartObject();

        if (entrylist.size() > 0) {
            jg.writeArrayFieldStart("objects");
            for (BagEntry be : entrylist) {
                jg.writeStartObject();
                jg.writeStringField("href", be.getIdentifier());
                jg.writeStringField("id", be.getIdentifier());
                jg.writeEndObject();

            }
            jg.writeEndArray();
        }
        jg.writeObjectFieldStart("pagination");
        jg.writeNumberField("limit", entrylist.size());
        jg.writeNumberField("offset", 0);
        jg.writeNumberField("total_count", entrylist.size());
        jg.writeEndObject();

        // doc
        jg.writeEndObject();
        jg.close();

    }
}
