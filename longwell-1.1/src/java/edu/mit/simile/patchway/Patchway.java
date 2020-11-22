/*
 * Created on Oct 8, 2004
 */
package edu.mit.simile.patchway;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.simile.RDFBrowser;


/**
 * @author ryanlee
 */
public class Patchway extends RDFBrowser {
    private static final long serialVersionUID = 5226002606379506186L;
    
    final private String browseURI = "patchway";
    final private String uiTemplate = "patchway.vt";
    final private String responseTemplate = "patchway-answer.vt";

    public void init(ServletConfig config) throws ServletException {
    		super.init(config);
    		
    		/**
    		 * a start towards using joseki programatically, but I am failing...
    		 * 
            Resource module = null ;
            module = sourceDesc.getProperty(JosekiVocab.sourceController).getResource() ;
            SourceController srcCtl = (SourceController)moduleLoader.loadAndInstantiate(module, SourceController.class) ;

            if ( srcCtl == null )
            {
                log.warn("Can't load find source controller for "+serverURI) ;
                return null ;
            }
            
            ModelSource a = srcCtl.createSourceModel(sourceDesc, serverURI) ;
            log.trace("Built model source controller") ;
            return a ;
            */
    		try {
        		logger.info("Initializing Patchway...");
        		// do nothing
        } catch (Exception e) {
            logger.error("Error initializing Joseki", e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
        String[] query;
        String queryString = null;
        String error = null;
        String template = null;
        String type = null;
        QueryProcessor qp = null;
        Model answer = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        long start = System.currentTimeMillis();
        
        query = request.getParameterValues("query");
        
        if (null != query && query.length > 0)
            queryString = query[0];
        
        try {
            if (null != queryString) {
                if (queryString.equals("")) {
                    error = "No query given.";
                    type = "text/html; charset=utf-8";
                    template = uiTemplate;
                } else {
                    qp = new QueryProcessor(queryString, this.model);
                    answer = qp.execute();
                    template = responseTemplate;
                    type = "application/rdf+xml";
                    answer.write(os);
                }
            } else {
                template = uiTemplate;
                type = "text/html; charset=utf-8";
            }
        } catch (Exception e) {
            logger.error("Error executing query.  ", e);
            error = e.toString();
            template = uiTemplate;
            type = "text/html; charset=utf-8";
        }
                
		VelocityContext vcContext = new VelocityContext();
		vcContext.put("answer", os.toString());

		long end = System.currentTimeMillis();
		logger.info("Time to process patchway template: " + format(end - start));

		response.setContentType(type);

		try {
		    if (null != error) {
		        response.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
		    } else {
		        ve.mergeTemplate(template, vcContext, response.getWriter());
		    }
		} catch (Exception e) {
		    logger.error("Error executing the request. ", e);
		}    
	}
}
