// (c) 2003, 2004 Andreas Harth
package edu.mit.simile.scutter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpRecoverableException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Retrieve data from URLs and store the RDF in the SourceModel.
 * Metadata about a fetch (when etc.) is stored in ScutterModel.
 *
 * @author Andreas Harth
 * $Id: HttpRetriever.java 67 2004-04-27 20:41:59Z aharth $
 */
public class HttpRetriever { // implements Runnable {
    // uri to be retrieved
    private Resource _uri;
    
    private ScutterModel _scutterm;
    private SourceModel _sourcem;
    
    // user-agent header
    private String _userAgentID;
    
    private HttpClient _client;
    
    static Logger _logger = Logger.getLogger(HttpRetriever.class);
    
    private String USERAGENT = "user-agent:";
    private String DISALLOW = "disallow:";
    
    // XXX this has the status if the last fetch FIXME ugly
    // how to pass more than two results from a method?
    private int _status;
    
    /**
     * Constructor.
     * crawl == true : use rdf:seeAlsos
     */
    public HttpRetriever(HttpClient client, String userAgentID, Resource uri, ScutterModel scutterm, SourceModel sourcem) {
        _client = client;
        _userAgentID = userAgentID;
        _uri = uri;
        _sourcem = sourcem;
        _scutterm = scutterm;
    }
    
    /**
     * Run method to start thread.
     */
    public void run() throws Exception {
        //NDC.push(_uri.toString());
        
        // fetch robots.txt
        Resource robotsURI = constructRobotsURI(_uri);
        
        // check if already there
        long rLastVisited = _scutterm.getLastVisited(robotsURI);
        
        // visit robots.txt weekly
        if ((rLastVisited + 1000*60*60*24*7) < System.currentTimeMillis()) {
            try {
                String robotsTxt = fetch(robotsURI, _client, _userAgentID, _scutterm);
                //System.out.println(_status + " " + robotsTxt);
                // distinguish between no robots.txt and 304 here? XXX
                if (_status == 200) {
                    _scutterm.setRobotsTxt(robotsURI, robotsTxt);
                } else if (_status == 404) {
                    _scutterm.removeRobotsTxt(robotsURI);
                } else if (_status == 304) {
                    ; // do nothing
                }
            } catch (IOException ex) {
                _logger.debug(ex);
            }
        }
        
        // check whether or not robots.txt permits access
        try {
            if(!robotSafe(_uri, _scutterm.getRobotsTxt(robotsURI))) {
                _logger.info("Not retrieving, excluded in robots.txt");
                // check back in one month++, maybe they change their minds
                //_scutterm.setLastVisited(_uri, System.currentTimeMillis()+60*1000*60*24*30);
                // note that this url is excluded
                _scutterm.setValidRDF(_uri, ScutterVocab.excluded);
                
                //NDC.pop();
                
                // that's it
                return;
            }
        } catch (IOException ioe) {
            _logger.debug(ioe);
        }
        
        String content = null;
        _logger.info("Retrieving " + _uri);
        
        try {
            content = fetch(_uri, _client, _userAgentID, _scutterm);
            
            // fetched ok?
            _scutterm.setValidRDF(_uri, ScutterVocab.notValidated);
            
            // content can be null because either notchanged
            // or an error occured, in either case don't update
            if (content != null && _status == 200) {
                // validate the RDF
                Model m = ModelFactory.createDefaultModel();
                
                // be extremely restrictive about what gets into the model
                RDFReader arp = m.getReader();
                arp.setProperty("error-mode", "strict-fatal");
                
                // XXX use InputStream instead of Reader, check also character encoding here
                arp.read(m, new StringReader(content), _uri.toString());
                                
                // No exception? then it must be valid
                _scutterm.setValidRDF(_uri, ScutterVocab.valid);
                
                // XXX modify
                _sourcem.add(m, _uri);
                
                // here, call joseki server and add stuff
                /*
                 PutMethod put = new PutMethod("http://localhost:2020/store?uri=" + URLEncoder.encode(_uri.toString()));
                 put.setRequestBody(content);
                 int statusCode = _client.executeMethod(put);
                 _logger.debug("status code of put " + statusCode);
                 */
            }
        } catch (Throwable t) {
            _scutterm.setValidRDF(_uri, ScutterVocab.validationError);
            
            _logger.error(t);
            
            throw new Exception("Validation error occurred while loading RDF from " + _uri);
            /*
             StringWriter stack = new StringWriter();
             t.printStackTrace(new PrintWriter(stack));
             _logger.error(stack.toString(), t);
             */
        } finally {
            //NDC.pop();
        }
    }
    
