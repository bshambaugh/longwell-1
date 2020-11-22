/*
 * Created on Oct 20, 2004
 */
package edu.mit.simile.longwell.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.values.FacetRestriction;
import edu.mit.simile.longwell.values.LabelledResource;
import edu.mit.simile.vocabularies.Display;


/**
 * A facet model based on remote, network-accessed stores.
 * 
 * @author ryanlee
 */
public abstract class RemoteFacetModel extends FacetModel {
    protected Vector _objects;
    protected Model _cachedModel;
    
    public RemoteFacetModel(Model model, String indexPath) throws Exception {
        super(model, indexPath);
    }

    public void configureBrowser(BrowserConfig bc) throws Exception {		
        this._objects = new Vector();
        this._cachedModel = ModelFactory.createDefaultModel();
        createCachedModel(bc);
        super.configureBrowser(bc);
    }

    protected abstract void createCachedModel(BrowserConfig bc) throws Exception;
    
    /**
     * Get the label for a URI.
     *
     * @param uri
     * @return The label
     */
    public String getLabel(String uri) {
        Resource r = this._cachedModel.getResource(uri);
        
        if (r.hasProperty(RDFS.label)) {
            return r.getProperty(RDFS.label).getString().trim();
        } else if (r.hasProperty(DC.title)) {
            return r.getProperty(DC.title).getString().trim();
        } else {
            return null;
        }
    }
        
    /**
     * return the subclass for this term if there is one
     *
     * @param uri
     * @return The URI of the subclass.
     */
    public String getSubClass(String uri) {
        Resource res = this._cachedModel.getResource(uri);
        
        if (res.hasProperty(RDFS.subClassOf)) {
            Resource r = (Resource) res.getProperty(RDFS.subClassOf).getObject();
            
            return r.getURI();
        } else {
            return null;
        }
    }
    
    /**
     * Extract a Resource from a model given a URI (may be anonymous)
     * 
     * @param model
     * @param uri
     */
    protected Resource extractResource(Model model, String uri) {
        Resource s = null;
        if(uri != null) {
            if (uri.startsWith("http://simile.mit.edu/anonymous#")) {
                s = this._cachedModel.createResource(new AnonId(uri.substring(uri.indexOf("#") + 1)));
            } else {
                s = this._cachedModel.getResource(uri);
            }
        }
        return s;
    }
    
    /**
     * This returns all the values of a particular field in one member of the results set.
     *
     * @param objectURI The member of the results set.
     * @param browserField The field.
     *
     * @return
     */
    protected Vector getObjectField(String objectURI, LabelledResource browserField) {
        Resource object = this._cachedModel.getResource(objectURI);
        Property p = this._cachedModel.getProperty(browserField.getResource());
        FieldValues fieldValues = new FieldValues();
        
        if (object.hasProperty(p)) {
            StmtIterator fieldValueIter = object.listProperties(p);
            
            while (fieldValueIter.hasNext()) {
			    RDFNode obj = fieldValueIter.nextStatement().getObject();
			    if (obj.canAs(Resource.class)) {
			        if (!((Resource) obj).hasProperty(RDF.type, OWL.Restriction))
				        fieldValues.addLabelledResource(obj, browserField);
			    } else {
			        if (this.defaultLang != null) {
			            if (((Literal) obj.as(Literal.class)).getLanguage().equals(this.defaultLang)) {
			                fieldValues.addLabelledResource(obj, browserField);
			            }
			        } else
			            fieldValues.addLabelledResource(obj, browserField);
			    }
            }
        }
        
        return fieldValues;
    }
    
