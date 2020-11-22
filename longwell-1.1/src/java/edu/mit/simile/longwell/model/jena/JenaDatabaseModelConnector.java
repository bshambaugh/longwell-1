/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model.jena;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.simile.longwell.model.ModelConnector;


/**
 * @author ryanlee
 */
public class JenaDatabaseModelConnector extends ModelConnector {

    /* (non-Javadoc)
     * @see edu.mit.simile.longwell.model.ModelConnector#getModel(java.util.Properties, org.apache.log4j.Logger)
     */
    public Model getModel(Properties props, ServletContext context, Logger logger) throws Exception {
        // Jena requires correct casing for the database type name
        String jenaClassType = props.getProperty("database.type");
        String prefix = jenaClassType.toLowerCase();
        String DB = props.getProperty(prefix + ".database");
        String DB_URL = "jdbc:"+prefix+"://localhost/" + DB;
        String className = props.getProperty(prefix + ".classname");
        String DB_user = props.getProperty(prefix + ".user");
        String DB_pass = props.getProperty(prefix + ".password");
        String modelName = props.getProperty(prefix + ".model");        

        // Load the Driver 
        Class.forName(className);

        // Create database connection 
        IDBConnection conn = new DBConnection(DB_URL, DB_user, DB_pass, jenaClassType);

        if (conn.containsModel(modelName)) {
            // open a previously created model
            return ModelRDB.open(conn, modelName);
        } else {
            logger.info("Fatal error: could not connect to database and load model " + modelName);
            return null;
        }
    }
}
