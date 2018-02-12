package com.articulate.sigma.rest;

/*
http://localhost:8080/sigmarest/resources/helloworld
 */
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.articulate.sigma.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

@Path("/")
public class SigmaResource {

    @GET
    @Path("helloworld")
    @Produces("text/plain")
    public String getMessage(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
        }
        catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return crunchifyBuilder.toString();
        //return "Rest Never Sleeps";
    }

    @Path("term")
    @GET
    public Response term(
            @DefaultValue("Object") @QueryParam("term") String term) {
        HashSet<String> response = KBmanager.getMgr().getKB("SUMO").kbCache.getChildClasses(term);
        return Response.status(200).entity(response.toString()).build();
    }

    @Path("init")
    @GET
    public Response init() {

        KBmanager.getMgr().initializeOnce();
        return Response.status(200).entity("Sigma init completed").build();
    }

    @POST
    @Path("posting")
    public Response posting(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
        }
        catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return Response.status(200).entity(crunchifyBuilder.toString()).build();
    }

}