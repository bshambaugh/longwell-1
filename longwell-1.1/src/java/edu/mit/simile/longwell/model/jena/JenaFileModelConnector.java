/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model.jena;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.mit.simile.inferencing.JenaReasoner;
import edu.mit.simile.inferencing.Reasoner;
import edu.mit.simile.inferencing.SimileReasoner;
import edu.mit.simile.longwell.model.ModelConnector;


/**
 * @author ryanlee
 */
public class JenaFileModelConnector extends ModelConnector {

    /* (non-Javadoc)
     * @see edu.mit.simile.longwell.model.ModelConnector#getModel(java.util.Properties, org.apache.log4j.Logger)
     */
    public Model getModel(Properties props, ServletContext context, Logger logger) throws Exception {
        long startTime = System.currentTimeMillis();

        Model model = ModelFactory.createDefaultModel();
        
        File data = new File(context.getRealPath(props.getProperty("data.path","WEB-INF/data/")));
        
        logger.info("Loading files from data.path: " + data);
        
        if (data.isDirectory()) {
            model = loadModel(model,data, logger);
        } else {
        		throw new Exception("You must define a data.path that points to a directory");
        }

        long endTime = System.currentTimeMillis();
        logger.info("Time to read in RDF data and schemas: " + format(endTime - startTime));

        if (props.getProperty("inferencing").equals("yes")) {
        		long preInferencingSize = model.size();
        		
            logger.info("Prior to inferencing, RDF model contains " + preInferencingSize + " statements");
            logger.info("Starting inferencing...");
            logger.info("   [WARNING: this can take a while, even 15 minutes!]");
            startTime = System.currentTimeMillis();

            Reasoner reasoner = null;        
            if (context.getInitParameter("useJenaInferencer").equals("yes")) {
            		reasoner = new JenaReasoner();
            } else {
            		reasoner = new SimileReasoner();
            }

            model = reasoner.process(model);

            logger.info("Finished inferencing, RDF model contains " + model.size() + " statements");
            
            endTime = System.currentTimeMillis();
            
            logger.info("Time to run inferencing: " + format(endTime - startTime));
        }

        return model;
    }
}