    /**
     * Go out and fetch, but only if the file has changed.
     *
     * Set the lastVisited, httpStatus, and the lastModified properties.
     *
     * IOException is thrown in case there is an error (not status 200).
     */
    public String fetch(Resource uri, HttpClient client, String userAgentID, ScutterModel  scutterm) throws IOException { //String responseBodyUTF) throws IOException {
        String responseBodyUTF = null;
        
        // try once
        int statusCode = -1;
        
        int attempt = 0;
        
        //byte[] responseBody = null;
        
        GetMethod get = null;
        
        while (statusCode == -1 && attempt < 2) {
            String lastModified = scutterm.getLastModified(uri);
            
            // set header last_modified
            Header ifModifiedSince = new Header("If-Modified-Since", lastModified);
            Header userAgent = new Header("User-Agent", userAgentID);
            
            get = new GetMethod(uri.toString());
            get.setFollowRedirects(true);
            get.addRequestHeader(ifModifiedSince);
            get.addRequestHeader(userAgent);
            
            _logger.debug("About to fetch " + uri);
            
            attempt++;
            
            try {
                statusCode = client.executeMethod(get);
                
                // XXX streaming here please
                // XXX FIXME unicode here!
                responseBodyUTF = get.getResponseBodyAsString();
                
                //System.out.println(responseBodyUTF);
                
                /*
                 if (statusCode == 200)
                 
                 else if (statusCode == 304) {
                 _logger.debug("304 not modified!");
                 responseBodyUTF = get.getResponseBodyAsString();
                 } else if (statusCode == 404 {
                 _logger.debug("404 not found!");
                 }
                 */
            } catch (HttpRecoverableException e) {
                // XXX check for redirect
                /*
                 String redirectLocation;
                 Header locationHeader = get.getResponseHeader("location");
                 if (locationHeader != null) {
                 redirectLocation = locationHeader.getValue();
                 } else {
                 // The response is invalid and did not provide the new location for
                  // the resource.  Report an error or possibly handle the response
                   // like a 404 Not Found error.
                    }
                    */
                _logger.debug("Recoverable exception " + e.getMessage() + " retrying");
                // read response and release connection
                get.getResponseBody();
                get.releaseConnection();
            } catch (IOException io) {
                _logger.info("Failed to download file " + io.getMessage());
                // read response and release connection
                get.getResponseBody();
                get.releaseConnection();
                break;
            }
        }
        
        // should I set the stuff here or elsewhere? XXX synchronize
        scutterm.setHTTPStatus(uri, statusCode);
        
        Header lastModified = get.getResponseHeader("Last-Modified");
        
        if (lastModified != null) {
            // check for 304's XXX
            scutterm.setLastModified(uri, lastModified.getValue());
        }
        
        // last Visited
        scutterm.setLastVisited(uri, System.currentTimeMillis());
        
        // make sure to release connection back to connection manager
        if (get != null)
            get.releaseConnection();
        
        _status = statusCode;
        
        return responseBodyUTF; // statusCode
    }
    
    /**
     * Get robots.txt URI for a given URI
     */
    public Resource constructRobotsURI(Resource uri) {
        StringBuffer robotsURI = new StringBuffer();
        URL robotsTxt = null;
        
        try {
            URL url = new URL(uri.toString());
            
            if (!url.getProtocol().equals("http"))
                return null;
            
            int port = url.getPort();
            robotsURI.append("http://" + url.getHost());
            
            if (!(port == 80 || port == -1))
                robotsURI.append(":" + port);
            
            robotsURI.append("/robots.txt");
            
            robotsTxt = new URL(new String(robotsURI));
        } catch (MalformedURLException e) {
            // something weird is happening, so don't trust it
            return null;
        }
        
        _logger.debug("URL for robots.txt: " + robotsTxt.toString());
        
        return ResourceFactory.createResource(robotsTxt.toString());
    }
    
    
    /**
     * Match robots.txt with url
     * should have something more sophisticated here
     * adapted from from org.osjava.norbert, http://www.osjava.org/releases/official/norbert/
     */
    public boolean robotSafe(Resource uri, String robotsTxt) throws IOException {
        //_logger.debug("robotsSafe() " + uri + " " + robotsTxt);
        
        if (robotsTxt == null)
            return true;
        
        URL url = null;
        
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException mue) {
            _logger.error(mue);
            // don't fetch, be polite just in case
            return false;
        }
        
        String urlFilePart = url.getFile();
        
        // take each line, one at a time
        BufferedReader rdr = new BufferedReader(new StringReader(robotsTxt));
        String line = new String();
        String value = null;
        boolean check = false;
        
        while ((line = rdr.readLine()) != null) {
            // trim whitespace from either side
            line = line.trim();
            // all lowercase
            line = line.toLowerCase();
            
            // ignore startsWith('#')
            if (line.startsWith("#"))
                continue;
            
            // line starts with User-Agent: ?
            if (line.startsWith(USERAGENT)) {
                value = line.substring(USERAGENT.length()).trim();
                //_logger.debug("useragent: " + value);
                // first, extract short name for the scutter
                int index = _userAgentID.indexOf(' ');
                String userAgent = _userAgentID.substring(0, index);
                //_logger.debug("user agent is " + userAgent + ".");
                // determine if our scutter is meant
                if(value.startsWith("*") || value.startsWith(userAgent)) {
                    // yes, then read next line
                    check = true;
                    continue;
                }
            } else if (check == true && line.startsWith(DISALLOW)) {
                value = line.substring(DISALLOW.length()).trim();
                //_logger.debug("disallow: " + value);
                
                // check if this matches the current url file part
                // empty disallow: doesn't mean a thing
                if (!value.equals("") && urlFilePart.startsWith(value))
                    return false;
                // reset check flag
                check = false;
            }
        }
        
        // if not disallowed, assume we can fetch the file
        return true;
    }
}
