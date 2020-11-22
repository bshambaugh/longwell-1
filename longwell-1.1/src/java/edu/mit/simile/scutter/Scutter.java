// (c) 2003 Andreas Harth
// initially based on mattb's hackscutter from hackdiary.com

package edu.mit.simile.scutter;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Main class for the web crawler.
 *
 * @author Andreas Harth
 * $Id: Scutter.java 67 2004-04-27 20:41:59Z aharth $
 */
public class Scutter {
    static Logger _logger = Logger.getLogger(Scutter.class);
    
    // from httpclient library
    private MultiThreadedHttpConnectionManager _conManager;
    private HttpClient _client;
    
    // where the scutter related data is stored
    private ScutterModel _scutterm;
    private SourceModel _sourcem;
    
    // delay
    private static final int DELAYMS = 100;
    
    // store already visited URIs in memory, to prevent multiple fetching
    // for multithreaded environment only?
    private Hashtable _visited;
    
    private String USERAGENT = "SIMILE Scutter (dev@simile.mit.edu)";
    
    private int _depth = 0;
    
    
    /**
     * Constructor.
     */
    public Scutter(ScutterModel scutterm, SourceModel sourcem) {
        _conManager = new MultiThreadedHttpConnectionManager();
        _client = new HttpClient(_conManager);
        
        // timeout 20 seconds
        int timeout = 20*1000;
        _client.setConnectionTimeout(timeout);
        _client.setHttpConnectionFactoryTimeout(timeout) ;
        _client.setTimeout(timeout);
        
        _scutterm = scutterm;
        _sourcem = sourcem;
        
        _visited = new Hashtable();
    }
    
    /**
     * Return true if url is in blacklist.
     */
    private boolean inBlacklist(Resource uri) {
        String url = uri.toString();
        
        if(url.startsWith("http://hugo.ai")) {
            return true;
        }
        if(url.startsWith("http://fireball")) {
            return true;
        }
        if(url.startsWith("http://hampton.ws")) {
            return true;
        }
        if(url.startsWith("http://www.ecademy")) {
            return true;
        }
        if(url.startsWith("http://ecademy")) {
            return true;
        }
        if(url.startsWith("http://www.picdiary")) {
            return true;
        }
        if(url.startsWith("http://picdiary")) {
            return true;
        }
        if(url.startsWith("http://www.kwark.org")) {
            return true;
        }
        if(url.startsWith("http://blogs.thebhg.org")) {
            return true;
        }
        if(url.startsWith("http://triplestore.aktors.org/")) {
            return true;
        }
        if(url.startsWith("http://invite.deanforamerica.com/foafinate.php")) {
            return true;
        }
        if(url.startsWith("http://invite.deanforamerica.com/foafinate.php")) {
            return true;
        }
        if(url.startsWith("http://www.kwark.org/x/person.pl")) {
            return true;
        }
        if(url.startsWith("http://www.meinbild.ch/foaf.rdf?")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * OK to fetch a resource? in blacklist? already visited?
     * another scutter has it?
     */
    public boolean fetchOK(Resource uri, long currentTime) {
        // null? not able to fetch
        if (uri == null)
            return false;
        
        if (inBlacklist(uri) == true)
            return false;
        
        if (_visited.contains(uri))
            return false;
        
        if (otherScutterHasIt(uri))
            return false;
        
        long lastVisited = _scutterm.getLastVisited(uri);
        long updateMillis = _sourcem.getMillis(_sourcem.getUpdatePeriod(uri), _sourcem.getUpdateFrequency(uri));
        
        // check if the url is ready to get fetched
        if (currentTime > lastVisited + updateMillis)
            return true;
        else
            _logger.debug(uri + " update in " + ((lastVisited + updateMillis - currentTime)/1000) + " secs.");
        
        return false;
    }
    
    
    /**
     * Check whether other nodes has the url already fetched.
     */
    public boolean otherScutterHasIt(Resource uri) {
        //...
        // other scutters doesn't have i
        return false;
    }
    
    // XXX implement breadth-first search here (plus greed)
    // link structure is in ScutterModel
    // what happens when there are circles in the graph? then it's circling forever
    // solution: do choose randomly? ie. scramble the sequence of seeAlsos
    // increase rank of visited node by one? (this helps to find popular nodes)
    // check the subjects from the source model
    // XXX there is an intersection between source model and scutter model
    // get graph structure from source model, and update stuff from the meta model
    
    
    /**
     * Visit a resource and fetch subsequent URI's that
     * are connected via rdfs:seeAlso using breadth-first search.
     *
     * @parameter uri uri to visit
     * @parameter maxDepth depth of breadth-first crawl
     */
    public void visit(Resource uri, int maxDepth) throws Exception {
        visit(uri, maxDepth, _scutterm, _sourcem);
    }
    
    /**
     * Visit a resource and fetch subsequent URI's that
     * are connected via rdfs:seeAlso using breadth-first search.
     *
     * @parameter uri uri to visit
     */
    private void visit(Resource uri, int maxDepth, ScutterModel scutterm, SourceModel sourcem) throws Exception {
        // pick random subject
        if (uri == null) {
            _logger.debug("uri is null");
            return;
        }
        
        // fetch uri
        // XXX FIXME fetchOK and check for time needs to be conducted in
        // the SourceModel!!! because there is the updateFrequency thing
        // defined!!!
        if (fetchOK(uri, System.currentTimeMillis()))
            fetch(uri, scutterm, sourcem);
        
        // get the outgoing pointers
        Iterator sa = sourcem.getSeeAlsos(uri);
        
        // no more seeAlsos? stop this branch
        if (sa.hasNext() == false)
            return;
        
        // fetch outgoing rdfs:seeAlsos
        while (sa.hasNext()) {
            Resource res = (Resource)sa.next();
            _logger.debug(uri + " seeAlso " + res);
            if (fetchOK(res, System.currentTimeMillis()))
                fetch(res, scutterm, sourcem);
        }
        
        // visit outgoing rdfs:seeAlsos
        sa = sourcem.getSeeAlsos(uri);
        
        // XXX check here whether we are circling
        
        while (sa.hasNext()) {
            Resource res = (Resource)sa.next();
            _logger.debug(uri + " seeAlso " + res);
            // recursion
            if (res.equals(uri))
                _logger.error("res equals uri: bad, would be looping forever, but shouldn't happen anyways");
            else {
                if (_depth < maxDepth-1) {
                    System.out.println("recursion");
                    visit(res, maxDepth, scutterm, sourcem);
                    _depth++;
                } else {
                    return;
                }
            }
        }
    }
    
    /**
     * Just fetch a resource, no check on robots.txt or lastVisited
     * or blacklist, also no link traversal.
     * For convenience only takes a Resource as parameter.
     */
    public void fetch(Resource uri) throws Exception {
        fetch(uri, _scutterm, _sourcem);
    }
    
    /**
     * Fetch a resource.
     * never returns, starts new thread.
     * 
     * @ parameter uri URI to fetch
     */
    private void fetch(Resource uri, ScutterModel scutterm, SourceModel sourcem) throws Exception {
        _logger.debug("about to fetch " + uri);
        
        if (uri != null) {
            // retrieve asynchronous
            // XXX already done in ScutterModel setCrawlInProgress(uri, true);
            HttpRetriever retriever = new HttpRetriever(_client, USERAGENT, uri, scutterm, sourcem);
            
            _visited.put(uri, "1");
            
            // go
            retriever.run();
        }
    }
}
