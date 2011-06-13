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
import java.lang.reflect.Type;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.chronopolis.restserver.BagDescriptionDTO.Link;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

/**
 *
 * @author toaster
 */
@Provider
public class BagDescriptionMessageWriter implements MessageBodyWriter {

    private JsonFactory jsonFactory = new JsonFactory();
    private Class clazz = BagDescriptionDTO.class;

    @Override
    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
//        boolean isWritable = false;
        return clazz.isAssignableFrom(type);
//        if (clazz.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
//            ParameterizedType paramType = (ParameterizedType) genericType;
//            Type[] actualArgs = (paramType.getActualTypeArguments());
//            isWritable = (actualArgs.length == 1 && actualArgs[0].equals(clazz));
//        }
//
//        if (clazz.isAssignableFrom(type)) {
//            isWritable = true;
//        }
//
//        return isWritable;
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
        BagDescriptionDTO dto = (BagDescriptionDTO) t;
        jg.writeStartObject();

        if (dto.getBagit() != null) {
            jg.writeObjectFieldStart("bagit");
            jg.writeStringField("BagIt-Version", dto.getBagit().getVersion());
            jg.writeStringField("Tag-File-Character-Encoding", dto.getBagit().getEncoding());
            jg.writeEndObject();
        }

        if (dto.getInfo() != null && dto.getInfo().getBagAttributes() != null) {
            jg.writeArrayFieldStart("info");
            //TODO: is this wrong in the spec?
            for (Map.Entry<String,String> e : dto.getInfo().getBagAttributes().entrySet())
            {
                jg.writeStartArray();
                jg.writeString(e.getKey());
                jg.writeString(e.getValue());
                jg.writeEndArray();
            }
            jg.writeEndArray();
        }

        if (dto.getLinks() != null) {
            jg.writeArrayFieldStart("links");
            for (Link l : dto.getLinks()) {
                jg.writeStartObject();
                jg.writeStringField("href", l.getHref());
                jg.writeStringField("rel", l.getRel());
                jg.writeStringField("type", l.getType());
                jg.writeEndObject();
            }
            jg.writeEndArray();
        }

        jg.writeEndObject();
        jg.close();

    }
}
