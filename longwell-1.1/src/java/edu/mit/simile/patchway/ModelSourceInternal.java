/*
 * Created on Oct 8, 2004
 */
package edu.mit.simile.patchway;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joseki.server.ModelSourceJena;
import org.joseki.server.ProcessorRegistry;
import org.joseki.server.SourceController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping.IllegalPrefixException;


/**
 * Based on org.joseki.server.source.ModelSourceCom
 * 
 * @author ryanlee
 */
public class ModelSourceInternal implements ModelSourceJena {
    static Log logger = LogFactory.getLog(ModelSourceInternal.class.getName()) ;
    
    protected SourceController sourceController = null ;
    protected String serverURI;             // How it appears to the server 
    protected boolean isImmutable = false ;
    protected boolean isActive = false;
    
    private boolean inOperation = false ;
    private boolean isReadLocked = false ;
    private boolean isWriteLocked = false ;
    protected Model model = null;
    
    Map prefixes = null ;
    ProcessorRegistry processors = new ProcessorRegistry() ;

    public ModelSourceInternal(SourceController ctl, Model _model, String serverURI) {
        this.sourceController = ctl;
        this.serverURI = serverURI;
        this.model = _model;
    }
    
    /* (non-Javadoc)
     * @see org.joseki.server.ModelSourceJena#getModel()
     */
    public Model getModel() {
        return this.model;
    }

    /* (non-Javadoc)
     * @see org.joseki.server.ModelSource#getServerURI()
     */
    public String getServerURI() {
        return this.serverURI;
    }

    public Map getPrefixes() {
        if ( prefixes == null )
            prefixes = new HashMap() ;
        return prefixes ;
    }

    public void setPrefix(String prefix, String nsURI) {
        if ( prefixes == null )
            prefixes = new HashMap() ;
        // Set both here and in the underlying model.
        prefixes.put(prefix, nsURI) ;
        try { getModel().setNsPrefix(prefix, nsURI) ; } catch (IllegalPrefixException e ) {}
    }


    public synchronized void startOperation(boolean readOnly) {
        activate() ;
        if ( getModel().supportsTransactions() )
            getModel().begin() ;
        else
            getModel().enterCriticalSection(readOnly) ;
    }

    public synchronized void endOperation() {
        if ( getModel().supportsTransactions() )
            getModel().commit() ;
        else
            getModel().leaveCriticalSection() ;
        deactivate() ;
    }
    
    public synchronized void abortOperation() {
        if ( getModel().supportsTransactions() )
            getModel().abort() ;
        else
            getModel().leaveCriticalSection() ;
        deactivate() ;
    }

    public void flush() { return ; }
    
    public void release() {
        if (!isActive)
            return;
        isActive = false;
    }

    public boolean isImmutable() {
        return isImmutable;
    }

    public void setIsImmutable(boolean isFixed) {
        isImmutable = isFixed ;
    }

    public ProcessorRegistry getProcessorRegistry() {
        return processors ;
    }
    
    public void activate() {
        if ( sourceController != null )
            sourceController.activate() ;
    }
    
    public void deactivate() {
        if ( sourceController != null )
            sourceController.deactivate() ;
    }
}
