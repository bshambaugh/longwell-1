package edu.mit.simile.longwell;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Timer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.mit.simile.RDFBrowser;
import edu.mit.simile.knowle.Knowle;
import edu.mit.simile.longwell.model.FacetModel;
import edu.mit.simile.longwell.model.FacetModelFactory;
import edu.mit.simile.longwell.values.FacetRestriction;
import edu.mit.simile.scutter.Scutter;
import edu.mit.simile.scutter.ScutterModel;
import edu.mit.simile.scutter.SourceModel;
import edu.mit.simile.vocabularies.Display;

/**
 * SimileRepository creates the servlet for the SIMILE repository
 *
 * @author Mark H. Butler
 * @author Stefano Mazzocchi
 */
public class Longwell extends RDFBrowser {
    private static final long serialVersionUID = -6289985926345535965L;
    
    final static public String URL = "longwell";
    final static public String TEMPLATE = "longwell.vt";

    final long TIMER_DELAY = 60000;
    final long TIMER_REPEAT_RATE = 10000;
    
    private FacetModel facetModel;
    
    private String queryType;
    private String indexPath;
    private boolean allowAdd;
    private File configFile;
    private long configFileLastModifiedTime = -1;
    private ConfigurationListener listener;
    private boolean listening = false;
    
    /** scutter variables **/
    private boolean scuttering = false;
    private boolean scutterEnabled;
    private int scutterDepth;
    private Scutter scutter;
    
    private void configure() throws Exception {
    		long startTime, endTime;

    		logger.info("Loading Browser Configuration...");
    		BrowserConfig bc = new BrowserConfig();
    		bc.loadConfiguration(readModel(ModelFactory.createDefaultModel(), configFile, logger)); 

    		this.facetModel = FacetModelFactory.createFacetModel(queryType, model, indexPath, props);
    		this.facetModel.setDefaultLang(bc.getDefaultLang());
    		
    		startTime = System.currentTimeMillis();
    		this.facetModel.configureBrowser(bc);
    		endTime = System.currentTimeMillis();
    		logger.info("  Time to configure browser: " + format(endTime - startTime));
    		
    		startTime = System.currentTimeMillis();
    		this.facetModel.createPreferredTerms();			
    		endTime = System.currentTimeMillis();
    		logger.info("  Time to obtain the preferred terms: " + format(endTime - startTime));
    		
    		startTime = System.currentTimeMillis();
    		this.facetModel.createLangs();
    		endTime = System.currentTimeMillis();
    		logger.info("  Time to obtain languages: " + format(endTime - startTime));

    		startTime = System.currentTimeMillis();
    		this.facetModel.createIndex();
    		endTime = System.currentTimeMillis();
    		logger.info("  Time to build index: " + format(endTime - startTime));
    		
    		if (!scuttering && this.scutterEnabled) {
    		    startTime = System.currentTimeMillis();
    		    ScutterModel scutterm = null;
    		    SourceModel sourcem = null;
    		    FacetModel scutterfm = FacetModelFactory.createFacetModel(props.getProperty("scutter.queryType", "JenaLocalAPIModel"), ModelFactory.createDefaultModel(), props.getProperty("scutter.indexPath", "/WEB-INF/scutter-index"), props);
    		    try {
    		        scutterm = new ScutterModel(scutterfm);
    		        sourcem = new SourceModel(facetModel);
    		    } catch (Exception e) {
    		        e.printStackTrace();
    		        logger.error(e);
    		        System.err.println("Can't open source and/or scutter model!");
    		    }    		
    		    scutter = new Scutter(scutterm, sourcem);
    		    endTime = System.currentTimeMillis();
    		    logger.info("  Time to start scutter: " + format(endTime - startTime));
    		    scuttering = true;
    		}
    		
    		if (!listening) {
    		    Timer configTimer = new Timer();
    		    listener = new ConfigurationListener(bc, configFile, facetModel, logger);
    		    configTimer.schedule(listener, TIMER_DELAY, TIMER_REPEAT_RATE);
    		    logger.info("Configuration file watcher initialized.");
    		    listening = true;
    		}
    		
    		this.facetModel.setDefaultLang("all");
    		logger.info("..done");
    }
        
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        logger.info("Initializing Longwell...");
        
        this.configFile = new File(context.getRealPath("/WEB-INF/config.n3"));
        this.indexPath = context.getRealPath("/WEB-INF/index");
        this.queryType = props.getProperty("queryType");
        this.allowAdd = Boolean.valueOf(props.getProperty("allowAdd", "false")).booleanValue();
        this.scutterEnabled = Boolean.valueOf(props.getProperty("scutter.enabled", "false")).booleanValue();
        this.scutterDepth = Integer.valueOf(props.getProperty("scutter.depth", "0")).intValue();
        
        try {
            configure();
        } catch (Exception e) {
            logger.error("Error initializing Longwell", e);
            throw new ServletException(e.getMessage());
        }       
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		
		int base = (req.getParameter("base") != null) ? Integer.parseInt(req.getParameter("base")) : 0;

		Vector restrictions = new Vector();
		
		Enumeration names = req.getParameterNames();
		
		String addMsg = "";
		
