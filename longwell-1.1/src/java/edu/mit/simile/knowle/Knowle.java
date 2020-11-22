package edu.mit.simile.knowle;


import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NsIterator;

import edu.mit.simile.RDFBrowser;
import edu.mit.simile.knowle.model.Frame;
import edu.mit.simile.knowle.model.FrameMaker;
import edu.mit.simile.knowle.model.NonExistentResourceException;

/**
 * The Knowle RDF Browser.
 *
 * @author Ryan Lee
 */
public class Knowle extends RDFBrowser {

    private static final long serialVersionUID = -8891165039442490843L;
    
    final static public String URL = "knowle";
    final static public String TEMPLATE = "knowle.vt";

	private OntDocumentManager labelSource;
    private String sourceURI;
    
    public void init(ServletConfig config) throws ServletException {
    		super.init(config);
    		
    		try {
        		logger.info("Initializing Knowle...");

			this.labelSource = new OntDocumentManager();
			boolean custom = Boolean.valueOf(config.getInitParameter("customOntDocManagerPath")).booleanValue();
			if (custom) {
				String fullPath = "file:" + context.getRealPath("/") + context.getInitParameter("ontDocManagerPath");
				this.labelSource.setMetadataSearchPath(fullPath, true);
			}
	    		this.labelSource.setCacheModels(true);

	    		if (Boolean.valueOf(config.getInitParameter("loadOntologies")).booleanValue()) {
	    			NsIterator nsi = this.model.listNameSpaces();
	    			while(nsi.hasNext()) {
	    				String ns = nsi.nextNs();
	    				// use the alternate URI for this namespace if it's been mapped
	    				String loadUri = this.labelSource.doAltURLMapping(ns);
	    				Model ontology = ModelFactory.createDefaultModel();
	    				try {
	    					ontology.read(loadUri);
	    					this.labelSource.addModel(ns, ontology);
	    				} catch (Exception e) {
	    					logger.error("Error loading model: " + loadUri, e);
	    				}
	    			}
	    		}
        } catch (Exception e) {
            logger.error("Error initializing Knowle", e);
        }
    }

    /**
     * @param request
     * @param response
     *
     * @throws ServletException
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		String[] resources;
		String[] properties;
		String[] values;
		String error = null;
        
		long startTime = System.currentTimeMillis();

		resources = request.getParameterValues("resource");
		properties = request.getParameterValues("prop");
		values = request.getParameterValues("val");

		Frame f = null;
	
		if ((null != resources) && (!resources[0].equals(""))) {
			try {
			    f = FrameMaker.make(this.model, this.labelSource, resources[0]);
			} catch (NonExistentResourceException e) {
				f = FrameMaker.makeRandom(this.model, this.labelSource);
				error = e.getMessage() + "; random resource selected instead";
			}
		} else if ((null != properties) && (null != values) && (!properties[0].equals("")) && (!values[0].equals(""))) {
		    try {
		    		f = FrameMaker.make(this.model, this.labelSource, properties[0], values[0]);
		    } catch (NonExistentResourceException e) {
		    		f = FrameMaker.makeRandom(this.model, this.labelSource);
		    		error = e.getMessage() + "; random resource selected instead";
		    }
		} else {
		    f = FrameMaker.makeRandom(this.model, this.labelSource);
		    error = "No resource specified; random resource selected";
		}

		VelocityContext vcContext = new VelocityContext();
		vcContext.put("frame", f);
		vcContext.put("base", URL + "?resource=");
		vcContext.put("error", error);

		response.setContentType("text/html; charset=UTF-8");

		long endTime = System.currentTimeMillis();

		logger.info("Time to process knowle template: " + format(endTime - startTime));

		try {
		    ve.mergeTemplate(Knowle.TEMPLATE, vcContext, response.getWriter());
		} catch (Exception e) {
		    logger.error("Error executing the request. ", e);
		}    
	}
}
