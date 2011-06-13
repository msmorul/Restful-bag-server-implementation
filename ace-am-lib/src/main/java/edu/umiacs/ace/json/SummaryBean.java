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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 *
 * @author toaster
 */
public class SummaryBean implements Serializable
{

    private String collection;
    private List<Summary> summaries;

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
     * @return the summaries
     */
    public List<Summary> getSummaries()
    {
        return summaries;
    }

    /**
     * @param summaries the summaries to set
     */
    public void setSummaries(List<Summary> summaries)
    {
        this.summaries = summaries;
    }

    public static class Summary implements Serializable
    {

        private String reportName;
        private long id;
        private long collection;
        private String collectionName;
        private Date start;
        private Date end;
        private CollectionState collectionState;
        private LogSummary logSummary;

        public void setReportName(String reportName)
        {
            this.reportName = reportName;
        }

        public String getReportName()
        {
            return reportName;
        }

        /**
         * @return the id
         */
        public long getId()
        {
            return id;
        }

        /**
         * @param id the id to set
         */
        public void setId(long id)
        {
            this.id = id;
        }

        /**
         * @return the collection
         */
        public long getCollection()
        {
            return collection;
        }

        /**
         * @param collection the collection to set
         */
        public void setCollection(long collection)
        {
            this.collection = collection;
        }

        /**
         * @return the collectionName
         */
        public String getCollectionName()
        {
            return collectionName;
        }

        /**
         * @param collectionName the collectionName to set
         */
        public void setCollectionName(String collectionName)
        {
            this.collectionName = collectionName;
        }

        /**
         * @return the start
         *
         */
        public Date getStart()
        {
            return start;
        }

        /**
         * @param start the start to set
         */
        @JsonDeserialize(using = CustomDateDeserializer.class)
        public void setStart(Date start)
        {
            this.start = start;
        }

        /**
         * @return the end
         */
        public Date getEnd()
        {
            return end;
        }

        /**
         * @param end the end to set
         */
        @JsonDeserialize(using =CustomDateDeserializer.class)
        public void setEnd(Date end)
        {
            this.end = end;
        }

        /**
         * @return the collectionState
         */
        public CollectionState getCollectionState()
        {
            return collectionState;
        }

        /**
         * @param collectionState the collectionState to set
         */
        public void setCollectionState(CollectionState collectionState)
        {
            this.collectionState = collectionState;
        }

        /**
         * @return the logSummary
         */
        public LogSummary getLogSummary()
        {
            return logSummary;
        }

        /**
         * @param logSummary the logSummary to set
         */
        public void setLogSummary(LogSummary logSummary)
        {
            this.logSummary = logSummary;
        }

        public static class CollectionState implements Serializable
        {

            private Map<String, String> itemMap;

            @JsonAnySetter
            public void unknownItem(String name, String value)
            {
                if (itemMap == null)
                {
                    itemMap = new HashMap<String, String>();
                }
                itemMap.put(name, value);
            }

            public void setItemMap(Map<String, String> itemMap)
            {
                this.itemMap = itemMap;
            }

            public Map<String, String> getItemMap()
            {
                return itemMap;
            }

        }

        public static class LogSummary implements Serializable
        {

            private Map<String, String> itemMap;

            @JsonAnySetter
            public void unknownItem(String name, String value)
            {
                if (itemMap == null)
                {
                    itemMap = new HashMap<String, String>();
                }
                itemMap.put(name, value);
            }

            public void setItemMap(Map<String, String> itemMap)
            {
                this.itemMap = itemMap;
            }

            public Map<String, String> getItemMap()
            {
                return itemMap;
            }

        }
    }
}
