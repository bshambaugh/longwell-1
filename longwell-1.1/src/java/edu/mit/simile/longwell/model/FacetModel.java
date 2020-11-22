package edu.mit.simile.longwell.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import com.hp.hpl.jena.rdf.arp.lang.Iso3166;
import com.hp.hpl.jena.rdf.arp.lang.Iso639;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.values.FacetValue;
import edu.mit.simile.longwell.values.LabelledResource;
import edu.mit.simile.longwell.values.LabelledResourceCollection;
import edu.mit.simile.longwell.values.MultiOrderLabelledResourceCollection;
import edu.mit.simile.vocabularies.Display;


/**
 * This is the superclass for all the query engines.
 *
 * @author Mark H. Butler
 */

/*
 The lifecycle for this class is as follows:
        constructor();
        configureBrowser();
        createIndex();
 */
public abstract class FacetModel {
	
    protected BrowserConfig bc;
    protected Model model;
    protected HashSet primaryEquivalences;
    protected HashSet secondaryEquivalences;
    protected String indexPath;
    protected IndexSearcher searcher;
    protected HashMap langs;
    protected String defaultLang;

    public FacetModel(Model model, String indexPath) throws Exception {
        this.model = model;
        this.indexPath = indexPath;
        primaryEquivalences = new HashSet();
        secondaryEquivalences = new HashSet();
    }

    /**
     * Configure the browser configuration with label information
     */
    public void configureBrowser(BrowserConfig bc) throws Exception {
    		this.bc = bc;
    		bc.configure(this);
    }

    /**
     * Return the Browser Configuration associated with this FacetModel
     *
     * @return The Browser Configuration.
     */
    public BrowserConfig getBrowserConfig() {
        return bc;
    }

    /**
     * 
     * @return
     */
    public Model getModel() {
        return this.model;
    }
    
    /**
     * 
     * @return
     */
    public HashMap getLangs() {
        return this.langs;
    }
    
    /**
      * Get the label for a URI.
      *
      * @param uri
      * @return The label
      */
    public String getLabel(String uri) {
        Resource r = extractResource(model, uri);

        if (r.hasProperty(RDFS.label)) {
            return getLabel(r, RDFS.label);
        } else if (r.hasProperty(DC.title)) {
            return getLabel(r, DC.title);
        } else {
            return null;
        }
    }
    
    /**
     * 
     * @param uri
     * @param lang
     * @return
     */
    public String getLabel(String uri, String lang) {
        Resource r = extractResource(model, uri);

        if (r.hasProperty(RDFS.label)) {
            return getLabel(r, RDFS.label, lang);
        } else if (r.hasProperty(DC.title)) {
            return getLabel(r, DC.title, lang);
        } else {
            return null;
        }        
    }
     
    /**
     * 
     * @param r
     * @param p
     * @return
     */
    public String getLabel(Resource r, Property p) {
        return getLabel(r, p, this.defaultLang);
    }
    
    /**
     * 
     * @param r
     * @param p
     * @param lang
     * @return
     */
    public String getLabel(Resource r, Property p, String lang) {
        String result = null;
        String general = null;
        String backup = null;
        StmtIterator stmts = model.listStatements(r, p, (RDFNode) null);
        while (stmts.hasNext()) {
            Statement stmt = stmts.nextStatement();
            if (lang != null && stmt.getLiteral().getLanguage().equals(lang))
                result = stmt.getString().trim();
            else if (lang != null && stmt.getLiteral().getLanguage() != null && stmt.getLiteral().getLanguage().length() >= 2 && stmt.getLiteral().getLanguage().substring(0,2).equals(lang.substring(0,2)))
                general = stmt.getString().trim();
            else
                backup = stmt.getString().trim();
        }
        return ((result == null) ? ((general == null) ? backup : general ) : result);
    }

    /**
     * 
     * @param lang
     */
    public void setDefaultLang(String lang) {
        this.defaultLang = lang;
    }

