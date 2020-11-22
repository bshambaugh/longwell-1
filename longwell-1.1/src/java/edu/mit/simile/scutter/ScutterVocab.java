// (c) 2003, 2004 Andreas Harth

package edu.mit.simile.scutter;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary used by the scutter.
 * XXX provide doc in HTML that describes the scutter vocab.
 *
 * @author Andreas Harth
 * $Id: ScutterVocab.java 67 2004-04-27 20:41:59Z aharth $
 */
public class ScutterVocab extends Object {
    protected static final String _source = "http://www.semanticweb.org/2003/seco/sourceModel#";
    protected static final String _scutter = "http://www.semanticweb.org/2003/seco/scutterModel#";

    public static final Property updateFrequency = ResourceFactory.createProperty("http://purl.org/rss/1.0/modules/syndication/updateFrequency");
    public static final Property updatePeriod = ResourceFactory.createProperty("http://purl.org/rss/1.0/modules/syndication/updatePeriod");

    public static final Property provenance = ResourceFactory.createProperty("http://www.hackdiary.com/foaf/source");

    // meta model
    public static final Property lastVisited = ResourceFactory.createProperty(_scutter + "lastVisited");
    public static final Property httpLastModified = ResourceFactory.createProperty(_scutter + "httpLastModified");
    public static final Property httpStatus = ResourceFactory.createProperty(_scutter + "httpStatus");
    public static final Property validRDF = ResourceFactory.createProperty(_scutter+ "validRDF");

    public static final Resource invalidRDF = ResourceFactory.createResource(_scutter + "invalid");
    public static final Resource validNotSpecified = ResourceFactory.createResource(_scutter + "notspec");

    //    public static final Property robotsLastVisited = ResourceFactory.createProperty(_scutter + "robotsLastVisted");
    public static final Property robotsTxt = ResourceFactory.createProperty(_scutter + "robotsTxt");

    public static final Property crawlInProgress = ResourceFactory.createProperty(_scutter + "crawlInProgess");


    // for updatePeriod
    public static String updatePeriodNotSpecified = "notspecified";
    public static String lastModifiedNotSpecified = "notspecified";
    public static String robotsTxtNotSpecified = "notspecified";

    // for httpStatus
    public static int statusNotSpecified = -1;

    public static int lastLoginNotSpecified = -1;
    public static int lastLogoutNotSpecified = -1;

    public static long lastVisitNotSpecified = -1;

    //public static long robotsLastVisitNotSpecified = -1;

    // for updateFrequency
    public static int updateFreqNotSpecified = -1;

    // crawl in progress
    //public static int crawlInProgress = 4711;

    // crawl in progress
    public static int crawlNow = 5000;

    // RDF is valid (and in SourceModel)
    public static final Resource valid = ResourceFactory.createResource(_scutter + "valid");

    public static final long rankNotSpecified = -1;

    // URL excluded from crawling, either robots.txt or blacklist
    public static final Resource excluded = ResourceFactory.createResource(_scutter + "excluded");

    // RDF not yet validated
    public static final Resource notValidated = ResourceFactory.createResource(_scutter + "notValidated");

    // Error in validation
    public static final Resource validationError = ResourceFactory.createResource(_scutter + "validationError");
}
