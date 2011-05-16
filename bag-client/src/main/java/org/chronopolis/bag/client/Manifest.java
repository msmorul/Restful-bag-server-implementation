/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.chronopolis.bag.client;

import java.util.List;
import java.util.Map;

/**
 *
 * @author toaster
 */
public class Manifest {

    private List<TagFile> tag;
    private List<PayloadItem> payload;

    public void setPayload(List<PayloadItem> payload) {
        this.payload = payload;
    }

    public void setTag(List<TagFile> tag) {
        this.tag = tag;
    }

    public List<PayloadItem> getPayload() {
        return payload;
    }

    public List<TagFile> getTag() {
        return tag;
    }
    
    public static class TagFile
    {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
        
    }

    public static class PayloadItem {

        private Map<String,String> checksum;
        private String path;

        public void setPath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Map<String, String> getChecksum() {
            return checksum;
        }

        public void setChecksum(Map<String, String> checksum) {
            this.checksum = checksum;
        }


    }
}
