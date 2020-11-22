package edu.mit.simile.longwell;

import edu.mit.simile.longwell.model.FacetModel;
import edu.mit.simile.longwell.values.LabelledResource;
import edu.mit.simile.longwell.values.OptionCollection;
import edu.mit.simile.longwell.values.ResourceImage;
import edu.mit.simile.longwell.values.ResourceImageCollection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.mit.simile.vocabularies.Display;

/**
 * This class holds the browser configuration data
 *
 * @author Mark H. Butler
 * @author Ryan Lee
 */
public class BrowserConfig {
    // requires some sort of default language; most of the stuff out there is English
    private String LANG = "en";
    
    // which properties to display in the results pane
    private Vector fields;

    // which properties to display in the facet navigation pane    
    private Vector facets;

    // the facet names
    private HashSet facetNames;

    // alternate display properties
    private HashMap alternatePropertyDisplays;
    private OptionCollection alternateDisplays;

    // the classes to display
    private Vector configuredClasses;

    // some temporary vectors used during object creation
    private Vector tempFields;
    private Vector tempFacets;

    // whether the facet values are displayed with hierarchical info or not
    private boolean hierarchical;
    
    // default system display language
    private String defaultLang;
    
    /**
     * Creates a new BrowserConfig object.
     */
    BrowserConfig() {
        tempFields = new Vector();
        tempFacets = new Vector();
        configuredClasses = new Vector();
        alternatePropertyDisplays = new HashMap();
        alternateDisplays = new OptionCollection();
        defaultLang = "";
    }

    public void reset() {
        fields.clear();
        facets.clear();
        facetNames.clear();
        alternatePropertyDisplays.clear();
        alternateDisplays.clear();
        configuredClasses.clear();
        tempFields.clear();
        tempFacets.clear();
        defaultLang = "";
    }
    
    /**
     * Add a field to the browser configuration.
     *
     * @param p The RDF property used for the field.
     * @param isFacet Is this a facet or not?
     */
    public void addField(Property p, boolean isFacet) {
        tempFields.add(p.getURI());

        if (isFacet) {
            tempFacets.add(p.getURI());
        }
    }

    /**
     * Configure the browser to display instances of this class
     *
     * @param r An RDF resource indicating the class.
     */
    public void addConfiguredClass(Resource r) {
        configuredClasses.add(r.getURI());
    }

    /**
     * Add an alternate display type for a property.
     *
     * @param prop  The property
     * @param type  The type of display
     */
    public void addAlternatePropertyDisplay(Resource prop, Resource type) {
        alternatePropertyDisplays.put(prop.getURI(), type.getURI());
    }

    /**
     * Add an alternate display for a resource that's an object of a
     * certain property.
     *
     * @param m     The configuration model
     * @param prop  The property
     * @param obj   The object
     */
    public void addAlternateDisplay(Model m, Resource prop, Resource obj) {
        ResourceImageCollection opts = alternateDisplays.containsKey(prop.getURI())
            ? (ResourceImageCollection) alternateDisplays.get(prop.getURI())
            : new ResourceImageCollection();

        StmtIterator si = m.listStatements(obj, RDF.type, (Resource) null);

        if (si.hasNext()) {
            Resource type = si.nextStatement().getResource();
            si = m.listStatements(obj, Display.displayAs, (Resource) null);

            if (si.hasNext()) {
                ResourceImage img = new ResourceImage(m, si.nextStatement().getResource());

                if (type.equals(Display.Option)) {
                    opts.put(obj.getURI(), img);
                } else if (type.equals(Display.DefaultOption)) {
                    opts.put(null, img);
                }
            }
        }

        alternateDisplays.put(prop.getURI(), opts);
    }

    /**
     * 
     * @param hierarchy
     */
    public void setHierarchical(boolean hierarchy) {
        this.hierarchical = hierarchy;
    }
    
    /**
     * 
     * @param lang
     */
    public void setDefaultLang(String lang) {
        this.defaultLang = lang;
    }
    
    /**
     * Final stage creation of the BrowserConfig object.
     *
     * @param model The query model used for final stage creation.
     */
    public void configure(FacetModel model) {
        fields = makeBrowserFields(model, tempFields);
        facets = makeBrowserFields(model, tempFacets);
        facetNames = new HashSet(tempFacets);
    }

    /**
     * Create a Vector of browser fields.
     *
     * @param model The query model used for final stage creation.
     * @param URIs The URIs of the fields.
     *
     * @return The browser fields as a Vector of LabelledResources.
     */
    private Vector makeBrowserFields(FacetModel model, Vector URIs) {
        Vector results = new Vector();
        Iterator i = URIs.iterator();

        while (i.hasNext()) {
            String uri = (String) i.next();

            if (alternatePropertyDisplays.containsKey(uri)) {
                results.add(new LabelledResource(uri, model.getLabel(uri),
                        (String) alternatePropertyDisplays.get(uri)));
            } else {
                results.add(new LabelledResource(uri, model.getLabel(uri)));
            }
        }

        return results;
    }

