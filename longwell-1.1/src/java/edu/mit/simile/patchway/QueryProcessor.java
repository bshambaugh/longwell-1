/*
 * Created on Oct 13, 2004
 */
package edu.mit.simile.patchway;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryExecution;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;

/**
 * Feed RDQL to a model and get the answer back.
 * 
 * @author ryanlee
 */
public class QueryProcessor {
    String _queryString;
    Query _query;
    Model _model;
    
    public QueryProcessor(String query, Model model) throws Exception {
        if (null == query || query.equals("")) {
            throw new Exception("No query string provided");
        }
        
        Query q = null;
        
        try {
            q = new Query(query);
            q.setSource(model);
        } catch (Exception e) {
            throw new Exception("Parse error: " + query);
        }
        
        this._queryString = query;
        this._query = q;
        this._model = model;
    }
    
    public Model execute() {
        QueryResults results = null;
        Model resultModel = ModelFactory.createDefaultModel();

        QueryExecution qe = new QueryEngine(this._query);
        
        results = qe.exec();

        for (; results.hasNext();) {
            ResultBinding rb = (ResultBinding)results.next();
            rb.mergeTriples(resultModel);
        }
        
        resultModel.setNsPrefixes(this._model);
                
        return resultModel;
    }
}
