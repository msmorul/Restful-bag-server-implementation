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
