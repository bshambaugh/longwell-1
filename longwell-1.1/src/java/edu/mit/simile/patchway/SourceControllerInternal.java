/*
 * Created on Oct 8, 2004
 */
package edu.mit.simile.patchway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joseki.server.ModelSource;
import org.joseki.server.SourceController;
import org.joseki.vocabulary.JosekiVocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author ryanlee
 */
public class SourceControllerInternal implements SourceController {
    static Log logger =
        LogFactory.getLog(SourceControllerInternal.class.getName());
    
    protected String serverURI;
        
    // ----------------------------------------------------------
    // -- Loadable interface     
    public String getInterfaceURI() {
        return JosekiVocab.SourceController.getURI();
    } 
    
    public void init(Resource binding, Resource implementation) { ; }
    
    // ----------------------------------------------------------
    // -- SourceController interface
    // Called once, during configuration
    public ModelSource createSourceModel(Model model, String _serverURI) {
        serverURI = _serverURI;
        
        ModelSource mSrc = new ModelSourceInternal(this, model, serverURI);
        
        return mSrc;
    }
    
    public ModelSource createSourceModel(Resource description, String _serverURI) {
        return null;
    }
    
    public String getServerURI() {
        return serverURI;
    }
    
    // Called when used.
    public void activate() {
        return;
    }
    
    // Called when not in use any more.
    public void deactivate() {
        return;
    }
    
    // Called each time a source needs to be built.
    public Model buildSource() {
        logger.warn("Attempt to build a database source") ;
        return null ;
    }
    
    // Called when released (if releasable)
    public void releaseSource() {
        logger.warn("Attempt to release a database source") ;
    }
}
