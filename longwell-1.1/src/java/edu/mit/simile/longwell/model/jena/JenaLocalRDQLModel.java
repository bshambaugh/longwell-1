package edu.mit.simile.longwell.model.jena;

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.longwell.model.QueryHelper;
import edu.mit.simile.vocabularies.Display;


/**
 * A FacetModel that uses a mixture of RDQL and the Jena API to query the model.
 * 
 * This inherits from the RemoteFacetModel; I guess the name should change.
 *
 * @author Mark H. Butler
 */
public class JenaLocalRDQLModel extends RemoteFacetModel {
	
    /**
     * Creates a new JenaLocalRDQLModel object.
     *
     * @param model The RDF model containing the dataset.
     * @param bc The Browser Configuration.
     */
    public JenaLocalRDQLModel(Model model, String indexPath) throws Exception {
        super(model, indexPath);
    }

    protected void createCachedModel(BrowserConfig bc) throws Exception {
        // get the labels
        String labelQueryString = "SELECT ?a, ?b WHERE ( ?a, <" + RDFS.label.getURI() + ">, ?b)";
        QueryHelper labelQuery = new QueryHelper(model, labelQueryString);
        this._cachedModel = labelQuery.getModel();
        labelQuery.close();

        // get the subclasses
        String subClassQueryString = "SELECT ?a, ?b WHERE (?a, <" + RDFS.subClassOf.getURI() + ">, ?b)";
        QueryHelper subClassQuery = new QueryHelper(model, subClassQueryString);
        this._cachedModel.add(subClassQuery.getModel());
        subClassQuery.close();
        
        // get all objects of registered types
        // for each configured object type
        Iterator j = bc.getConfiguredClasses().iterator();

        while (j.hasNext()) {
            String queryString = "SELECT ?a, ?b, ?c WHERE (?a, rdf:type, <" + ((String) j.next()) + ">), (?a, ?b, ?c)";
            QueryHelper typeQuery = new QueryHelper(model, queryString);
            this._cachedModel.add(typeQuery.getModel());
            typeQuery.close();
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
    
    public void createIndex() {
        // fixme
    }

    /**
      * This creates a list of equivalent terms so we can hide them in the UI.
      */
    public void createPreferredTerms() {
        String queryString = "SELECT ?a, ?b WHERE (?a, <" + OWL.sameAs.getURI() +
            ">, ?b) , (?a, <" + RDF.type.getURI() + ">, <" + Display.PreferredTerm.getURI() +
            ">)";
        QueryHelper preferredTermQuery = new QueryHelper(model, queryString);
        Iterator iter = preferredTermQuery.getResults();

        while (iter.hasNext()) {
            ResultBinding res = (ResultBinding) iter.next();
            primaryEquivalences.add(((Resource) res.get("a")).getURI());
            secondaryEquivalences.add(((Resource) res.get("b")).getURI());
        }

        preferredTermQuery.close();
    }
}
