package edu.mit.simile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.ModelLoader;

import edu.mit.simile.longwell.model.ModelConnectorFactory;

/**
 * SimileRepository creates the servlet for the SIMILE repository
 *
 * @author Mark H. Butler
 * @author Stefano Mazzocchi
 */
public abstract class RDFBrowser extends HttpServlet {
    static final float SECOND = 1000;
    static final float MINUTE = 60 * SECOND;
    static final float HOUR   = 60 * MINUTE;

    protected Logger logger = Logger.getLogger(RDFBrowser.class);
    
    protected Properties props;
    protected VelocityEngine ve;
    protected Model model;
    
    protected ServletContext context;
    protected ServletConfig config;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
			this.config = config;
			this.context = config.getServletContext();
			
			PropertyConfigurator.configure(context.getRealPath("WEB-INF/log4j.properties"));        
			
			this.props = new Properties();
			
			InputStream pis = context.getResource(context.getInitParameter("properties")).openStream();
			this.props.load(pis);
			pis.close();
			
			this.model = (Model) this.context.getAttribute("RDFModel");

			if (this.model == null) {
				this.model = getModel(props);
				this.context.setAttribute("RDFModel", this.model);
			}
			
			ve = new VelocityEngine();
			ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,"org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			ve.setProperty("runtime.log.logsystem.log4j.category", "Velocity");
			ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, context.getRealPath(context.getInitParameter("templates")));
			ve.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, new Boolean(false));
			ve.addProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD, new Boolean(true));
			ve.init();            
        } catch (Exception e) {
            logger.error("Error initializing RDFBrowser.", e);
		    this.context.setAttribute("RDFModel", ModelFactory.createDefaultModel());
		    // NOTE(SM): the above prevents the other browsers to retry to instantiate the model
        }       
    }

    /**
     * This loads the demo dataset into memory.
     */
    private Model getModel(Properties props) throws Exception {
    		Model model = null;
        String modelSource = props.getProperty("repository.type", "file");

        model = ModelConnectorFactory.createModel(modelSource, context, props, logger);
        
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
    
    /**
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
}
