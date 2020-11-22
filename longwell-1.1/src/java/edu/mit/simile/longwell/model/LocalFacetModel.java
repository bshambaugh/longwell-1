/*
 * Created on Oct 20, 2004
 */
package edu.mit.simile.longwell.model;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.values.FacetRestriction;
import edu.mit.simile.longwell.values.LabelledResource;
import edu.mit.simile.vocabularies.Display;


/**
 * A facet model based on local, non-network accessed stores.
 * 
 * @author ryanlee
 */
public abstract class LocalFacetModel extends FacetModel {
	protected Vector objects;

    public LocalFacetModel(Model model, String indexPath) throws Exception {
        super(model, indexPath);
    }
	
	public void configureBrowser(BrowserConfig bc) throws Exception {
	    objects = new Vector();
		super.configureBrowser(bc);
		
		// for each configured object type
		Iterator i = bc.getConfiguredClasses().iterator();

		while (i.hasNext()) {
			Resource t = model.getResource((String) i.next());
			
			// for each data object in the model of that type
			ResIterator objectIter = model.listSubjectsWithProperty(RDF.type, t);
			
			while (objectIter.hasNext()) {
				Resource r = objectIter.nextResource();
				String uri = pseudoUri(r);
				if (!r.hasProperty(OWL.sameAs) ||
					r.hasProperty(RDF.type, Display.PreferredTerm)) {
					objects.add(uri);
				}
			}
		}
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
		Resource object = extractResource(model, objectURI);
		
		Property p = model.getProperty(browserField.getResource());
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
			            } else if (this.defaultLang.equals("all")) {
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
	 * This returns all the inbound links as <code>InboundLinks</code>.
	 *
	 * @param objectURI  The member of the results set.
	 * 
	 * @return
	 */
	protected InboundLinks getInboundLinks(String objectURI) {
	    InboundLinks links = new InboundLinks();
	    Resource object = extractResource(model, objectURI);
	    StmtIterator si = model.listStatements((Resource) null, (Property) null, (RDFNode) object);
	    while (si.hasNext()) {
	        Statement s = si.nextStatement();
	        Resource subject = s.getSubject();
	        Property predicate = (Property) s.getPredicate();
	        Property inverse = null;
	        if (model.contains(predicate, OWL.inverseOf, (RDFNode) null)) {
	            StmtIterator i = model.listStatements((Resource) predicate, OWL.inverseOf, (RDFNode) null);
	            // precludes multiple inverses
	            Resource inverseRes = (Resource) i.nextStatement().getObject();
	            inverse = model.getProperty(inverseRes.getURI()); 
	        } else if (model.contains((Resource) null, OWL.inverseOf, (RDFNode) predicate)) {
	            StmtIterator i = model.listStatements((Resource) null, OWL.inverseOf, (RDFNode) predicate);
	            // precludes multiple inverses
	            Resource inverseRes = i.nextStatement().getSubject();	
	            inverse = model.getProperty(inverseRes.getURI());
	        } else {
	            if (!subject.hasProperty(RDF.type, OWL.Restriction) && (subject.hasProperty(RDF.type, Display.PreferredTerm) || !subject.hasProperty(OWL.sameAs)))
	                links.addInboundLink((RDFNode) subject, predicate);
	        }
	        
	        if (inverse != null && !model.contains(object, inverse, (RDFNode) null)) {
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
			Resource object = extractResource(model, (String) iter.next());
			Property p = model.getProperty(facet.getResource());
			
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

    /* (non-Javadoc)
     * @see edu.mit.simile.longwell.model.FacetModel#createResultsSet(java.util.Vector)
     */
	public Vector createResultsSet(Vector restrictions)
	throws IOException {
		Vector results = null;
		
		if (restrictions.size() == 0) {
			return objects;
		}
		
		Iterator i = restrictions.iterator();
		FacetRestriction restriction = (FacetRestriction) i.next();

		if (restriction.getFacetURI().equals("freetextsearch")) {
			results = new Vector();
			
			try {
				if (searcher == null) {
					return null;
				} else {
					String queryString = restriction.getValueURI();
					Query query = QueryParser.parse("+" + queryString, "search",
							new SimpleAnalyzer());
					System.out.println(query.toString());
					
					Hits hits = searcher.search(query);
					
					for (int j = 0; j < hits.length(); j++) {
						String uri = hits.doc(j).getField("uri").stringValue();
						results.add(uri);
					}
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
		} else {
			results = new Vector(objects);
		}
		
		Iterator j = restrictions.iterator();
		
		while (j.hasNext()) {
			restriction = (FacetRestriction) j.next();
			
			Resource o = extractResource(model, restriction.getValueURI());
			Iterator objectIter = results.iterator();
			Property p = (!restriction.getFacetURI().equals("any") && !restriction.getFacetURI().equals("focus"))
			? model.getProperty(restriction.getFacetURI()) : null;
			
			while (objectIter.hasNext()) {
				String objUri = (String) objectIter.next();
				Resource s = extractResource(model, objUri);
				
				// if a member of the results set does not have a property with the correct value
				// remove it from the results set
				if (restriction.getFacetURI().equals("any")) {
					StmtIterator statements = model.listStatements(s, (Property) null, (RDFNode) o);
					
					if (!statements.hasNext()) {
					    StmtIterator reverseStatements = model.listStatements(o, (Property) null, (RDFNode) s);
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
