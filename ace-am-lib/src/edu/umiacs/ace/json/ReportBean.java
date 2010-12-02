/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
