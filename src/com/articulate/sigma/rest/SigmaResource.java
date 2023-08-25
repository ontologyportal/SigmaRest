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
import com.articulate.sigma.utils.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.json.simple.JSONAware;
import org.json.simple.JSONValue;
import tptp_parser.TPTPFormula;

@Path("/")
public class SigmaResource {
    public static KB kb = null;

    /*****************************************************************
     */
    @Path("init")
    @GET
    public Response init() {
        KBmanager.getMgr().initializeOnce();
        try {
            SigmaResource.kb = KBmanager.getMgr().getKB("SUMO");
        } catch (Exception e) {
            System.out.println(e.toString());
            return Response.serverError().entity(e.toString()).build();
        }
        return Response.ok("Sigma init completed").build();
    }

    @Path("reset")
    @GET
    public Response reset() {
        try {
            KB kb = SigmaResource.kb;
            kb.deleteUserAssertionsAndReload();
        } catch (Exception e) {
            System.out.println(e.toString());
            return Response.serverError().entity(e.toString()).build();
        }
        return Response.ok("Sigma reset completed").build();
    }

    @Path("ask")
    @GET
    @Produces("application/json")
    public Response query(
            @DefaultValue("(subclass ?X Object)") @QueryParam("query") String query,
            @DefaultValue("30") @QueryParam("timeout") int timeout) {
        KB kb = SigmaResource.kb;
        long start = System.currentTimeMillis();
        TPTP3ProofProcessor tpp = null;
        kb.loadVampire();
        Vampire vamp = kb.askVampire(query, timeout, 10);
        tpp = new TPTP3ProofProcessor();
        tpp.parseProofOutput(vamp.output, query, kb, vamp.qlist);
        long end = System.currentTimeMillis();
        double durationS = (end - start) / 1000.0;

        String tor = "{\"bindings\": " + this.toJSON(tpp.bindingMap)
                + ", \"proof\": " + this.toJSON(tpp.proof)
                + ", \"time\": " + durationS;
        if (durationS >= timeout) {
            tor += ", \"error\": \"timeout\"}";
            return Response.serverError().entity(tor).build();
        }
        tor += "}";
        return Response.ok(tor).build();
    }

    @Path("tell")
    @GET
    public Response tell(
            @DefaultValue("Object") @QueryParam("statement") String statement) {
        KB kb = SigmaResource.kb;
        String resp = kb.tell(statement);
        return Response.ok(resp).build();
    }

    private String toJSON(Set<String> data) {
        String tor = "[";
        for (String d : data) {
            if (tor.length() > 1)
                tor += ", ";
            tor += d;
        }
        tor += "]";
        return tor;
    }

    private <T extends Object> String toJSON(List<T> data) {
        String tor = "[";
        for (T d : data) {
            if (tor.length() > 1)
                tor += ", ";
            tor += "\"" + d.toString() + "\"";
        }
        tor += "]";
        return tor;
    }

    private String toJSON(Map<String, String> data) {
        String tor = "{";
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (tor.length() > 1)
                tor += ", ";
            tor += "\"" + key + "\": " + "\"" + value + "\"";
        }
        tor += "}";
        return tor;
    }

    // ###############################################################
    // NOT UPDATED TO USE JSON
    // ###############################################################

    /*****************************************************************
     */
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
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return crunchifyBuilder.toString();
        // return "Rest Never Sleeps";
    }

    /*****************************************************************
     */
    @Path("term")
    @GET
    public Response term(
            @DefaultValue("Object") @QueryParam("term") String term) {

        if (!kb.containsTerm(term))
            return Response.status(200).entity("no such term in KB: " + term).build();
        Set<String> response = KBmanager.getMgr().getKB("SUMO").kbCache.getChildClasses(term);
        if (response == null)
            return Response.status(200).entity("no results for term: " + term).build();
        return Response.status(200).entity(response.toString()).build();
    }

    /*****************************************************************
     */
    @Path("getAllSub")
    @GET
    public Response getAllSub(
            @DefaultValue("Object") @QueryParam("term") String term,
            @DefaultValue("subclass") @QueryParam("rel") String rel) {

        if (!kb.containsTerm(term))
            return Response.status(200).entity("no such term in KB: " + term).build();
        Set<String> response = kb.kbCache.getChildTerms(term, rel);
        if (response == null)
            return Response.status(200).entity("no results for term: " + term).build();
        return Response.status(200).entity(response.toString()).build();
    }

    /*****************************************************************
     */
    @Path("getNearestTerms")
    @GET
    public Response getNearestTerms(
            @DefaultValue("Object") @QueryParam("term") String term) {

        ArrayList<String> response = null;
        if (Character.isLowerCase(term.charAt(0)))
            response = kb.getNearestRelations(term);
        else
            response = kb.getNearestNonRelations(term);
        if (response == null)
            return Response.status(200).entity("no results for term: " + term).build();
        return Response.status(200).entity(response.toString()).build();
    }

    /*****************************************************************
     */
    @Path("generateSemNetNeighbors")
    @GET
    public Response generateSemNetNeighbors(
            @DefaultValue("Object") @QueryParam("term") String term,
            @DefaultValue("1") @QueryParam("depth") String depth,
            @DefaultValue("false") @QueryParam("axioms") String axioms) {

        KButilities kbu = new KButilities();
        boolean links = false;
        if (axioms.equals("true"))
            links = true;
        Set<KButilities.GraphArc> ts = kbu.generateSemNetNeighbors(kb, false, true, links, term,
                Integer.parseInt(depth));
        String response = JSONValue.toJSONString(ts).replaceAll("\\{\"", "\n\\{\"");
        if (response == null)
            return Response.status(200).entity("no results for term: " + term).build();
        return Response.status(200).entity(response.toString()).build();
    }

    /*****************************************************************
     */
    @Path("getWords")
    @GET
    public Response getWords(
            @DefaultValue("Object") @QueryParam("term") String term) {

        if (!kb.containsTerm(term))
            return Response.status(200).entity("no such term in KB: " + term).build();
        Collection<String> response = WordNet.wn.getWordsFromTerm(term).keySet();
        if (response == null)
            return Response.status(200).entity("no results for term: " + term).build();
        return Response.status(200).entity(response.toString()).build();
    }

    /*****************************************************************
     */
    @Path("wsd")
    @GET
    public Response wsd(
            @DefaultValue("Object") @QueryParam("term") String term,
            @DefaultValue("") @QueryParam("sentence") String sent) {

        List<String> words = Arrays.asList(sent.split(" "));
        String candidateSynset = WSD.findWordSenseInContext(term, words);
        return Response.status(200).entity(candidateSynset).build();
    }

    /*****************************************************************
     */
    @Path("initnlp")
    @GET
    public Response initnlp() {

        return Response.status(200).entity("SigmaNLP init completed").build();
    }

    /*****************************************************************
     */
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
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return Response.status(200).entity(crunchifyBuilder.toString()).build();
    }

}