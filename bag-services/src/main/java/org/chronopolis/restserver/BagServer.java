/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.chronopolis.restserver;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.chronopolis.bagserver.BagEntry;
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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createBag(@FormParam("id") String newBagId,
            //@Context SecurityContext securityCtx,
            @Context ServletContext servletCtx) {

        LOG.debug("Create bag " + newBagId);

        BagVault vault = getVault(servletCtx);
        if (vault.bagExists(newBagId)) {
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

        BagVault vault = getVault(servletCtx);
        BagEntry be = vault.getBag(bagId);
        if (be == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().build();

    }

    /**
     * handler for validation and commit actions
     * @param bagId
     */
    @Path("{bagid}")
    @POST
    public void invokeBagAction(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
//        BagEntry be = vault.getBag(bagId);
//        if (be == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
    }

    /**
     * Return links, parsed bag-info.txt, and parsed bagit.txt
     * 
     * @param bagId bag to query
     */
    @Path("{bagid}")
    @GET
    public Response getBagDescription(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    @Path("{bagid}/copies")
    @GET
    public Response getBagCopies(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

    @GET
    @Path("{bagid}/notes")
    public Response getBagNotes(@PathParam("bagid") String bagId,
            @Context ServletContext servletCtx) {
        return null;
    }

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
