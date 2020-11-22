// (c) 2003, 2004 Andreas Harth

package edu.mit.simile.scutter;


import edu.mit.simile.longwell.model.FacetModel;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PropertyNotFoundException;

/**
 * The ScutterModel holds all information the Scutter has
 * about crawls: when an URI has been last visited, robots.txt
 * for the URI, last modified headers, etc.
 *
 * @author Andreas Harth
 * $Id: ScutterModel.java 107 2004-05-20 16:29:51Z aharth $
 */
public class ScutterModel {
    static Logger _logger = Logger.getLogger(ScutterModel.class);
    protected FacetModel _facet;
    protected Model _model;
    
    /**
     * Constructor.
     * gets the meta model as constructor
     */
    public ScutterModel(FacetModel m) {
        this._facet = m;
        this._model = m.getModel();
    }
    
    
    /**
     * Check whether URL already exists in the model.
     * XXX is this as well if it has been crawled before?
     */
    public boolean subjectExists(Resource uri) {
        StmtIterator stmts = _model.listStatements(new SimpleSelector(uri, ScutterVocab.lastVisited, (RDFNode)null));
        
        return stmts.hasNext();
    }
    
    
    /**
     * Number of data sources that have been crawled.
     * XXX probably very expensive operation for just getting a number.
     */
    public int getDataSourcesCount() {
        int i = 0;
        StmtIterator stmts = _model.listStatements(new SimpleSelector(null, ScutterVocab.httpStatus, 200));
        
        while (stmts.hasNext()) {
            stmts.next();
            i++;
        }
        
        return i;
    }
    
    // ------------------ getter/setter methods ----------------------
    
    /**
     * Get the HTTP status code.
     */
    public int getHTTPStatus(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.httpStatus);
        
        if (stmt != null)
            return stmt.getInt();
        
        return ScutterVocab.statusNotSpecified;
    }
    
    
    /**
     * Set the HTTP status code.
     */
    public void setHTTPStatus(Resource uri, int status) throws RDFException {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.httpStatus);
            stmt.changeObject(status);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.httpStatus, status));
        }
    }
    
    
    /**
     * Get whether the uri is valid RDF.
     */
    public Resource getValidRDF(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.validRDF);
        
        if (stmt != null)
            return stmt.getResource();
        
        return ScutterVocab.validNotSpecified;
    }
    
    
    /**
     * Set validRDF property.
     */
    public void setValidRDF(Resource uri, Resource valid) throws RDFException {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.validRDF);
            stmt.changeObject(valid);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.validRDF, valid));
        }
    }
    
    
    /**
     * Get whether the uri is valid RDF.
     */
    public long getLastVisited(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.lastVisited);
        
        if (stmt != null)
            return stmt.getLong();
        
        return ScutterVocab.lastVisitNotSpecified;
    }
    
    
    /**
     * Set the last visited property for a given URL.
     */
    public void setLastVisited(Resource uri, long currentTime) throws RDFException {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.lastVisited);
            stmt.changeObject(currentTime);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.lastVisited, currentTime));
        }
    }
    
    
    /**
     * Get http last_modified header from last crawl.
     */
    public String getLastModified(Resource uri) {
        try {
            Statement stmt = _model.getProperty(uri, ScutterVocab.httpLastModified);
            
            if (stmt != null)
                return stmt.getString();
        } catch (RDFException e) {
            _logger.debug(e.getMessage());
        }
        
        return ScutterVocab.lastModifiedNotSpecified;
    }
    
    
    /**
     * Set the last modified header.
     */
    public void setLastModified(Resource uri, String lastModified) throws RDFException {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.httpLastModified);
            stmt.changeObject(lastModified);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.httpLastModified, lastModified));
        }
    }
    
    
    /**
     * Get the crawlInProgress property for a given URI.
     */
    public boolean getCrawlInProgress(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.crawlInProgress);
        
        if (stmt != null)
            return stmt.getBoolean();
        
        return false;
    }
    
    
    /**
     * Set crawl-in-progress property.
     */
    public void setCrawlInProgress(Resource uri, boolean flag) {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.crawlInProgress);
            stmt.changeObject(flag);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.crawlInProgress, flag));
        }
    }
    
    
    // ------- robots.txt
    
    /**
     * Get robots.txt as string.
     */
    public String getRobotsTxt(Resource uri) {
        Statement stmt = _model.getProperty(uri, ScutterVocab.robotsTxt);
        
        if (stmt != null)
            return stmt.getString();
        
        return ScutterVocab.robotsTxtNotSpecified;
    }
    
    
    /**
     * Set robots.txt.
     */
    public void setRobotsTxt(Resource uri, String robotsTxt) {
        try {
            Statement stmt = _model.getRequiredProperty(uri, ScutterVocab.robotsTxt);
            stmt.changeObject(robotsTxt);
        } catch (PropertyNotFoundException pe) {
            _model.add(_model.createStatement(uri, ScutterVocab.robotsTxt, robotsTxt));
        }
    }
    
    
    /**
     * Remove robots.txt 
     */
    public void removeRobotsTxt(Resource uri) {
        StmtIterator stmts = _model.listStatements(uri, ScutterVocab.robotsTxt, (RDFNode)null);
        
        _model.remove(stmts);
    }
}
