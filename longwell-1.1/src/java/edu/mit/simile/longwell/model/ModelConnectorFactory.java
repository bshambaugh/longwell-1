/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model;

import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;

import org.apache.log4j.Logger;

import javax.servlet.ServletContext;

/**
 * Dispatches connections to existing, local models.
 * Some model sources cannot be connected to locally and will return empty default Models.
 * 
 * @author ryanlee
 */
public class ModelConnectorFactory {
    public static Model createModel(String type, ServletContext context, Properties properties, Logger logger) throws Exception {
        String className = null;
        if (type.equals("files")) {
            className = "edu.mit.simile.longwell.model.jena.JenaFileModelConnector";
        } else if (type.equals("database")) {
            className = "edu.mit.simile.longwell.model.jena.JenaDatabaseModelConnector";
        } else if (type.equals("kowari-memory")) {
            className = "edu.mit.simile.longwell.model.kowari.KowariMemoryModelConnector";
        } else if (type.equals("kowari")) {
            className = "edu.mit.simile.longwell.model.kowari.KowariModelConnector";
        } else if (type.equals("joseki")) {
            className = "edu.mit.simile.longwell.model.joseki.JosekiModelConnector";
        } else if (type.equals("3store")) {
            className = "edu.mit.simile.longwell.model.threestore.ThreeStoreModelConnector";
        } else if (type.equals("sesame")) {
            className = "edu.mit.simile.longwell.model.sesame.SesameModelConnector";
        } else {
            throw new Exception("You must specify a repository.type in the properties file");
        }

        try {
            Class m = Class.forName(className);
            ModelConnector connector = (ModelConnector) m.newInstance();
            return connector.getModel(properties, context, logger);
        } catch (ClassNotFoundException e) {
            throw new Exception(className + " is unavailable, check that required libraries are in place");
        }       
    }
}
