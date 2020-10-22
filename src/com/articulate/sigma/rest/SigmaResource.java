package com.articulate.sigma.rest;

/*
http://localhost:8080/sigmarest/resources/helloworld
 */
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.articulate.sigma.*;
import com.articulate.sigma.trans.TPTP3ProofProcessor;
import com.articulate.sigma.tp.Vampire;
import com.articulate.sigma.wordNet.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Path("/")
public class SigmaResource {

    KB kb = null;

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

    @Path("getAllSub")
    @GET
    public Response getAllSub(

            @DefaultValue("Object") @QueryParam("term") String term,
            @DefaultValue("subclass") @QueryParam("rel") String rel) {
        HashSet<String> response = KBmanager.getMgr().getKB("SUMO").kbCache.getChildTerms(term,rel);
        return Response.status(200).entity(response.toString()).build();
    }

    @Path("getWords")
    @GET
    public Response getWords(

            @DefaultValue("Object") @QueryParam("term") String term) {
        Collection<String> response = WordNet.wn.getWordsFromTerm(term).keySet();
        return Response.status(200).entity(response.toString()).build();
    }

    @Path("wsd")
    @GET
    public Response wsd(
            @DefaultValue("Object") @QueryParam("term") String term,
            @DefaultValue("") @QueryParam("sentence") String sent) {

        List<String> words = Arrays.asList(sent.split(" "));
        String candidateSynset = WSD.findWordSenseInContext(term, words);
        return Response.status(200).entity(candidateSynset).build();
    }

    @Path("query")
    @GET
    public Response query(
            @DefaultValue("(subclass ?X Object)") @QueryParam("query") String query,
            @DefaultValue("30") @QueryParam("timeout") int timeout) {

        com.articulate.sigma.trans.TPTP3ProofProcessor tpp = null;
        kb = KBmanager.getMgr().getKB("SUMO");
        kb.loadVampire();
        Vampire vamp = kb.askVampire(query, timeout, 1);
        System.out.println("KB.main(): completed query with result: " + StringUtil.arrayListToCRLFString(vamp.output));
        tpp = new TPTP3ProofProcessor();
        tpp.parseProofOutput(vamp.output, query, kb);
        return Response.status(200).entity(tpp.bindings + "\n\n" + tpp.proof).build();
    }

    @Path("init")
    @GET
    public Response init() {

        KBmanager.getMgr().initializeOnce();
        return Response.status(200).entity("Sigma init completed").build();
    }

    @Path("initnlp")
    @GET
    public Response initnlp() {

        return Response.status(200).entity("SigmaNLP init completed").build();
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