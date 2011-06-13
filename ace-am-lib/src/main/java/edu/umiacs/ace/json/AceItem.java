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

import java.util.Date;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class AceItem {

    private long id;
    private char state;
    private boolean directory;
    private String path;
    private String parentPath;
    private Date lastSeen;
    private Date stateChange;
    private Date lastVisited;
    private String fileDigest;
    private long size;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @JsonDeserialize(using = CustomLongDeserializer.class)
    public void setId(long id) {
        this.id = id;
    }

    @JsonDeserialize(using = CustomLongDeserializer.class)
    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    /**
     * @return the state
     */
    public char getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    @JsonDeserialize(using = CustomCharDeserializer.class)
    public void setState(char state) {
        this.state = state;
    }

    /**
     * @return the directory
     */
    public boolean isDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the parentPath
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * @param parentPath the parentPath to set
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * @return the lastSeen
     */
    public Date getLastSeen() {
        return lastSeen;
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * @return the stateChange
     */
    public Date getStateChange() {
        return stateChange;
    }

    /**
     * @param stateChange the stateChange to set
     */
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public void setStateChange(Date stateChange) {
        this.stateChange = stateChange;
    }

    /**
     * @return the lastVisited
     */
    public Date getLastVisited() {
        return lastVisited;
    }

    /**
     * @param lastVisited the lastVisited to set
     */
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public void setLastVisited(Date lastVisited) {
        this.lastVisited = lastVisited;
    }

    /**
     * @return the fileDigest
     */
    public String getFileDigest() {
        return fileDigest;
    }

    /**
     * @param fileDigest the fileDigest to set
     */
    public void setFileDigest(String fileDigest) {
        this.fileDigest = fileDigest;
    }

    @Override
    public String toString() {
        return path + " " + state + " " + fileDigest + " " + size;
    }


}
