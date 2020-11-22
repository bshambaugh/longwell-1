package edu.mit.simile.knowle.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A containment class providing a view of RDF centered around one
 * node, containing its properties and inbound links and other human
 * friendly attributes, particularly title(s) and type(s).  The frame
 * 'understands' the rdfs:label, rdf:type, and dc:title properties.
 *
 * @author Ryan Lee
 */
public class Frame {
	///// Member variables
	private HashMap inbound;
	private HashMap asprop;
	private HashMap literals;
	private HashMap resources;
	private HashMap frames;
	private Model src;
	private OntDocumentManager labelSource;
	private Vector titles;
	private Vector types;
	private String center;

	///// Demo stopgap - @@@remove!
	private Property VRAtitle =
		ResourceFactory.createProperty(
			"http://simile.mit.edu/2003/10/ontologies/vraCore3#title");
	
	///// Creation
	/**
	 * Instantiate a new frame object.
	 *
	 * @param m  The model this frame is coming from.
	 */
	public Frame(Model m, OntDocumentManager odm) {
		inbound = new HashMap();
		asprop = new HashMap();
		literals = new HashMap();
		resources = new HashMap();
		frames = new HashMap();
		titles = new Vector();
		types = new Vector();
		src = m;
		labelSource = odm;	
	}

	///// Accessors
	/**
	 * Returns the focus of the frame.
	 *
	 * @return  The resource in the middle, a URI String.
	 */
	public String getCenter() {
		return this.center;
	}

	/**
	 * Returns the set of statements with the center as the object.
	 *
	 * @return  A HashMap of inbound statements.
	 */
	public HashMap getInbound() {
		return inbound;
	}

	/**
	 * Returns the set of statements with the center as the predicate.
	 *
	 * @return  A HashMap of as-property statements.
	 */
	public HashMap getAsProp() {
		return asprop;
	}

	/**
	 * Returns the set of statements relating the center to other
	 * non-anonymous resources.
	 *
	 * @return  A HashMap of resource-object statements.
	 */
	public HashMap getResources() {
		return resources;
	}

	/**
	 * Returns the statements relating the center node to literals.
	 *
	 * @return  A HashMap of literal-object statements.
	 */
	public HashMap getLiterals() {
		return literals;
	}

	/**
	 * Returns the statements relating the center node to anonymous
	 * resources (i.e., other subframes in this context).
	 *
	 * @return  A HashMap of anonymous-node-object statements.
	 */
	public HashMap getFrames() {
		return frames;
	}

	/**
	 * Returns titles of the center node.
	 *
	 * @return  A Vector of titles for the center node.
	 */
	public Vector getTitles() {
		return titles;
	}

	/**
	 * Returns types of the center node.
	 *
	 * @return  A Vector of rdf:type objects.
	 */
	public Vector getTypes() {
		return types;
	}

	/**
	 * Sets the center node resource.
	 * 
	 * @param uri  The center node resource.
	 */
	public void setCenter(String uri) {
		this.center = uri;
	}

	/**
	 * Adds a title to the center node.
	 *
	 * @param title A String title.
	 */
	public void addTitle(String title) {
		titles.add(title);
	}

	/**
	 * Adds a type to the center node; public, but normally an offshoot
	 * from addProperty.
	 *
	 * @param type A LabelledResource type.
	 *
	 * @see LabelledResource
	 */
	public void addType(LabelledResource type) {
		types.add(type);
	}

	/**
	 * Adds an inbound statement from a resource related by a property.
	 *
	 * @param r The subject of the inbound statement.
	 * @param p The property of the inbound statement.
	 */
	public void addInbound(Resource r, Property p) {
		LabelledResource rLR = null;
		if (!r.isAnon())
			rLR =
				new LabelledResource(
					r.getURI(),
					retrieveLabel(r, false),
					"&lt;" + r.getURI() + "&gt;");
		else
			rLR = new LabelledResource(pseudoUri(r), retrieveLabel(r, false), "&lt;" + pseudoUri(r) + "&gt;");
		Vector v =
			inbound.containsKey(rLR) ? (Vector) inbound.get(rLR) : new Vector();
		LabelledResource pLR = makePropertyLR(p);
		v.add(pLR);
		inbound.put(rLR, v);
	}

