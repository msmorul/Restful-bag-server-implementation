/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.chronopolis.bagserver.BagEntry;
import org.chronopolis.bagserver.BagEntry.State;
import org.chronopolis.bagserver.BagInfo;
import org.chronopolis.bagserver.BagIt;
import org.chronopolis.bagserver.BagVault;

/**
 *
 * @author toaster
 */
@Path("bags")
public class BagServer {

    public static final String VAULT = "org.chronopolis.BagVault";
    private static final Logger LOG = Logger.getLogger(BagServer.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<BagEntry> getBagList(@Context ServletContext servletCtx) {
        LOG.debug("list bags ");

        BagVault vault = getVault(servletCtx);
        List<BagEntry> entryList = new ArrayList<BagEntry>();

        for (BagEntry be : vault.getBags()) {
            if (be.getBagState() == State.COMMITTED) {
                entryList.add(be);
            }
        }

        return entryList;
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createBag(@FormParam("id") String newBagId,
            @Context ServletContext servletCtx) {

        LOG.debug("Create bag " + newBagId);

        BagVault vault = getVault(servletCtx);
        if (vault.bagExists(newBagId)) {
            LOG.info("Request for existing bag: " + newBagId);
            return Response.status(409).build();
        }

        vault.createNewBag(newBagId);

        return Response.ok().build();
    }

    /**
     * Remove a bag at the specified location
     * @param bagId
     */
    @Path("{bagid}")
    @DELETE
    public Response removeBag(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {

        LOG.debug("Remove bag " + bagId);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);

            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (be.delete()) {
            LOG.trace("Bag Successfully removed " + bagId);
            return Response.ok().build();

        } else {
            LOG.error("Count not remove " + bagId);
            return Response.serverError().build();
        }
    }

    /**
     * handler for validation and commit actions
     * if commit or validate are not null, then those actions are invoked. Should this be tightened per spec?
     * //TODO invokeBagAction validation
     * @param bagId
     */
    @Path("{bagid}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    public Response invokeBagAction(@PathParam("bagid") String bagId,
            @FormParam("commit") String commit,
            @FormParam("validate") String validate,
            @Context ServletContext servletCtx) {

        LOG.debug("Request for commit/validate on " + bagId
                + " Commit=" + commit + ", Validate=" + validate);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (commit != null && !commit.isEmpty()) {
            if (be.isComplete() && be.commit()) {
                return Response.ok().build();
            } else {
                LOG.info("Could not commit bag: " + bagId);
                // per spec, 400 to clients on incomplete bags
                return Response.status(400).build();
            }
        } else if (validate != null && !validate.isEmpty()) {
            //TODO: validation
        }


        return Response.ok().build();
    }

    /**
     * Return links, parsed bag-info.txt, and parsed bagit.txt from spec
     * @param bagId bag to query
     */
    @Path("{bagid}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getBagDescription(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        LOG.debug("metadata request for " + bagId);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        BagDescriptionDTO dto = new BagDescriptionDTO();
        dto.setInfo(be.getBagInfo());
        dto.setBagit(be.getBagIt());
        return Response.ok(dto).build();

    }

    @Path("{bagid}/contents/data/{dataFile: .*}")
    @GET
    public Response getBagData(@PathParam("bagid") String bagId,
            @PathParam("dataFile") String dataFile,
            @Context ServletContext servletCtx) {
        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        InputStream is = be.openDataInputStream(dataFile);
        if (is == null) {
            return Response.status(404).build();
        }
        return Response.ok(is).build();
    }

    /**
     * Retrieve data from the specified bag
     * @param bagId bag to open
     * @param dataFile data file in bag
     * @param request
     * @param servletCtx
     * @return
     */
    @Path("{bagid}/contents/data/{dataFile: .*}")
    @PUT
    public Response putBagData(@PathParam("bagid") String bagId,
            @PathParam("dataFile") String dataFile,
            @Context HttpServletRequest request,
            @Context ServletContext servletCtx) {
        LOG.trace("Upload file: " + bagId + " " + dataFile);
        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        byte[] block = new byte[32768];
        OutputStream os = be.openDataOutputStream(dataFile);
        if (os == null) {
            return Response.serverError().build();
        }

        try {
            InputStream is = request.getInputStream();
            int read;
            while ((read = is.read(block)) != -1) {
                os.write(block, 0, read);
            }
            os.close();
            LOG.trace("Finish file upload: " + bagId + " " + dataFile);
        } catch (IOException e) {
            LOG.error("Error writing file " + bagId + ": " + dataFile);
            throw new WebApplicationException(e);
        }
        return Response.ok().build();
    }

    /**
     * Handle the retrieval of tag files. 
     * @param bagId
     * @param contentFile
     * @param servletCtx
     * @param request
     * @return
     */
    @Path("{bagid}/contents/{contentFile}")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public Response getTagFile(@PathParam("bagid") String bagId,
            @PathParam("contentFile") String contentFile,
            @Context ServletContext servletCtx) {
        LOG.debug("Retrieve metadata file: " + contentFile + " to bag " + bagId);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream is = be.openTagInputStream(contentFile);
        return Response.ok(is).build();

    }

    @Path("{bagid}/contents/{contentFile}")
    @Consumes(MediaType.TEXT_PLAIN)
    @PUT
    public Response putTagFile(@PathParam("bagid") String bagId,
            @PathParam("contentFile") String contentFile,
            @Context ServletContext servletCtx,
            @Context HttpServletRequest request) {
        LOG.debug("Uploading metadata file: " + contentFile + " to bag " + bagId);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        //BAGIT file
        if (contentFile.toLowerCase().equals("bagit.txt")) {
            LOG.trace("Parsing bagit.txt");
            try {
                BagIt bagit = BagIt.readFile(new InputStreamReader(request.getInputStream()));
                if (!be.setBagItInformation(bagit)) {
                    return Response.serverError().build();
                }
            } catch (IOException e) {
                LOG.error(e);
                throw new WebApplicationException(e);
            }
            // BAGINFO
        } else if (contentFile.toLowerCase().equals("bag-info.txt")) {
            LOG.trace("Parsing baginfo.txt");
            try {
                BagInfo bagInfo = BagInfo.readInfo(new InputStreamReader(request.getInputStream()));
                if (!be.setBagInfo(bagInfo)) {
                    return Response.serverError().build();
                }
            } catch (IOException e) {
                LOG.error(e);
                throw new WebApplicationException(e);
            }
            // ALL OTHER TAG FILES
        } else {
            byte[] block = new byte[32768];
            OutputStream os = be.openTagOutputStream(contentFile);
            if (os == null) {
                return Response.serverError().build();
            }

            try {
                InputStream is = request.getInputStream();
                int read;
                while ((read = is.read(block)) != -1) {
                    os.write(block, 0, read);
                }
                os.close();
            } catch (IOException e) {
                LOG.error("Error writing file " + bagId + ": " + contentFile);
                throw new WebApplicationException(e);
            }
        }

        return Response.ok().build();
    }

    /**
     * Return combined manifest describing items in the collection
     * @param bagId
     * @param servletCtx
     * @return
     */
    @GET
    @Path("{bagid}/manifest")
    public Response getBagManifest(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);

        LOG.debug("manifest request for " + bagId);

        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<String> tagList = be.listTagFiles();
        ManifestBuilder mb = new ManifestBuilder();

        for (String s : tagList) {
            try {
                LOG.trace("Adding tagfile to manifest: " + s);
                mb.parseTagFile(s, be);
            } catch (IOException e) {
                LOG.error("Errror reading, " + s, e);
                return Response.serverError().build();
            }
        }

        return Response.ok(mb).build();

    }

    /**
     * TODO: implement once spec has this listed
     * @param bagId
     * @param servletCtx
     * @return
     */
    @Path("{bagid}/copies")
    @GET
    public Response getBagCopies(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    /**
     * TODO: implement once spec has description
     * @param bagId
     * @param servletCtx
     * @return
     */
    @GET
    @Path("{bagid}/notes")
    public Response getBagNotes(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    /**
     * TODO getBagMetadata
     * @param bagId
     * @param servletCtx
     * @return
     */
    @GET
    @Path("{bagid}/metadata")
    public Response getBagMetadata(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    private BagVault getVault(ServletContext ctx) {
        return (BagVault) ctx.getAttribute(VAULT);
    }
}