    /**
     * This returns all the inbound links as a <code>Vector</code>.
     * 
     * @param objectURI  The member of the results set.
     * 
     * @return
     */
    protected InboundLinks getInboundLinks(String objectURI) {
        InboundLinks links = new InboundLinks();
        Resource object = this._cachedModel.getResource(objectURI);
        StmtIterator si = this._cachedModel.listStatements((Resource) null, (Property) null, (RDFNode) object);
        while (si.hasNext()) {
            Statement s = si.nextStatement();
            Resource subject = s.getSubject();
            Property predicate = (Property) s.getPredicate();
            Property inverse = null;
            if (this._cachedModel.contains(predicate, OWL.inverseOf, (RDFNode) null)) {
                StmtIterator i = this._cachedModel.listStatements((Resource) predicate, OWL.inverseOf, (RDFNode) null);
                // precludes multiple inverses
                Resource inverseRes = (Resource) i.nextStatement().getObject();
                inverse = model.getProperty(inverseRes.getURI()); 
            } else if (this._cachedModel.contains((Resource) null, OWL.inverseOf, (RDFNode) predicate)) {
                StmtIterator i = this._cachedModel.listStatements((Resource) null, OWL.inverseOf, (RDFNode) predicate);
                // precludes multiple inverses
                Resource inverseRes = i.nextStatement().getSubject();	
                inverse = this._cachedModel.getProperty(inverseRes.getURI());
            } else {
	            if (!subject.hasProperty(RDF.type, OWL.Restriction) && (subject.hasProperty(RDF.type, Display.PreferredTerm) || !subject.hasProperty(OWL.sameAs)))
	                links.addInboundLink((RDFNode) subject, predicate);
            }
            
            if (inverse != null && !this._cachedModel.contains(object, inverse, (RDFNode) null)) {
	            if (!subject.hasProperty(RDF.type, OWL.Restriction) && (subject.hasProperty(RDF.type, Display.PreferredTerm) || !subject.hasProperty(OWL.sameAs)))
	                links.addInboundLink((RDFNode) subject, predicate);
            }
        }
        return links;
    }
    
    /**
     * This returns all the facet values associated with a particular facet in the results set.
     *
     * @param hits The results set.
     * @param facet The facet
     *
     * @return
     */
    protected FacetValues createFacetValues(Vector hits, LabelledResource facet) {
        FacetValues terms = new FacetValues();
        Iterator iter = hits.iterator();
        
        while (iter.hasNext()) {
            Resource object = this._cachedModel.getResource((String) iter.next());
            Property p = this._cachedModel.getProperty(facet.getResource());
            
            if (object.hasProperty(p)) {
                StmtIterator fields = object.listProperties(p);
                
                while (fields.hasNext()) {
                    Resource obj = ((Resource) fields.nextStatement().getObject());
                    if(!obj.hasProperty(RDF.type, (RDFNode) OWL.Restriction))
                        terms.add(pseudoUri(obj));
                }
            }
        }
        
        return terms;
    }	
    
    /**
     * This returns all the results that match the current restrictions
     *
     * @param restrictions The current restrictions
     * @return
     * @throws IOException
     */
    public Vector createResultsSet(Vector restrictions)
        throws IOException {
        Iterator i = restrictions.iterator();
        Vector results = new Vector(this._objects);

        while (i.hasNext()) {
            FacetRestriction restriction = (FacetRestriction) i.next();
            Resource o = this._cachedModel.getResource(restriction.getValueURI());
            Iterator objectIter = results.iterator();
            Property p = (!restriction.getFacetURI().equals("any") && !restriction.getFacetURI().equals("focus"))
                ? this._cachedModel.getProperty(restriction.getFacetURI()) : null;

            while (objectIter.hasNext()) {
                Resource s = this._cachedModel.getResource((String) objectIter.next());

                // if a member of the results set does not have a property with the correct value
                // remove it from the results set
                if (restriction.getFacetURI().equals("any")) {
                    StmtIterator statements = this._cachedModel.listStatements(s, (Property) null,
                            (RDFNode) o);

                    if (!statements.hasNext()) {
 					    StmtIterator reverseStatements = this._cachedModel.listStatements(o, (Property) null, (RDFNode) s);
 					    if (!reverseStatements.hasNext()) {
 					        objectIter.remove();
 					    }
                    }
                } else if (restriction.getFacetURI().equals("focus")) {
                    if(!pseudoUri(o).equals(pseudoUri(s))) objectIter.remove();
                } else if (restriction.getFacetURI().equals("freetextsearch")) {
                    // do nothing
                } else if (!(s.hasProperty(p, o))) {
                    objectIter.remove();
                }
            }
        }

        return results;
    }
}
