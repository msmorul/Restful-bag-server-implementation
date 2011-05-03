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
 * //TODO: add links to this message
 *
 * @author toaster
 */
@XmlRootElement
public class BagDescriptionDTO {

    private BagInfo bagInfo;
    private BagIt bagit;

    public BagInfo getBagInfo() {
        return bagInfo;
    }

    public BagIt getBagit() {
        return bagit;
    }
   
}
