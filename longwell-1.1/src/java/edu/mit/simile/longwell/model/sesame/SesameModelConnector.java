/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model.sesame;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.mit.simile.longwell.model.ModelConnector;

/**
 * Connects to a local, existing Sesame model.  But not really, since
 * that isn't currently possible with Sesame and Jena.
 * 
 * @author ryanlee
 */
public class SesameModelConnector extends ModelConnector {

    /**
     * Returns default Jena Model.
     * 
     * @see edu.mit.simile.longwell.model.ModelConnector#getModel(java.util.Properties, org.apache.log4j.Logger)
     */
    public Model getModel(Properties properties, ServletContext context, Logger logger) {
        return ModelFactory.createDefaultModel();
    }
}
