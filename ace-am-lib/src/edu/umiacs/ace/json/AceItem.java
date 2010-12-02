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
