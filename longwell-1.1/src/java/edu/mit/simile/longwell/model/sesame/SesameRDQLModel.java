/*
 * Created on Jun 30, 2004
 */
package edu.mit.simile.longwell.model.sesame;

import java.net.URL;
import java.util.Iterator;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.vocabularies.Display;

import org.openrdf.model.Value;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.constants.QueryLanguage;
import org.openrdf.sesame.repository.SesameRepository;
import org.openrdf.sesame.repository.remote.HTTPService;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author ryanlee
 */
public class SesameRDQLModel extends RemoteFacetModel {
    private SesameRepository _repo;
    private String _serverURI, _user, _password, _repository;

    public SesameRDQLModel(Model model, String indexPath, String serverURI, String repository, String user, String password) throws Exception {
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
        
        String query = "SELECT ?a, ?b WHERE (?a, <" + RDFS.label.getURI() + ">, ?b)";
        QueryResultsTable results = this._repo.performTableQuery(QueryLanguage.RDQL, query);
        int rows = results.getRowCount();
        int cols = results.getColumnCount();
        for (int i = 0; i < rows; i++) {
            Resource a = null;
            RDFNode b = null;
            for (int j = 0; j < cols; j++) {
                Value val = results.getValue(i, j);
                if (j == 0) a = ResourceFactory.createResource(val.toString());
                if (j == 1) b = ResourceFactory.createPlainLiteral(val.toString());
            }
            this._cachedModel.add(a, RDFS.label, b);
        }
        
        query = "SELECT ?a, ?b WHERE (?a, <" + RDFS.subClassOf.getURI() + ">, ?b)";
        results = this._repo.performTableQuery(QueryLanguage.RDQL, query);
        rows = results.getRowCount();
        cols = results.getColumnCount();
        for (int i = 0; i < rows; i++) {
            Resource a = null;
            Resource b = null;
            for (int j = 0; j < cols; j++) {
                Value val = results.getValue(i, j);
                if (j == 0) a = ResourceFactory.createResource(val.toString());
                if (j == 1) b = ResourceFactory.createResource(val.toString());
            }
            this._cachedModel.add(a, RDFS.subClassOf, b);
        }

        Iterator iter = bc.getConfiguredClasses().iterator();
        while (iter.hasNext()) {
            query = "SELECT ?a, ?b, ?c WHERE (?a, <"+ RDF.type.getURI() + ">, <" + (String) iter.next() + ">), (?a, ?b, ?c)";
            results = this._repo.performTableQuery(QueryLanguage.RDQL, query);
            rows = results.getRowCount();
            cols = results.getColumnCount();
            for (int i = 0; i < rows; i++) {
                Resource a = null;
                Resource b = null;
                RDFNode c = null;
                for (int j = 0; j < cols; j++) {
                    Value val = results.getValue(i, j);
                    if (j == 0) a = ResourceFactory.createResource(val.toString());
                    if (j == 1) b = ResourceFactory.createResource(val.toString());
                    if (j == 2) {
                        if (val instanceof org.openrdf.model.Resource)
                            	c = ResourceFactory.createResource(val.toString());
                        else if (val instanceof org.openrdf.model.Literal)
                            c = ResourceFactory.createPlainLiteral(val.toString());
                    }
                }
                this._cachedModel.add(a, ResourceFactory.createProperty(b.getURI()), c);
            }
        }
        
        iter = bc.getConfiguredClasses().iterator();
        while (iter.hasNext()) {
            Resource t = ResourceFactory.createResource((String) iter.next());

            // for each data object in the model of that type
            query = "SELECT ?a WHERE (?a, <" + RDF.type.getURI() + ">, <"+ t.getURI() + ">)";
            results = this._repo.performTableQuery(QueryLanguage.RDQL, query);
            rows = results.getRowCount();
            cols = results.getColumnCount();
            for (int i = 0; i < rows; i++) {
                Resource a = null;
                for (int j = 0; j < cols; j++) {
                    Value val = results.getValue(i, j);
                    if (j == 0) a = ResourceFactory.createResource(val.toString());
                    this._objects.add(a.getURI());
                }
            }
        }
    }
    
   /**
    * This creates a list of equivalent terms so we can hide them in the UI.
    */
   public void createPreferredTerms() {
       try {
           String query = "SELECT ?a WHERE (?a, <" + RDF.type.getURI() + ">, <"+ Display.PreferredTerm.getURI() + ">)";
           QueryResultsTable results = this._repo.performTableQuery(QueryLanguage.RDQL, query);
           int rows = results.getRowCount();
           int cols = results.getColumnCount();
           for (int i = 0; i < rows; i++) {
               Resource a = null;
               for (int j = 0; j < cols; j++) {
                   Value val = results.getValue(i, j);
                   if (j == 0) a = ResourceFactory.createResource(val.toString());
                   primaryEquivalences.add(pseudoUri(a));

                   // a secondary term is any term that is subject to a sameAs relation
                   // but is not a preferred term
                   String subquery = "SELECT ?b WHERE (<" + a.getURI() + ">, <" + OWL.sameAs.getURI() + ">, ?b)";
                   QueryResultsTable subresults = this._repo.performTableQuery(QueryLanguage.RDQL, subquery);
                   int subrows = subresults.getRowCount();
                   int subcols = subresults.getColumnCount();
                   Resource b = null;
                   for (int subi = 0; subi < subrows; subi++) {
                       for (int subj = 0; subj < subcols; subj++) {
                           Value subval = subresults.getValue(subi, subj);
                           if (subj == 0) b = ResourceFactory.createResource(subval.toString());
                           secondaryEquivalences.add(pseudoUri(b));
                       }
                   }
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
}
