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
