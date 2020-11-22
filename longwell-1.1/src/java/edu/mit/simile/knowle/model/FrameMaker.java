package edu.mit.simile.knowle.model;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;

/**
 * A class for constructing a frame based on a model and the resource
 * intended to be the center node, or other parameters identifying the
 * center node.
 *
 * @author Ryan Lee
 */
public class FrameMaker {
	///// Constants
	/**
	 * The maximum anonymous nesting to pursue.
	 */
	private static int MAX_DEPTH = 3;

	/**
	 * Creates a Frame based on a model and a resource.
	 *
	 * @param m    The origin model.
	 * @param res  The resource to be the center node.
	 *
	 * @return     A Frame with the resource as the center node.
	 * 
	 * @throws NonExistentResourceException  when the resource requested isn't in the model
	 */
	public static Frame make(Model m, OntDocumentManager odm, String res)
		throws NonExistentResourceException {
		Resource r = extractResource(m, res);
		Frame answer = null;
		if (m.containsResource((RDFNode) r)) {
			answer = start(m, odm, r, 0);
			prop(answer, m, r);
			end(answer, m, r);
		} else {
			throw new NonExistentResourceException(res);
		}
		return answer;
	}

	/**
	 * Creates a Frame based on a model and a unique property-object
	 * combination.
	 *
	 * @param m    The origin model.
	 * @param prop The String URI of a property whose object can
	 *             uniquely identify a resource subject.
	 * @param obj  The String URI of a unique node, Resource or Literal.
	 *
	 * @return     The Frame centered on the identified node.
	 * 
	 * @throws NonExistentResourceException  when the resource requested isn't in the model
	 */
	public static Frame make(Model m, OntDocumentManager odm, String prop, String obj)
			throws NonExistentResourceException {
		Property p = ResourceFactory.createProperty(prop);
		Frame answer = null;
		RDFNode o = null;
		// obj may be a literal or a resource
		if (obj.startsWith("\"")) {
			String objVal = obj.length() > 1 ?
							obj.substring(1, obj.length() - 1) :
							"";
			StmtIterator si = m.listStatements((Resource) null, p, (RDFNode) null);
			while(si.hasNext()) {
				Statement s = si.nextStatement();
				if(objVal.equals(s.getString())) {
					o = s.getObject();
					break;
				}
			}
		} else {
			o = ResourceFactory.createResource(obj);
		}
		
		if (o != null && m.containsResource((RDFNode) p) && m.containsResource(o)) {
			answer = start(m, odm, p, o, 0);
			prop(answer, m, p, o);
			end(answer, m, p, o);
		} else {
			throw new NonExistentResourceException("anonymous with " + prop + " and " + obj);
		}

		return answer;
	}

	/**
	 * Creates a random Frame from a model.  The performance is not
	 * good for this option since randomizing the selection reqiures
	 * knowing how large the collection of statements is, thus two
	 * passes over model.listSubjects().
	 *
	 * @param m  The origin model.
	 *
	 * @return   A randomly centered Frame.
	 */
	public static Frame makeRandom(Model m, OntDocumentManager odm) {
		Frame answer = null;
		if (!m.isEmpty()) {
			ResIterator resources = m.listSubjects();
			long subjSize = 0;
			while (resources.hasNext()) {
				resources.next();
				subjSize++;
			}
			resources = m.listSubjects();
			Resource base = null;
			Resource last = null;
			long randIndex = Math.round(Math.random() * (double) subjSize);
			if (resources.hasNext()) {
				long idx = 1;
				while (resources.hasNext()) {
					last = resources.nextResource();
					if (idx == randIndex)
						base = last;
					idx++;
				}
				if (null == base)
					base = last;
				answer = start(m, odm, base, 0);
				prop(answer, m, base);
				end(answer, m, base);
			}
		}
		return answer;
	}

	/**
	 * Begins working through a model, starting at the center node.
	 *
	 * @param m      The origin model.
	 * @param r      The resource to begin at.
	 * @param depth  The current depth of subframing.
	 *
	 * @return  A Frame with the given resource at the center.
	 */
	protected static Frame start(Model m, OntDocumentManager odm, Resource r, int depth) {
		Frame f = new Frame(m, odm);

		if (depth == MAX_DEPTH) {
			f.setCenter("http://www.example.org/#deadend");
			f.addTitle("max anonymous depth exceeded");
			return f;
		}

		StmtIterator si = m.listStatements(r, (Property) null, (RDFNode) null);
		while (si.hasNext()) {
			Statement s = si.nextStatement();
			if (!s.getObject().asNode().isLiteral()) {
				if (s.getResource().isAnon()) {
					f.addFrame(
						s.getPredicate(),
						start(m, odm, s.getResource(), depth + 1));
				} else {
					f.addResource(s.getPredicate(), s.getResource());
				}
			} else {
				f.addLiteral(s.getPredicate(), s.getLiteral().getString());
			}
		}
		return f;
	}

