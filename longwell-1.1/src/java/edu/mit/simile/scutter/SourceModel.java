// (c) 2003, 2004 Andreas Harth

package edu.mit.simile.scutter;

import edu.mit.simile.longwell.model.FacetModel;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceRequiredException;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Scutter stores all files fetched in SourceModel using
 * reification, thus SourceModel is used for aggregating the crawled RDF.
 *
 * @author Andreas Harth
 * $Id: SourceModel.java 67 2004-04-27 20:41:59Z aharth $
 */
public class SourceModel {
    static Logger _logger = Logger.getLogger(SourceModel.class);
    protected FacetModel _facet;
    protected Model _model;
    
    
    /**
     * Constructor.
     */
    public SourceModel(FacetModel m) {
        this._facet = m;
        this._model = this._facet.getModel(); 
    }
    
    
    /**
     * Cleanup.
     */
    public void close() {
        ;
    }
    
    
    /**
     * Remove a reified statement in the form stmt[provenance->http://index.rss]
     */
    public void removeReifStatement(Statement stmt) {
        try {
            Resource re = stmt.getSubject();
            ReifiedStatement rs = (ReifiedStatement)re.as(ReifiedStatement.class);
            Statement s = rs.getStatement();
            
            _model.removeReification(rs);
            _model.remove(s);
        } catch (ClassCastException e) {
            _logger.info("Somebody used source property wrongly");
        }
    }
    
    
    /**
     * Jena has this strange behavior to just add similar statements
     * and not overwrite statements
     * We need to delete all statements from a given url so that
     * when the new crawl comes we don't have duplicates/or old
     * stuff in the model.
     */
    public void removeStatementsFrom(Resource URL) throws RDFException {
        _logger.debug("URL: " + URL);
        StmtIterator statements = _model.listStatements(new SimpleSelector(null, ScutterVocab.provenance, URL));
        int count = 0;
        
        while(statements.hasNext()) {
            try {
                Statement stmt = statements.nextStatement();
                
                removeReifStatement(stmt);
                
                statements.remove();
                
                count++;
            } catch (ClassCastException e) {
                _logger.info("Somebody used source property wrongly");
            }
        }
        
        _logger.info("Deleted " + count + " triples from "+URL.getURI());
    }
    
    
    /**
     * Add incoming (already validated) model to SourceModel after
     * removing old content.
     * Statements are stored reified to retain provenance.
     * XXX Idea is to change just the provenance URI to retain old
     * content, but that has to be done.
     */
    public void add(Model incoming, Resource uri) {
        try {
            // remove old statements
            removeStatementsFrom(uri);
            
            int count = 0;
            
            // add new statements
            for(StmtIterator i = incoming.listStatements(); i.hasNext(); ) {
                Statement s = i.nextStatement();
                addStatementFrom(s, uri);
                count += 2;
            }
            
            _logger.info("Added " + count + " triples to SourceModel.");
        } catch(RDFException e) {
            _logger.error(e);
        }
    }
    
    
    /**
     * Store a statement using reification, which means that a complete
     * statement is subject of another statement.
     */
    public void addStatementFrom(Statement s, Resource from) throws RDFException {
        _model.add(s);
        ReifiedStatement restmt = _model.createReifiedStatement(s);
        _model.add(_model.createStatement(restmt, ScutterVocab.provenance, from));
    }
    
    /**
     * For test cases only.
     */
    public Model getModel() {
        return _model;
    }
    
    // ----------- get methods used for scuttering
    
    /**
     * Get iterator over all rdfs:seeAlso resources.
     * XXX FIXME can get into loops here easily
     */
    public Iterator getSeeAlsos(Resource uri) throws RDFException {
        StmtIterator statements = _model.listStatements(new SimpleSelector(uri, RDFS.seeAlso, (RDFNode)null));
        ArrayList list = new ArrayList();
        
        while(statements.hasNext()) {
            Statement stmt = statements.nextStatement();
            try {
                list.add(stmt.getResource());
            } catch (ResourceRequiredException rre) {
                _logger.debug("anonymous resource :(");
            }
        }
        
        return list.iterator();
    }
    
    /**
     * Get milisecs for 
     * 'hourly' | 'daily' | 'weekly' | 'monthly' | 'yearly'
     */
    public long getMillis(String period, int frequency) {
        long hour = 1000*60*60;
        
        if (period == null || period.equals(ScutterVocab.updatePeriodNotSpecified) || frequency == ScutterVocab.updateFreqNotSpecified) {
            return hour*24*7; // default --> weekly
        }
        
        hour = hour/frequency;
        
        long milis = 0;
        
        //_logger.debug("m " + milis + " h " + hour + " f " + frequency + " p " + period);
        
        period = period.trim().toLowerCase();
        
        if (period.equals("hourly")) {
            milis = hour;
        } else if (period.equals("daily")) {
            milis = hour*24;
        } else if (period.equals("weekly")) {
            milis = hour*24*7;
        } else if (period.equals("monthly")) {
            milis = hour*24*7*30;
        } else if (period.equals("yearly")) {
            milis = hour*24*7*365;
        } else {
            _logger.error(period + " is none of 'hourly' | 'daily' | 'weekly' | 'monthly' | 'yearly'");
        }
        
        return milis;
    }
    
    
    /**
     * Get the updateFrequency property for a given URI.
     */
    public int getUpdateFrequency(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.updateFrequency);
        
        //_logger.debug("freq " + stmt.getInt());
        
        if (stmt != null)
            return stmt.getInt();
        
        return ScutterVocab.updateFreqNotSpecified;
    }
    
    
    /**
     * Get the updatePeriod property for a given URI.
     */
    public String getUpdatePeriod(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.updatePeriod);
        
        //_logger.debug("period " + stmt.getString());
        
        if (stmt != null)
            return stmt.getString();
        
        return ScutterVocab.updatePeriodNotSpecified;
    }
}