	/**
	 * Adds a statement with the center node as the property.
	 *
	 * @param s  The statement subject.
	 * @param o  The statement object.
	 */
	public void addAsProp(Resource s, Resource o) {
		LabelledResource sLR = null, oLR = null;
		if (null != s)
			sLR =
				new LabelledResource(
					s.toString(),
					retrieveLabel(s, false),
					"&lt;" + s.toString() + "&gt;");
		else
			sLR = new LabelledResource(null, "{{{anonymous}}}", "No URI");
		Vector v =
			asprop.containsKey(sLR) ? (Vector) asprop.get(sLR) : new Vector();
		if (null != o)
			oLR =
				new LabelledResource(
					o.toString(),
					retrieveLabel(o, false),
					"&lt;" + o.toString() + "&gt;");
		else
			oLR = new LabelledResource(null, "{{{anonymous}}}", "No URI");
		v.add(oLR);
		asprop.put(sLR, v);
	}

	/**
	 * Adds a statement with the center node as the property.
	 *
	 * @param s  The statement subject.
	 * @param o  The statement object, a String.
	 */
	public void addAsProp(Resource s, String o) {
		LabelledResource sLR = null;
		if (null != s)
			sLR =
				new LabelledResource(
					s.toString(),
					retrieveLabel(s, false),
					"&lt;" + s.toString() + "&gt;");
		else
			sLR = new LabelledResource(null, "{{{anonymous}}}", "No URI");
		Vector v =
			asprop.containsKey(sLR) ? (Vector) asprop.get(sLR) : new Vector();
		LabelledResource oLR = new LabelledResource(null, o, null);
		v.add(oLR);
		asprop.put(sLR, v);
	}

	/**
	 * Adds a statement with a literal as the object.
	 *
	 * @param p  The statement property.
	 * @param l  The statement object, a String.
	 */
	public void addLiteral(Property p, String l) {
		if (p.equals(RDFS.label) || p.equals(DC.title) || p.equals(VRAtitle)) {
			addTitle(l);
		}

		LabelledResource pLR = makePropertyLR(p);

		Vector v =
			literals.containsKey(pLR)
				? (Vector) literals.get(pLR)
				: new Vector();
		LabelledResource lLR = new LabelledResource(l);
		v.add(lLR);
		literals.put(pLR, v);
	}

	/**
	 * Adds a statement relating a resource to the center node through
	 * a property.
	 *
	 * @param p  The property relating subject to object.
	 * @param r  The resource object.
	 */
	public void addResource(Property p, Resource r) {
		if (p.equals(RDF.type)) {
			LabelledResource type =
				new LabelledResource(
					r.toString(),
					retrieveLabel(r, false),
					"&lt;" + r.toString() + "&gt;");
			addType(type);
		}

		LabelledResource pLR = makePropertyLR(p);
		
		Vector v =
			resources.containsKey(pLR)
				? (Vector) resources.get(pLR)
				: new Vector();
		LabelledResource rLR =
			new LabelledResource(
				r.toString(),
				retrieveLabel(r, false),
				"&lt;" + r.toString() + "&gt;");
		v.add(rLR);
		resources.put(pLR, v);
	}

	/**
	 * Adds a subframe to this frame as related through a property.
	 *
	 * @param p  The property relating the center node to a subframe.
	 * @param f  The subframe, a Frame object.
	 */
	public void addFrame(Property p, Frame f) {
		LabelledResource pLR = makePropertyLR(p);

		Vector v =
			(frames.containsKey(pLR)) ? (Vector) frames.get(pLR) : new Vector();
		v.add(f);
		frames.put(pLR, v);
	}