    /**
     * return the subclass for this term if there is one
     *
     * @param uri
     * @return The URI of the subclass.
     */
    public String getSubClass(String uri) {
        Resource r = extractResource(model, uri);

        if (r.hasProperty(RDFS.subClassOf)) {
            return ((Resource) r.getProperty(RDFS.subClassOf).getObject()).getURI();
        } else {
            return null;
        }
    }

    /**
     * This creates a vector of languages used by literals in the model.
     */
    public void createLangs() {
        langs = new HashMap();
        langs.put("all", "All (default)");
        NodeIterator nodes = model.listObjects();
        while (nodes.hasNext()) {
            RDFNode node = nodes.nextNode();
            if (node.canAs(Literal.class)) {
                Literal l = (Literal) node.as(Literal.class);
                String langcode = l.getLanguage();
                Iso639 lang = null;
                Iso3166 country = null;
                if (langcode.length() == 5) {
                    lang = Iso639.find(langcode.substring(0,2));
                    country = Iso3166.find(langcode.substring(3,5).toLowerCase());
                } else {
                    lang = Iso639.find(langcode);
                }
                if (lang != null && !langs.containsKey(langcode)) {
                    langs.put(langcode, lang.name + ((country != null) ? " (" + country.name + ")" : ""));
                }
            }
        }
    }
    
    /**
     * This creates a list of equivalent terms so we can hide them in the UI.
     */
    public void createPreferredTerms() {
        StmtIterator statements = model.listStatements((Resource) null, RDF.type,
                Display.PreferredTerm);

        while (statements.hasNext()) {
            Statement statement = statements.nextStatement();
            Resource pref = (Resource) statement.getSubject();
            primaryEquivalences.add(pseudoUri(pref));

            // a secondary term is any term that is subject to a sameAs relation
            // but is not a preferred term
            StmtIterator secondaries = model.listStatements(pref, OWL.sameAs, (RDFNode) null);

            while (secondaries.hasNext()) {
                Statement secondStatement = secondaries.nextStatement();
                secondaryEquivalences.add(pseudoUri((Resource) secondStatement.getObject()));
            }
        }
    }

    public void createIndex() throws Exception {
        IndexWriter writer = new IndexWriter(indexPath, new SimpleAnalyzer(), true);

        // for each configured object type
        Iterator j = bc.getConfiguredClasses().iterator();

        while (j.hasNext()) {
            Resource t = model.getResource((String) j.next());

            // for each objects in the model of that type
            ResIterator i = model.listSubjectsWithProperty(RDF.type, t);

            while (i.hasNext()) {
                Resource r = i.nextResource();
                Document doc = new Document();
                if (null != r.getURI())
                	doc.add(Field.Keyword("uri", r.getURI()));
                else
                	doc.add(Field.Keyword("uri", pseudoUri(r)));

                // for each configured field
                Iterator k = bc.getFields().iterator();

                while (k.hasNext()) {
                    LabelledResource browserField = (LabelledResource) k.next();
                    Property p = model.getProperty(browserField.getResource());

                    if (r.hasProperty(p)) {
                        // copy multiple property values 
                        StmtIterator l = r.listProperties(p);

                        while (l.hasNext()) {
                            Statement q = l.nextStatement();

                            // need to check here if this is a resource or not ... 
                            RDFNode n = q.getObject();
                            String u;

                            if (n instanceof Resource) {
                                if (((Resource) n).hasProperty(RDFS.label)) {
                                    u = ((Resource) n).getProperty(RDFS.label).getString().trim();
                                } else if (null != ((Resource) n).getURI()) {
                                    u = ((Resource) n).getURI();
                                } else {
                                		u = pseudoUri((Resource) n);
                                }
                            } else {
                                u = q.getString().trim();
                            }

                            doc.add(Field.Text("search", u));
                        }
                    }
                }

                writer.addDocument(doc);
            }
        }

        writer.optimize();
        writer.close();

        searcher = new IndexSearcher(indexPath);
    }

    /**
     * Is this term equivalent to other terms?
     *
     * @param uri
     * @return
     */
    public boolean equivalentTerm(String uri) {
        return primaryEquivalences.contains(uri);
    }

