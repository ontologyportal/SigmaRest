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
        Set<String> response = kb.kbCache.getChildTerms(term,rel);
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
        Set<KButilities.GraphArc> ts = kbu.generateSemNetNeighbors(kb,false,true,links,term,Integer.parseInt(depth));
        String response = JSONValue.toJSONString(ts).replaceAll("\\{\"","\n\\{\"");
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
    @Path("query")
    @GET
    public Response query(
            @DefaultValue("(subclass ?X Object)") @QueryParam("query") String query,
            @DefaultValue("30") @QueryParam("timeout") int timeout) {

        com.articulate.sigma.trans.TPTP3ProofProcessor tpp = null;
        kb.loadVampire();
        Vampire vamp = kb.askVampire(query, timeout, 1);
        if (vamp == null)
            return Response.status(200).entity("no results or error").build();
        System.out.println("KB.main(): completed query with result: " + StringUtil.arrayListToCRLFString(vamp.output));
        tpp = new TPTP3ProofProcessor();
        tpp.parseProofOutput(vamp.output, query, kb, vamp.qlist);
        StringBuffer proofsb = new StringBuffer();
        for (TPTPFormula form : tpp.proof) {
            proofsb.append(form.toString() + "\n");
        }
        return Response.status(200).entity(tpp.bindingMap + "\n\n" + proofsb.toString()).build();
    }

    /*****************************************************************
     */
    @Path("tell")
    @GET
    public Response tell(
            @DefaultValue("(instance Foo Object)") @QueryParam("form") String form) {

        String res = kb.tell(form);
        return Response.status(200).entity(res).build();
    }

    /*****************************************************************
     */
    @Path("init")
    @GET
    public Response init() {

        KBmanager.getMgr().initializeOnce();
        kb = KBmanager.getMgr().getKB("SUMO");
        return Response.status(200).entity("Sigma init completed").build();
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
        }
        catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        System.out.println("Data Received: " + crunchifyBuilder.toString());

        // return HTTP response 200 in case of success
        return Response.status(200).entity(crunchifyBuilder.toString()).build();
    }

}