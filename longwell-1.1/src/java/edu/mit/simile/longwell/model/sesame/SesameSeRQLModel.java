/*
 * Created on Aug 2, 2004
 */
package edu.mit.simile.longwell.model.sesame;

import java.net.URL;
import java.util.Iterator;

import org.openrdf.model.Value;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.remote.HTTPService;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.vocabularies.Display;


/**
 * @author ryanlee
 */
public class SesameSeRQLModel extends RemoteFacetModel {
    private SesameRepository _repo;
    private String _serverURI, _user, _password, _repository;

    public SesameSeRQLModel(Model model, String indexPath, String serverURI, String repository, String user, String password) throws Exception {
        super(model, indexPath);
        this._serverURI = serverURI;
        this._user = user;
        this._password = password;
        this._repository = repository;
    }
    
    protected void createCachedModel(BrowserConfig bc) throws Exception {
        URL serverURL = new URL(this._serverURI);
        HTTPService service = (HTTPService) Sesame.getService(serverURL);
        service.login(this._user, this._password);
        this._repo = service.getRepository(this._repository);

        String query = "SELECT subject, title FROM {subject} <rdfs:label> {title}";
        QueryResultsTable results = this._repo.performTableQuery(QueryLanguage.SERQL, query);
        int rows = results.getRowCount();
        for (int i = 0; i < rows; i++) {
            Resource a = ResourceFactory.createResource(results.getValue(i, 0).toString());
            RDFNode b = ResourceFactory.createPlainLiteral(results.getValue(i, 1).toString());
            this._cachedModel.add(a, RDFS.label, b);
        }
        
        query = "SELECT subject, superclass FROM {subject} <rdfs:subClassOf> {superclass}";
        results = this._repo.performTableQuery(QueryLanguage.SERQL, query);
        rows = results.getRowCount();
        for (int i = 0; i < rows; i++) {
            Resource a = ResourceFactory.createResource(results.getValue(i, 0).toString());
            Resource b = ResourceFactory.createResource(results.getValue(i, 1).toString());
            this._cachedModel.add(a, RDFS.subClassOf, b);
        }

        Iterator iter = bc.getConfiguredClasses().iterator();
        while (iter.hasNext()) {            
            query = "SELECT subject, predicate, object FROM {subject} <rdf:type> {<!" + (String) iter.next() + ">}; predicate {object}";
            results = this._repo.performTableQuery(QueryLanguage.SERQL, query);
            rows = results.getRowCount();
            for (int i = 0; i < rows; i++) {
                Resource a = ResourceFactory.createResource(results.getValue(i, 0).toString());
                Property b = ResourceFactory.createProperty(results.getValue(i, 1).toString());
                RDFNode c = null;

                Value value = results.getValue(i, 2);
                if (value instanceof org.openrdf.model.Resource) {
                    c = ResourceFactory.createResource(value.toString());
                }
                else if (value instanceof org.openrdf.model.Literal) {
                    c = ResourceFactory.createPlainLiteral(value.toString());
                }

                this._cachedModel.add(a, b, c);
            }
        }
        
        iter = bc.getConfiguredClasses().iterator();
        while (iter.hasNext()) {
            String classURI = (String) iter.next();

            // for each data object in the model of that type
            query = "SELECT subject FROM {subject} <rdf:type> {<!"+ classURI + ">}";
            results = this._repo.performTableQuery(QueryLanguage.SERQL, query);
            rows = results.getRowCount();
            for (int i = 0; i < rows; i++) {
                this._objects.add( results.getValue(i, 0).toString() );
            }
        }
    }

   /**
    * This creates a list of equivalent terms so we can hide them in the UI.
    */
   public void createPreferredTerms() {
       try {
           String query = "SELECT subject FROM {subject} <rdf:type> {<!"+ Display.PreferredTerm.getURI() + ">}";
           QueryResultsTable results = this._repo.performTableQuery(QueryLanguage.SERQL, query);
           int rows = results.getRowCount();
           for (int i = 0; i < rows; i++) {
               org.openrdf.model.Resource a = (org.openrdf.model.Resource) results.getValue(i, 0);
			  String uriString = pseudoUri(a);
               primaryEquivalences.add(uriString);

               // a secondary term is any term that is subject to a sameAs relation
               // but is not a preferred term
               String subquery = "SELECT equal FROM {<!" + uriString + ">} <!" + OWL.sameAs.getURI() + "> {equal}";
               QueryResultsTable subresults = this._repo.performTableQuery(QueryLanguage.SERQL, subquery);
               int subrows = subresults.getRowCount();
               int subcols = subresults.getColumnCount();
               for (int j = 0; j < subrows; j++) {
                   org.openrdf.model.Resource b = (org.openrdf.model.Resource) subresults.getValue(j, 0);
                   secondaryEquivalences.add(pseudoUri(b));
               }
           }
       } catch (Exception e) {
           System.err.println(e.toString());
           e.printStackTrace();
       }
   }

   public void createIndex() {
       // TO DO write!
   }

   protected String pseudoUri(org.openrdf.model.Resource res) {
       if (res instanceof org.openrdf.model.URI) {
          return ((org.openrdf.model.URI) res).getURI();
 	   } else {
          return "http://simile.mit.edu/anonymous#" + ((org.openrdf.model.BNode) res).getID();
       }
    }
}