    /**
     * Is this a secondary equivalent term so should it be hidden in the UI?
     *
     * @param uri
     * @return
     */
    public boolean hideEquivalent(String uri) {
        return secondaryEquivalences.contains(uri);
    }

    /**
      * Create the data used in the result pane
      *
      * @param hits All the results
      * @param base The number of the first result
      * @param end The number of the last result
      * @return
      */
    public Vector createResults(Vector hits, int base, int end)
        throws IOException {
        Vector documents = new Vector();
        int count = 0;
        Iterator iter = hits.iterator();

        while (iter.hasNext()) {
            String hit = (String) iter.next();

            if ((count >= base) && (count < end)) {
                HashMap document = new HashMap();
                Iterator browserFields = bc.getFields().iterator();

                while (browserFields.hasNext()) {
                    LabelledResource browserField = (LabelledResource) browserFields.next();
                    Vector results = getObjectField(hit, browserField);

                    if (results.size() > 0) {
                        document.put(browserField.getResource(),
                            new LabelledResourceCollection(browserField, results));
                    }
                }

                // the second argument (label) doesn't really matter right now
                LabelledResource self = new LabelledResource(hit, hit);
                
                // now get inbound links
                Vector inbound = getInboundLinks(hit);
                
                Result result = new Result(self, document, inbound);
                documents.add(result);
            }

            count++;
        }

        return documents;
    }

    /**
      * Create the facet navigator from the search results
      *
      * @param hits All the results
      * @return
      */
    public Vector createFacetNavigator(Vector hits, String order)
        throws IOException {
        Vector browser = new Vector();
        Iterator facets = bc.getFacets().iterator();

        // for each facet type
        while (facets.hasNext()) {
            LabelledResource facet = (LabelledResource) facets.next();
            MultiOrderLabelledResourceCollection facetValues = new MultiOrderLabelledResourceCollection(facet,
                    createFacetValues(hits, facet).values(), hits.size(), order);

            if (facetValues.getSize() > 0) {
                browser.add(facetValues);
            }
        }

        return browser;
    }

    protected abstract Vector getObjectField(String objectURI, LabelledResource browserField);

    protected abstract InboundLinks getInboundLinks(String objectURI);
    
    protected abstract FacetValues createFacetValues(Vector hits, LabelledResource facet);

    public abstract Vector createResultsSet(Vector restrictions)
        throws IOException;

    protected boolean isAnonymous(RDFNode node) {
    		boolean answer = false;
    		if(node.canAs(Resource.class)) {
    			if(((Resource) node.as(Resource.class)).isAnon()) {
    				answer = true;
    			}
    		}
    		return answer;
    }
    
    protected String pseudoUri(Resource res) {
    		String uri = null;
		if (res.isAnon()) {
			uri = "http://simile.mit.edu/anonymous#" + res.getId();
		} else {
			uri = res.getURI();
		}
		return uri;
    }
    
    protected Resource extractResource(Model model, String uri) {
    		Resource s = null;
    		if(uri != null) {
    			if (uri.startsWith("http://simile.mit.edu/anonymous#")) {
    				s = model.createResource(new AnonId(uri.substring(uri.indexOf("#") + 1)));
    			} else {
    				s = model.getResource(uri);
    			}
    		}
    		return s;
    }
    
    // a helper class for the construction of fieldvalues
    // this is used by classes that inherit from FacetModel. 
    // using an inner class like this isn't ideal, the other option is to pass this FacetModel
    // to it at create time. This is necessary so that it calls the correct version of
    // getLabel which is FacetModel dependent. This approach is used in FacetValues also
    // but is messy because it means there is a reflexive relation between FacetModel and
    // FacetValues. 
    public class FieldValues extends Vector {
        private static final long serialVersionUID = -7222377385369361606L;

        public FieldValues() {
            super();
        }

