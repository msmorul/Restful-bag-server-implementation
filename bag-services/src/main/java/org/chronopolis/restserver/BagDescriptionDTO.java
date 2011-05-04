/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import javax.xml.bind.annotation.XmlRootElement;
import org.chronopolis.bagserver.BagInfo;
import org.chronopolis.bagserver.BagIt;

/**
 * Response for a GET to /bags/BAG_ID
 *
 *
 * @author toaster
 */
@XmlRootElement
public class BagDescriptionDTO {

    private BagInfo bagInfo;
    private BagIt bagit;
    private Link[] links = {new Link("copies", ""), new Link("manifest", "index"),
        new Link("metadata", "related"), new Link("notes", "")};

    public BagInfo getInfo() {
        return bagInfo;
    }

    public BagIt getBagit() {
        return bagit;
    }

    public Link[] getLinks() {
        return links;
    }

    public void setInfo(BagInfo bagInfo) {
        this.bagInfo = bagInfo;
    }

    public void setBagit(BagIt bagit) {
        this.bagit = bagit;
    }

    public static class Link {

        private String href;
        private String rel;
        private String type = "application/json";

        public String getHref() {
            return href;
        }

        public String getRel() {
            return rel;
        }

        public String getType() {
            return type;
        }

        public Link(String href, String rel) {
            this.href = href;
            this.rel = rel;
        }
    }
}
