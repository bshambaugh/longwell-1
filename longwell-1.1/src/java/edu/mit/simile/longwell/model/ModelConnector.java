/*
 * Created on Oct 4, 2004
 */
package edu.mit.simile.longwell.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.ModelLoader;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * Opens an already existing model (from the filesystem, a database, a triple store, etc.)
 * 
 * @author ryanlee
 */
public abstract class ModelConnector {
    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;
    
    /**
     * Local-model connecting methods
     * 
     * @param properties
     * @param logger
     * @return
     */
    public abstract Model getModel(Properties properties, ServletContext context, Logger logger) throws Exception;
    
    /**
     * Format time in a more human reaable manner
     * 
     * @param time
     * @return
     */
    protected String format(long time) {
        StringBuffer out = new StringBuffer();
        if (time <= SECOND) {
            out.append(time);
            out.append(" milliseconds");
        } else if (time <= MINUTE) {
            out.append(time / SECOND);
            out.append(" seconds");
        } else if (time <= HOUR) {
            out.append(time / MINUTE);
            out.append(" minutes");
        } else {
            out.append(time / HOUR);
            out.append(" hours");
        }
        return out.toString();
    }
    
    /**
     * Load a model from the filesystem
     * 
     * @param model
     * @param dir
     * @return
     * @throws Exception
     */
    protected Model loadModel(Model model, File dir, Logger logger) throws Exception {
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
			    model = loadModel(model, file, logger);
			} else if (file.getName().matches(".*\\.rdf(s?)|.*\\.n3|.*\\.owl")) {
    				model = readModel(model, file, logger);
    			}
		}
		return model;
    }

    /**
     * 
     * @param model
     * @param file
     * @param logger
     * @return
     * @throws Exception
     */
    protected Model readModel(Model model, String file, Logger logger) throws Exception {
		return readModel(model, new File(file), logger);
    }

    	/**
    	 * 
    	 * @param model
    	 * @param file
    	 * @param logger
    	 * @return
    	 * @throws Exception
    	 */
    protected Model readModel(Model model, File file, Logger logger) throws Exception {
        logger.info("Loading model: " + file);
        String syntax = ModelLoader.guessLang(file.getCanonicalPath());
        FileInputStream fis = new FileInputStream(file);
        model.read(fis, "", syntax);
        fis.close();
        return model;
    }
}
