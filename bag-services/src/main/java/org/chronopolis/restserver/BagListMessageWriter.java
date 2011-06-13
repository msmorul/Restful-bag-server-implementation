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
