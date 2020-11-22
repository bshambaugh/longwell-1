/*
 * Created on Oct 22, 2004
 */
package edu.mit.simile.longwell;

import java.util.TimerTask;
import java.io.File;
import java.io.FileInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.ModelLoader;

import org.apache.log4j.Logger;

import edu.mit.simile.longwell.model.FacetModel;

/**
 * @author ryanlee
 */
public class ConfigurationListener extends TimerTask {
    private BrowserConfig _bc;
    private File _configFile;
    private long _lastModified;
    private Logger _logger;
    private FacetModel _fm;
    
    public ConfigurationListener(BrowserConfig bc, File configFile, FacetModel fm, Logger logger) {
        this._bc = bc;
        this._configFile = configFile;
        this._fm = fm;
        this._logger = logger;
        this._lastModified = this._configFile.lastModified();
    }
    
    public void run()	 {
        if (this._configFile.lastModified() > this._lastModified) {
            boolean load = true;
            Model configModel = ModelFactory.createDefaultModel();

            try {
                String syntax = ModelLoader.guessLang(this._configFile.getCanonicalPath());
                FileInputStream fis = new FileInputStream(this._configFile);
                configModel.read(fis, "", syntax);
                fis.close();
            } catch (Exception e) {
                load = false;
                this._logger.error("Could not re-load configuration file; check for syntax errors: " + e.toString());
            }
            
            if (load) {
                try {
                    this._bc.reset();
                    this._bc.loadConfiguration(configModel);
                    this._fm.configureBrowser(this._bc);
                    this._lastModified = this._configFile.lastModified();
                    this._logger.info("Reloaded configuration");
                } catch (Exception e) {
                    this._logger.error("Could not re-configure browser; check model for errors: " + e.toString());                    
                }
            }
        }
    }
}