    /**
     * Get the browser fields
     *
     * @return A Vector of fields as LabelledResources.
     */
    public Vector getFields() {
        return fields;
    }

    /**
     * Get the browser facets.
     *
     * @return A Vector of facets as LabelledResources.
     */
    public Vector getFacets() {
        return facets;
    }

    /**
     * Get the classes that the browser has been configured to display.
     *
     * @return A Vector of URIs of the configured classes.
     */
    public Vector getConfiguredClasses() {
        return configuredClasses;
    }

    public OptionCollection getAlternateDisplays() {
        return alternateDisplays;
    }

    /**
     * Has the browser been configured to display this Facet?
     *
     * @param name The URI of the Facet
     *
     * @return
     */
    public boolean containsFacet(String name) {
        return facetNames.contains(name);
    }

    /**
     * 
     * @return
     */
    public boolean isHierarchical() {
        return this.hierarchical;
    }
    
    /**
     * 
     * @return
     */
    public String getDefaultLang() {
        return this.defaultLang;
    }
    
    /**
     * Load the configuration information from an RDF file.
     *
     * @param m  The Jena model containing the configuration information
     *
     * @author Ryan Lee
     */
    public void loadConfiguration(Model m) throws Exception {
    	
        // find the base configuration
        StmtIterator si = m.listStatements((Resource) null, RDF.type, Display.BrowserConfiguration);

        if (si.hasNext()) {
            Resource config = si.nextStatement().getSubject();

            // then query the model for separate parts for filling
            // out this object
            StmtIterator ci = m.listStatements(config, Display.facetValueOrganizeAs, (Resource) null);
            
            if (ci.hasNext()) {
                Resource r = (Resource) ci.nextStatement().getObject();
                if (r.getURI().equals(Display.HierarchicalOrganization.getURI())) {
                    setHierarchical(true);
                } else {
                    setHierarchical(false);
                }
            }
            
            // language
            ci = m.listStatements(config, Display.lang, (RDFNode) null);
            setDefaultLang(LANG);
            while (ci.hasNext()) {
                String lang = (String) ci.nextStatement().getString();
                setDefaultLang(lang);
            }
            
            // look for properties to display
            ci = m.listStatements(config, Display.displayProperties, (Resource) null);

            while (ci.hasNext()) {
                List displayList = ((RDFList) ((Resource) ci.nextStatement().getObject()).as(RDFList.class)).asJavaList();
                Iterator i = displayList.iterator();
                while (i.hasNext()) {
                    Resource r = (Resource) i.next();
                    addField(m.createProperty(r.getURI()), false);
                }
            }

            // look for facets to display
            ci = m.listStatements(config, Display.displayFacets, (Resource) null);

            while (ci.hasNext()) {
                List displayList = ((RDFList) ((Resource) ci.nextStatement().getObject()).as(RDFList.class)).asJavaList();
                Iterator i = displayList.iterator();
                while (i.hasNext()) {
                    Resource r = (Resource) i.next();
                    addField(m.createProperty(r.getURI()), true);
                }
            }

            // look for classes to display
            ci = m.listStatements(config, Display.displayClasses, (Resource) null);

            while (ci.hasNext()) {
                List displayList = ((RDFList) ((Resource) ci.nextStatement().getObject()).as(RDFList.class)).asJavaList();
                Iterator i = displayList.iterator();
                while (i.hasNext()) {
                    Resource r = (Resource) i.next();
                    addConfiguredClass(r);
                }
            }

            // look for alternate display types
            ci = m.listStatements(config, Display.propertyObjectDisplay, (Resource) null);

            while (ci.hasNext()) {
                List displayList = ((RDFList) ((Resource) ci.nextStatement().getObject()).as(RDFList.class)).asJavaList();
                Iterator i = displayList.iterator();
                while (i.hasNext()) {
                    Resource r = (Resource) i.next();
                    Resource o = m.getProperty(r, RDF.type).getResource();
                    addAlternatePropertyDisplay(r, o);

                    if (o.equals(Display.RequiredIcon) || o.equals(Display.OptionalIcon)) {
                        // make a new alternateDisplay
                        StmtIterator ssi = m.listStatements(r, Display.choices, (Resource) null);

                        while (ssi.hasNext()) {
                            List opts = ((RDFList) ((Resource) ssi.nextStatement().getObject()).as(RDFList.class)).asJavaList();
                            Iterator sni = opts.iterator();

                            while (sni.hasNext()) {
                                Resource alt = (Resource) sni.next();
                                addAlternateDisplay(m, r, alt);
                            }
                        }
                    }
                }
            }
        }
    }
}
