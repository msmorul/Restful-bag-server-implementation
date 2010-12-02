/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
