package edu.mit.simile.longwell.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;

import java.util.Iterator;

/**
 * A simple helper class that hides some of the complexity of the RDQL API.
 *
 * @author Mark H. Butler
 */
public class QueryHelper {
    private Query query;
    private QueryExecution qe;
    private QueryResults queryResults;

    /**
     * Creates a new QueryHelper object.
     *
     * @param model The model we are going to query.
     * @param queryString The query string.
     */
    public QueryHelper(Model model, String queryString) {
        // System.out.println(queryString);
        query = new Query(queryString);
        query.setSource(model);
        qe = new QueryEngine(query);
        queryResults = qe.exec();
    }

    /**
     * Get the query results.
     *
     * @return
     */
    public Iterator getResults() {
        Iterator i = queryResults;

        return i;
    }

    /**
     * Convert the query results to a Jena RDF Model.
     *
     * @return
     */
    public Model getModel() {
        Model resultModel = ModelFactory.createDefaultModel();
        Iterator results = queryResults;

        for (; results.hasNext();) {
            ResultBinding rb = (ResultBinding) results.next();
            rb.mergeTriples(resultModel);
        }

        return resultModel;
    }

    /**
     * Close the query.
     */
    public void close() {
        queryResults.close();
    }
}