	/**
	 * Converts the frame to a string.
	 *
	 * @return  A String representation of the frame.
	 */
	public String toString() {
		String answer = "";

		if (!literals.isEmpty()) {
			Iterator literalIterator = literals.keySet().iterator();
			while (literalIterator.hasNext()) {
				LabelledResource p = (LabelledResource) literalIterator.next();
				Vector values = (Vector) literals.get(p);
				Iterator vIterator = values.iterator();
				answer += p.getLabel() + ": ";
				while (vIterator.hasNext()) {
					answer += ((LabelledResource) vIterator.next()).getLabel()
						+ (vIterator.hasNext() ? ", " : "");
				}
				answer += "\n";
			}
		}

		if (!resources.isEmpty()) {
			Iterator resourceIterator = resources.keySet().iterator();
			while (resourceIterator.hasNext()) {
				LabelledResource p = (LabelledResource) resourceIterator.next();
				Vector values = (Vector) resources.get(p);
				Iterator vIterator = values.iterator();
				answer += p.getLabel() + ": ";
				while (vIterator.hasNext()) {
					answer += ((LabelledResource) vIterator.next()).getLabel()
						+ (vIterator.hasNext() ? ", " : "");
				}
				answer += "\n";
			}
		}

		if (!frames.isEmpty()) {
			Iterator frameIterator = frames.keySet().iterator();
			while (frameIterator.hasNext()) {
				LabelledResource p = (LabelledResource) frameIterator.next();
				Vector values = (Vector) frames.get(p);
				answer += " " + p.getLabel() + ": " + values.toString() + "\n";
			}
		}

		if (!inbound.isEmpty()) {
			Iterator inboundIterator = inbound.keySet().iterator();
			while (inboundIterator.hasNext()) {
				LabelledResource r = (LabelledResource) inboundIterator.next();
				Vector values = (Vector) inbound.get(r);
				Iterator vIterator = values.iterator();
				answer += r.getLabel() + " ";
				while (vIterator.hasNext()) {
					answer += ((LabelledResource) vIterator.next()).getLabel()
						+ (vIterator.hasNext() ? ", " : "");
				}
				answer += " this\n";
			}
		}

		if (!asprop.isEmpty()) {
			Iterator aspropIterator = asprop.keySet().iterator();
			while (aspropIterator.hasNext()) {
				LabelledResource r = (LabelledResource) aspropIterator.next();
				Vector values = (Vector) asprop.get(r);
				Iterator vIterator = values.iterator();
				answer += r.getLabel() + " this ";
				while (vIterator.hasNext()) {
					answer += ((LabelledResource) vIterator.next()).getLabel()
						+ (vIterator.hasNext() ? ", " : "");
				}
				answer += "\n";
			}
		}

		return answer;
	}

	/**
	 * In the special case that the property is a containment relationship,
	 * do something different, otherwise, create a normal labelled resource.
	 *
	 * @param p  The base property to create from.
	 * @return   The <code>LabelledResource</code> representation.
	 */
	private LabelledResource makePropertyLR(Property p) {
		LabelledResource pLR = null;
		if (p.getOrdinal() > 0) {
			pLR = new LabelledResource(p.toString(), "list item (" + p.getOrdinal() + ")");
		} else {
			pLR =
				new LabelledResource(
					p.toString(),
					retrieveLabel(p, true),
					"&lt;" + p.toString() + "&gt;");
		}
		return pLR;

	}

	/**
	 * Using the OntDocumentManager, if it exists, return a label for a given resource.
	 * 
	 * @param n  The resource to look up.
	 * 
	 * @return   The label for a resource.
	 */
	private String lookupLabel(Resource n) {
		String answer = null;
		String ns = n.getNameSpace();
		if(null != labelSource) {
			Model ont = labelSource.getModel(ns);
			if(null != ont) {
				StmtIterator si = ont.listStatements(n, RDFS.label, (RDFNode) null);
				if(si.hasNext()) {
					Statement s = si.nextStatement();
					answer = s.getLiteral().getString();
				} else {
					si = src.listStatements(n, DC.title, (RDFNode) null);
					if (si.hasNext()) {
						Statement s = si.nextStatement();
						answer = s.getLiteral().getString();
					}
				}
			}
		}
		return answer;
	}

	/**
	 * Retrieves the human-friendly label for a given resource.
	 *
	 * @return  
	 */
	private String retrieveLabel(Resource n, boolean shorten) {
		String answer = lookupLabel(n);
		if (null == answer) {
			StmtIterator si = src.listStatements(n, RDFS.label, (RDFNode) null);
			if (si.hasNext()) {
				Statement s = si.nextStatement();
				answer = s.getLiteral().getString();
			} else {
				si = src.listStatements(n, DC.title, (RDFNode) null);
				if (si.hasNext()) {
					Statement s = si.nextStatement();
					answer = s.getLiteral().getString();
				} else {
					si = src.listStatements(n, (Property) VRAtitle, (RDFNode) null);
					if (si.hasNext()) {
						Statement s = si.nextStatement();
						answer = s.getLiteral().getString();
					} else if (shorten) {
						answer = "{" + n.getLocalName() + "}";
					} else {
						answer = "&lt;untitled&gt;";
					}
				}
			}
		}
		return answer;
	}
	
	protected static String pseudoUri(Resource res) {
	    String uri = null;
	    if (res.isAnon()) {
	        uri = "http://simile.mit.edu/anonymous#" + res.getId();
	    } else {
	        uri = res.getURI();
	    }
	    return uri;
	}
}