        public void addLabelledResource(String s, LabelledResource browserField) {
            if (bc.containsFacet(browserField.getResource())) {
                if (!hideEquivalent(s)) {
                    LabelledResource value = new LabelledResource(s, getLabel(s));
                    add(value);
                }
            } else {
                LabelledResource value = new LabelledResource(s);
                add(value);
            }
        }

        public void addLabelledResource(RDFNode r, LabelledResource browserField) {
            if (bc.containsFacet(browserField.getResource())) {
                String s = pseudoUri((Resource) r);

                if (!hideEquivalent(s)) {
                		LabelledResource value = new LabelledResource(s, getLabel(s));
                		add(value);
                }
            } else if (r instanceof Resource) {
        			String uri = pseudoUri((Resource) r);
        			LabelledResource value = new LabelledResource(uri, getLabel(uri));
        			add(value);
            } else {
                String s = ((Literal) r).getString();
                LabelledResource value = new LabelledResource(s);
                add(value);
            }
        }
    }

    /**
     * A collection of facet values
     *
     * @author Mark H. Butler
     */
    public class FacetValues extends HashMap {
        private static final long serialVersionUID = 603241099775818277L;

        /**
         * Creates a new FacetValues object.
         */
        public FacetValues() {
            super();
        }

        /**
         * Add a term to this collection of facet values
         *
         * @param model The current FacetModel
         * @param termURI The URI of the term
         */
        public void add(String termURI) {
            if (!hideEquivalent(termURI)) {
                if (!bc.isHierarchical()) {
                    if (this.containsKey(termURI)) {
                        // the term already exists
                        FacetValue value = (FacetValue) get(termURI);
                        value.increment();
                    } else {
                        String termLabel = getLabel(termURI);
                        FacetValue newValue = null;
                        if (equivalentTerm(termURI)) {
                            newValue = new FacetValue(termURI, termLabel, true);
                        } else {
                            newValue = new FacetValue(termURI, termLabel);
                        }
                        put(termURI, newValue);
                    }
                } else {
                    if (this.containsKey(termURI)) {
                        // the term already exists
                        FacetValue value = (FacetValue) get(termURI);
                        value.increment();
                    } else {
                        String broaderTermURI = getSubClass(termURI);
                        
                        if (broaderTermURI == null) {
                            // there is no broader term for this term
                            // but it does not exist so create the term		   
                            String termLabel = getLabel(termURI);
                            FacetValue newValue = null;
                            
                            if (equivalentTerm(termURI)) {
                                newValue = new FacetValue(termURI, termLabel, true);
                            } else {
                                newValue = new FacetValue(termURI, termLabel);
                            }
                            
                            put(termURI, newValue);
                        } else {
                            // there is a broader term for this term
                            if (containsKey(broaderTermURI)) {
                                // it already exists
                                FacetValue value = (FacetValue) get(broaderTermURI);
                                
                                if (value.contains(termURI)) {
                                    value.narrowerTermIncrement(termURI);
                                } else {
                                    value.add(termURI, getLabel(termURI));
                                }
                            } else {
                                String broaderTermLabel = getLabel(broaderTermURI);
                                FacetValue newValue = new FacetValue(broaderTermURI, broaderTermLabel,
                                        termURI, getLabel(termURI));
                                put(broaderTermURI, newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 
     * 
     * @author Ryan Lee
     */
    public class InboundLinks extends Vector {
        private static final long serialVersionUID = 6369755826284898998L;

        public InboundLinks() {
            super();
        }

        public void addInboundLink(RDFNode r, Property p) {
        		String subjectUri = pseudoUri((Resource) r);
        		String propertyUri = pseudoUri((Resource) p);
        		LabelledResource subjectValue = new LabelledResource(subjectUri, getLabel(subjectUri));
        		LabelledResource propertyValue = new LabelledResource(propertyUri, getLabel(propertyUri));
        		addInboundLink(subjectValue, propertyValue);
        }
        
        public void addInboundLink(LabelledResource subject, LabelledResource property) {
            Vector pair = new Vector();
            pair.add(subject);
            pair.add(property);
            if(!contains(pair)) add(pair);
        }
    }
}