	/**
	 * Begins working to build a frame based on a unique property/resource.
	 * (Arbitrariliy chooses the first one in the case that it isn't
	 *  unique; perhaps something else can be done to warn that the
	 *  relationship is not unique.)
	 *
	 * @param m      The origin model.
	 * @param p      The property portion of a unique property/node combo.
	 * @param o      The object portion of a unique property/node combo.
	 * @param depth  The current depth of subframing.
	 *
	 * @return  The Frame with the identified resource at the center.
	 */
	protected static Frame start(Model m,
								 OntDocumentManager odm,
								 Property p,
								 RDFNode o,
								 int depth) {
		Frame f = new Frame(m, odm);
		StmtIterator si = m.listStatements((Resource) null, p, o);
		if (si.hasNext()) {
			Statement s = si.nextStatement();
			f = start(m, odm, s.getSubject(), depth);
		}
		return f;
	}

	/**
	 * Adds to a Frame by finding statements with the center node as
	 * a property.
	 *
	 * @param f
	 * @param m
	 * @param p
	 *
	 * @return
	 */
	protected static void prop(Frame f, Model m, Resource p) {
		try {
			Property derived = ResourceFactory.createProperty(p.getURI());
			StmtIterator si =
				m.listStatements((Resource) null, derived, (RDFNode) null);
			while (si.hasNext()) {
				Statement s = si.nextStatement();
				if (s.getSubject().isAnon()
					&& !s.getObject().asNode().isLiteral()
					&& s.getObject().asNode().isBlank()) {
					f.addAsProp((Resource) null, (Resource) null);
				} else if (
					s.getSubject().isAnon()
						&& !s.getObject().asNode().isLiteral()
						&& !s.getObject().asNode().isBlank()) {
					f.addAsProp(null, s.getResource());
				} else if (
					!s.getSubject().isAnon()
						&& !s.getObject().asNode().isLiteral()
						&& !s.getObject().asNode().isBlank()) {
					f.addAsProp(s.getSubject(), s.getResource());
				} else if (
					!s.getSubject().isAnon()
						&& !s.getObject().asNode().isLiteral()
						&& s.getObject().asNode().isBlank()) {
					f.addAsProp(s.getSubject(), (Resource) null);
				} else if (
					s.getSubject().isAnon()
						&& s.getObject().asNode().isLiteral()) {
					f.addAsProp(null, s.getLiteral().getString());
				} else {
					f.addAsProp(s.getSubject(), s.getLiteral().getString());
				}
			}
		} catch (InvalidPropertyURIException e) {
			;
		}
	}

	/**
	 * Adds to a Frame by finding statements with the center node as
	 * a property.
	 *
	 * @param f
	 * @param m
	 * @param p
	 * @param o
	 *
	 * @return
	 */
	protected static void prop(Frame f, Model m, Property p, RDFNode o) {
		StmtIterator si = m.listStatements((Resource) null, p, o);
		if (si.hasNext()) {
			Statement s = si.nextStatement();
			prop(f, m, s.getSubject());
		}
	}

	/**
	 * Concludes the creation of a Frame by looking for inbound links.
	 *
	 * @param f  The Frame to continue building.
	 * @param m  The origin model.
	 * @param r  The resource that is the object of inbound statements.
	 */
	protected static void end(Frame f, Model m, Resource r) {
		f.setCenter(r.getURI());
		StmtIterator si =
			m.listStatements((Resource) null, (Property) null, (RDFNode) r);
		while (si.hasNext()) {
			Statement s = si.nextStatement();
			f.addInbound(s.getSubject(), s.getPredicate());
		}
	}

	/**
	 * Begins working to build a frame based on a unique property/resource.
	 * (Arbitrariliy chooses the first one in the case that it isn't
	 *  unique; perhaps something else can be done to warn that the
	 *  relationship is not unique.)
	 *
	 * @param f  The Frame to add to.
	 * @param m  The origin model.
	 * @param p  The property portion of a unique property/node combo.
	 * @param o  The object portion of a unique property/node combo.
	 */
	protected static void end(Frame f, Model m, Property p, RDFNode o) {
		StmtIterator si = m.listStatements((Resource) null, p, o);
		if (si.hasNext()) {
			Statement s = si.nextStatement();
			end(f, m, s.getSubject());
		}
	}
	
	protected static Resource extractResource(Model model, String uri) {
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
}
