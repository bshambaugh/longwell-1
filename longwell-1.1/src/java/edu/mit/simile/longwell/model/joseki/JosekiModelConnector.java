/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model.joseki;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.mit.simile.longwell.model.ModelConnector;


/**
 * Connects to a local, existing Joseki model.  But not really, since
 * Joseki is intentionally a remote-access store.
 * 
 * @author ryanlee
 */
public class JosekiModelConnector extends ModelConnector {

    /**
     * Returns default Jena Model.
     * 
     * @see edu.mit.simile.longwell.model.ModelConnector#getModel(java.util.Properties, org.apache.log4j.Logger)
     */
    public Model getModel(Properties properties, ServletContext context, Logger logger) {
        return ModelFactory.createDefaultModel();
    }
}
