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
package edu.umiacs.ace.json;

import java.io.Serializable;
import java.util.List;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * representation of ace report json
 * @author toaster
 */
public class ReportBean implements Serializable
{

    private String collection;
    private int count;
    private long next;
    private List<AceItem> entries;

    /**
     * @return the collection
     */
    public String getCollection()
    {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection)
    {
        this.collection = collection;
    }

    /**
     * @return the count
     */
    public int getCount()
    {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count)
    {
        this.count = count;
    }

    /**
     * @return the next
     */
    public long getNext()
    {
        return next;
    }

    /**
     * @param next the next to set
     */
    @JsonDeserialize(using =CustomLongDeserializer.class)
    public void setNext(long next)
    {
        this.next = next;
    }

    /**
     * @return the entries
     */
    public List<AceItem> getEntries()
    {
        return entries;
    }

    /**
     * @param entries the entries to set
     */
    public void setEntries(List<AceItem> entries)
    {
        this.entries = entries;
    }
}
