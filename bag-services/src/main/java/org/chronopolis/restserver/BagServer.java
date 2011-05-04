/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
     * //TODO invokeBagAction
     * @param bagId
     */
    @Path("{bagid}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    public Response invokeBagAction(@PathParam("bagid") String bagId,
            @FormParam("commit") String commit,
            @FormParam("validate") String validate,
            @Context ServletContext servletCtx) {

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (commit != null && !commit.isEmpty())
        {
            if (be.isComplete() && be.commit())
            {
                return Response.ok().build();
            }
            else
            {
                LOG.info("Could not commit bag: " + bagId);
                return Response.serverError().build();
            }
        }
        else if (validate != null && !validate.isEmpty())
        {
            //TODO: validation
        }


        return Response.ok().build();
    }

    /**
     * Return links, parsed bag-info.txt, and parsed bagit.txt from spec
     * TODO: getBagDescription
     * @param bagId bag to query
     */
    @Path("{bagid}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getBagDescription(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    @Path("{bagid}/data/{dataFile}")
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
    @Path("{bagid}/contents/data/{dataFile}")
    @PUT
    public Response putBagData(@PathParam("bagid") String bagId,
            @PathParam("dataFile") String dataFile,
            @Context HttpServletRequest request,
            @Context ServletContext servletCtx) {
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
    public Response getMetadataFile(@PathParam("bagid") String bagId,
            @PathParam("contentFile") String contentFile,
            @Context ServletContext servletCtx) {
        LOG.debug("Retrieve metadata file: " + contentFile + " to bag " + bagId);

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            LOG.info("Request for unknown bag: " + bagId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        InputStream is = be.openTagStream(contentFile);
        return Response.ok(is).build();

    }

    @Path("{bagid}/contents/{contentFile}")
    @Consumes(MediaType.TEXT_PLAIN)
    @PUT
    public Response putMetadataFile(@PathParam("bagid") String bagId,
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
        } else {
            //TODO: parse other files
        }

        return Response.ok().build();
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
     * Return combined manifest describing items in the collection
     * TODO getBagManifest
     * @param bagId
     * @param servletCtx
     * @return
     */
    @GET
    @Path("{bagid}/manifest")
    public Response getBagManifest(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

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
