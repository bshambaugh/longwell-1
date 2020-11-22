/*
 * Created on Jun 8, 2004
 */
package edu.mit.simile.longwell.model.joseki;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.joseki.QueryEngineHTTP;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.vocabularies.Display;

/**
 * @author ryanlee
 */
public class JosekiRDQLModel extends RemoteFacetModel {
    private String _josekiURI;
    
    public JosekiRDQLModel(Model model, String indexPath, String josekiURI) throws Exception {
		super(model, indexPath);
		this._josekiURI = josekiURI;
    }
    
    protected void createCachedModel(BrowserConfig bc) throws Exception {
	    // get the labels
        String labelQueryString = "SELECT ?a, ?b WHERE ( ?a, <" + RDFS.label.getURI() + ">, ?b)";
        Query q = new Query(labelQueryString);
        QueryExecution qe = new QueryEngineHTTP(q, this._josekiURI);
        QueryResults results = qe.exec();
        for (Iterator iter = results; iter.hasNext(); ) {
        		ResultBinding rbind = (ResultBinding) iter.next();
        		Object objA = rbind.get("a");
        		Object objB = rbind.get("b");
        		this._cachedModel.add((Resource) objA, RDFS.label, (RDFNode) objB);
        }
        results.close();
        
	    // get the subclasses
        String subclassQueryString = "SELECT ?a, ?b WHERE (?a, <" + RDFS.subClassOf.getURI() + ">, ?b)";
        q = new Query(subclassQueryString);
        qe = new QueryEngineHTTP(q, this._josekiURI);
        results = qe.exec();
        for (Iterator iter = results; iter.hasNext(); ) {
        		ResultBinding rbind = (ResultBinding) iter.next();
        		Object objA = rbind.get("a");
        		Object objB = rbind.get("b");
        		this._cachedModel.add((Resource) objA, RDFS.subClassOf, (RDFNode) objB);
        }
        results.close();

        // get all objects of registered types
        // for each configured object type
        Iterator j = bc.getConfiguredClasses().iterator();

        while (j.hasNext()) {
            String queryString = "SELECT ?a, ?b, ?c WHERE (?a, rdf:type, <" + ((String) j.next()) +
                ">), (?a, ?b, ?c)";
            q = new Query(queryString);
            qe = new QueryEngineHTTP(q, this._josekiURI);
            results = qe.exec();
            for (Iterator iter = results; iter.hasNext(); ) {
            		ResultBinding rbind = (ResultBinding) iter.next();
            		Object objA = rbind.get("a");
            		Object objB = rbind.get("b");
            		Object objC = rbind.get("c");
            		this._cachedModel.add((Resource) objA, ResourceFactory.createProperty(((Resource) objB).getURI()), (RDFNode) objC);
            }
            results.close();
        }

        // for each configured object type
        Iterator i = bc.getConfiguredClasses().iterator();

        while (i.hasNext()) {
            Resource t = this._cachedModel.getResource((String) i.next());

            // for each data object in the model of that type
            ResIterator objectIter = this._cachedModel.listSubjectsWithProperty(RDF.type, t);

            while (objectIter.hasNext()) {
                Resource r = objectIter.nextResource();
                this._objects.add(r.getURI());
            }
        }
    }
	
    /**
     * This creates a list of equivalent terms so we can hide them in the UI.
     */
   public void createPreferredTerms() {
       String queryString = "SELECT ?a WHERE (?a, <" + RDF.type.getURI() +
       	">, <" + Display.PreferredTerm.getURI() + ">)";
       Query q = new Query(queryString);
       QueryExecution qe = new QueryEngineHTTP(q, this._josekiURI);
       QueryResults results = qe.exec();
       for (Iterator iter = results; iter.hasNext(); ) {
       		ResultBinding rbind = (ResultBinding) iter.next();
       		Resource pref = (Resource) rbind.get("a");
            primaryEquivalences.add(pseudoUri(pref));
            // a secondary term is any term that is subject to a sameAs relation
            // but is not a preferred term

            String secQuery = "SELECT ?b WHERE (<" + pref.getURI() + ">, <" + OWL.sameAs.getURI() + ">, ?b)";
            Query secq = new Query(secQuery);
            QueryExecution secqe = new QueryEngineHTTP(secq, this._josekiURI);
            QueryResults secqr = secqe.exec();
            for (Iterator seciter = secqr; seciter.hasNext(); ) {
                ResultBinding sarb = (ResultBinding) seciter.next();
                Resource sec = (Resource) sarb.get("b");
                secondaryEquivalences.add(pseudoUri(sec));
            }
       }
       results.close();
   }
}
