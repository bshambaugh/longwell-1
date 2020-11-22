/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model.kowari;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import org.kowari.store.Database;
import org.kowari.store.jena.GraphKowariMaker;
import org.kowari.store.jena.ModelKowariMaker;
import org.kowari.store.xa.XADatabaseImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.ReificationStyle;

import edu.mit.simile.longwell.model.ModelConnector;


/**
 * @author ryanlee
 */
public class KowariLocalModelConnector extends ModelConnector {

    /* (non-Javadoc)
     * @see edu.mit.simile.longwell.model.ModelConnector#getModel(java.util.Properties, org.apache.log4j.Logger)
     */
    public Model getModel(Properties properties, ServletContext context, Logger logger) throws Exception {
    		Model kmodel = null;
    		Database database = null;
    		String modelName = properties.getProperty("kowari.model");
    	    long startTime = System.currentTimeMillis();

    		try {
    			String serverName = modelName.substring(modelName.lastIndexOf("/") + 1, modelName.indexOf("#"));
    			URI serverURI = new URI(modelName.substring(1, modelName.indexOf("#")));
    			File dir = new File(System.getProperty("java.io.tmpdir"), System.getProperty("user.name"));
    			File serverDir = new File(dir, serverName);
    			serverDir.mkdirs();
    			removeContents(serverDir);

    			database = new XADatabaseImpl(serverURI, serverDir);
    			GraphKowariMaker gkm = new GraphKowariMaker(database, ReificationStyle.Minimal);
    			ModelKowariMaker mkm = new ModelKowariMaker(gkm, database);
    			kmodel = mkm.makeModel(gkm.createGraph(modelName.substring(modelName.indexOf("#") + 1, modelName.length() - 1), true));
    		} catch (Exception e) {
    			System.err.println(e.toString());
    			e.printStackTrace();
    		}

    		kmodel.begin();

    		File data = new File(context.getRealPath(properties.getProperty("data.path","WEB-INF/data/")));
            
        logger.info("Loading files from data.path: " + data);
            
        if (data.isDirectory()) {
            kmodel = loadModel(kmodel, data, logger);
        } else {
        		throw new Exception("You must define a data.path that points to a directory");
        }

        kmodel = kmodel.commit();
        long endTime = System.currentTimeMillis();        
        logger.info("Time to read in RDF data and schemas: " + format(endTime - startTime));
    		return kmodel;
    	}

    /**
	 * Remove the contents of the given directory.
	 * 
	 * @param dir the file handle to the directory to remove.
	 */
	private void removeContents(File dir) {
		File[] files = dir.listFiles();
		if (files != null)
			for (int i = 0; i < files.length; ++i)
				if (files[i].isFile())
					files[i].delete();
	}
}