		while (names.hasMoreElements()) {
		    String name = URLDecoder.decode((String) names.nextElement(), "ISO-8859-1");
		    
		    if (name.equals("add") && this.allowAdd) {
		        String[] values = req.getParameterValues(name);
		        
		        try {
		            for (int i = 0; i < values.length; i ++) {
		                String val = URLDecoder.decode(values[i], "ISO-8859-1");
		                if (this.scutterEnabled)
		                    this.scutter.visit(ResourceFactory.createResource(val), this.scutterDepth);
		                else {
		                    URL load = new URL(val);
		                    model.read(load.openStream(), val);
		                }
		                configure();
		            }
		        } catch (Exception e) {
		            logger.info("Error when reading model and reconfiguring: " + e.toString());
		            addMsg = "There was an error loading the requested resource: " + e.toString();
		        }
		    } else if (facetModel.getBrowserConfig().containsFacet(name) || name.equals("any") || name.equals("freetextsearch") || name.equals("focus") || name.equals("lang")) {
		        String[] values = req.getParameterValues(name);
		
		        for (int i = 0; i < values.length; i++) {
		            String uri = URLDecoder.decode(values[i], "ISO-8859-1");
		            FacetRestriction facetRestrict = null;
		
		            if (name.equals("any")) {
		                facetRestrict = new FacetRestriction("any", "any", uri, facetModel.getLabel(uri), generateQueryString(req, name, uri));
		                restrictions.add(facetRestrict);
		            } else if (name.equals("freetextsearch")) {
		                facetRestrict = new FacetRestriction("freetextsearch", "Free text", uri, uri, generateQueryString(req, name, uri));
		                // add free text restrictions to the beginning
		                restrictions.add(0, facetRestrict);
		            } else if (name.equals("focus")) {
		                facetRestrict = new FacetRestriction("focus", "focus", uri, facetModel.getLabel(uri), generateQueryString(req, name, uri));
		                restrictions.add(facetRestrict);
		            } else if (name.equals("lang")) {
		                facetModel.setDefaultLang(uri);
		            } else {
		                // use the browser configuration default language for the facet name language
		                facetRestrict = new FacetRestriction(name, facetModel.getLabel(name, facetModel.getBrowserConfig().getDefaultLang()), uri, facetModel.getLabel(uri), generateQueryString(req, name, uri));
		                restrictions.add(facetRestrict);
		            }
		        }
		    }
		}

		String queryString = generateQueryString(req, null, null);
		
		VelocityContext vcContext = new VelocityContext();
		vcContext.put("query_string", queryString);
		
		vcContext.put("allowadd", Boolean.valueOf(this.allowAdd).toString());
		if (!addMsg.equals("")) {
		    vcContext.put("addmsg", addMsg);
		}
		
		if (req.getParameter("search") != null) {
		    vcContext.put("search", req.getParameter("search"));
		}
		
		if (req.getParameter("style") != null) {
		    vcContext.put("style", req.getParameter("style"));
		}
		
		if (req.getParameter("lang") != null) {
		    vcContext.put("lang", req.getParameter("lang"));
		}
		
		vcContext.put("knowle", Knowle.URL);
		vcContext.put("base", Longwell.URL);

		vcContext.put("browser_config_fields", facetModel.getBrowserConfig().getFields());
		vcContext.put("restrictions", restrictions);
		vcContext.put("langs", facetModel.getLangs());
		
		Vector resultsSet = facetModel.createResultsSet(restrictions);
		int resultsSetSize = resultsSet.size();
		vcContext.put("num_results", new Integer(resultsSetSize));
		
		if (resultsSetSize > (base + 10)) {
		    vcContext.put("next", queryString + "&base=" + (base + 10));
		}
		
		if ((base - 10) >= 0) {
		    vcContext.put("previous", queryString + "&base=" + (base - 10));
		}

		int end = ((base + 10) < resultsSetSize) ? (base + 10) : resultsSetSize;		
		vcContext.put("results", facetModel.createResults(resultsSet, base, end));		
	
		String order = "alphabetic";
		
		if (req.getParameter("sort") != null) {
		    order = req.getParameter("sort");
		}

		vcContext.put("browser_values", facetModel.createFacetNavigator(resultsSet, order));
		vcContext.put("options", facetModel.getBrowserConfig().getAlternateDisplays());
		vcContext.put("DisplayURI", Display.URI);
		vcContext.put("DisplayLink", Display.Link);
		vcContext.put("DisplaySelfValueLink", Display.SelfValueLink);
		vcContext.put("DisplayGetImage", Display.GetImage);
		vcContext.put("DisplayOptionalIcon", Display.OptionalIcon);
		vcContext.put("order", order);
        
		long endTime = System.currentTimeMillis();

		logger.info("Time to process longwell template using " + queryType + ": " + format(endTime - startTime));

		res.setContentType("text/html; charset=UTF-8");		
		
		try {
		    ve.mergeTemplate(Longwell.TEMPLATE, vcContext, res.getWriter());
		} catch (Exception e) {
		    logger.error("Error executing the request. ", e);
		}       
	}
    
    // use excludeName and excludeURI to generate query strings for removing
    // specific facets
    private String generateQueryString(HttpServletRequest req, String excludeName, String excludeURI)
        throws IOException {
        String result = "";
        Enumeration names = req.getParameterNames();

        while (names.hasMoreElements()) {
            String name = URLDecoder.decode((String) names.nextElement(), "ISO-8859-1");
            String[] values = req.getParameterValues(name);

            for (int i = 0; i < values.length; i++) {
                String uri = URLDecoder.decode(values[i], "ISO-8859-1");

                // only add facet/search values to this string 
                if (facetModel.getBrowserConfig().containsFacet(name) || name.equals("any") ||
                        name.equals("freetextsearch") || name.equals("sort") || name.equals("focus") || name.equals("lang")) {
                    if (!(name.equals(excludeName) && uri.equals(excludeURI))) {
                        result += (URLEncoder.encode(name, "ISO-8859-1") + "=" + URLEncoder.encode(uri, "ISO-8859-1") + "&");
                    }
                }
            }
        }

        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }    
}
