package edu.mit.simile.inferencing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.vocabularies.Artstor;
import edu.mit.simile.vocabularies.Display;
import edu.mit.simile.vocabularies.SkosCore;
import edu.mit.simile.vocabularies.VRACore;

/**
 * A simple inference engine that generates the inferences we need to map between the collections.
 *
 * @author Mark H. Butler
 */
public class SimileReasoner implements Reasoner {

    Logger logger = Logger.getLogger(SimileReasoner.class);
    
	public Model process(Model model) {
		return processMappings(model);
	}

	/**
	 * Infers additional triples from owl:equivalentProperty and owl:sameAs relations
	 *
	 * @return
	 */
	public Model processMappings(Model model) {
		// Process owl:equivalentProperty relationships
		StmtIterator equivalentPropertyStatements = model.listStatements((Resource) null, OWL.equivalentProperty, (RDFNode) null);
		
		while (equivalentPropertyStatements.hasNext()) {
			Statement statement = equivalentPropertyStatements.nextStatement();
			Property p1 = model.createProperty(statement.getSubject().getURI());
			Property p2 = model.createProperty(((Resource) statement.getObject()).getURI());
			model = addEquivalentProperty(model, p1, p2);
			model = addEquivalentProperty(model, p2, p1);
		}
		equivalentPropertyStatements.close();

		// call this twice otherwise we are missing some statements
		// this is where having a proper reasoner would solve lots of problems
		
		model = processSameAs(model);
		model = processSameAs(model);

		// processInverseFunctionalSameAs(model);

		// call subclass code again 
		return processSubClasses(model);
	}

	/**
	 * Creates inferred subclass relationships.
	 *
	 * @return
	 */
	public Model processSubClasses(Model model) {
		// Process rdfs:subClassOf relationships
		// this is a standard RDFS inference
		StmtIterator subclassStatements = model.listStatements((Resource) null, RDFS.subClassOf, (RDFNode) null);
		
		while (subclassStatements.hasNext()) {
			Statement statement = subclassStatements.nextStatement();
			Resource subclass = statement.getSubject();
			RDFNode superclass = statement.getObject();
			
			model = addSuperClass(model, RDF.type, subclass, superclass);
		}
		
		// Process skos:broader relationships
		// this is a custom rule
		StmtIterator broaderStatements = model.listStatements((Resource) null, SkosCore.broader, (RDFNode) null);
		
		while (broaderStatements.hasNext()) {
			Statement statement = broaderStatements.nextStatement();
			Resource narrower = statement.getSubject();
			RDFNode broader = statement.getObject();
			
			model = addSuperClass(model, VRACore.subject, narrower, broader);
			model = addSuperClass(model, Artstor.topic, narrower, broader);
			model = addSuperClass(model, Artstor.geographic, narrower, broader);
		}
		
		// Process rdfs:subPropertyOf relationships
		// this is a standard RDFS inference
		StmtIterator subpropertyStatements = model.listStatements((Resource)null, RDFS.subPropertyOf, (RDFNode) null);
		
		while (subpropertyStatements.hasNext()) {
			Statement statement = subpropertyStatements.nextStatement();
			Property p1 = model.createProperty(statement.getSubject().getURI());
			Property p2 = model.createProperty(((Resource) statement.getObject()).getURI());
			model = addEquivalentProperty(model, p2, p1);          
		}
		
		return model;
	}
	
	private Model processSameAs(Model model) {
  		// Process owl:sameAs relationships
		// don't add all entailments, only the ones we need
		
		// as we need to add more sameAs relations, but we have to be careful
		// otherwise we will get a concurrent modification exception due to 
		// the way Jena implements iterators
    
		StmtIterator sameAsStatements = model.listStatements((Resource) null, OWL.sameAs, (RDFNode) null);
		Vector object = new Vector();
		Vector subject = new Vector();
    
		while (sameAsStatements.hasNext()) {
			Statement statement = sameAsStatements.nextStatement();
			object.add((RDFNode) statement.getSubject());
			subject.add((RDFNode) statement.getObject());
		}
		sameAsStatements.close();
    
		Iterator i = object.iterator();
		Iterator j = subject.iterator();
		
		while (i.hasNext() && j.hasNext()) {
			RDFNode r1 = (RDFNode) i.next();
			RDFNode r2 = (RDFNode) j.next();
			model = addSameAs(model, r1, r2);
			model = addSameAs(model, r2, r1);
		}
		
		return model;
	}

	/**
	 * 1. find an inverseFunctionalProperty
	 * 2. find any subjects that use that ifp
	 * 3. declare the first a primary for all objects of that property
	 * 4. find any other subjects that share those objects, declare sameAs
	 * 5. find any other objects of those subjects, declare the original the primary
	 *    for those as well
	 * 6. remove any of the corroborative subjects from the original list; iterate
	 */
	private Model processInverseFunctionalSameAs(Model model) {
		HashSet added = new HashSet();
		Vector pairs = new Vector();
		StmtIterator si = model.listStatements((Resource) null, RDF.type, OWL.InverseFunctionalProperty);
		while (si.hasNext()) {
			Resource i = (Resource) si.nextStatement().getSubject();
			Property ifp = model.getProperty(i.getURI());
			StmtIterator ssi = model.listStatements((Resource) null, ifp, (RDFNode) null);
			while (ssi.hasNext()) {
				Statement s = ssi.nextStatement();
				//System.err.println(s);
				Resource subj = s.getSubject();
				RDFNode obj = s.getObject();
				StmtIterator sames = model.listStatements((Resource) null, ifp, obj);
				while (sames.hasNext()) {
					Statement same = sames.nextStatement();
					//System.err.println("|>" + same);
					Resource sameSubj = same.getSubject();
					if (!subj.equals(sameSubj)) {
						added.add(subj);
						added.add(sameSubj);
						Vector pair = new Vector();
						pair.add(subj);
						pair.add(sameSubj);
						pairs.add(pair);
						//System.err.println("||>" + subj + "|" + sameSubj);
					}
				}
				sames.close();
			}
			ssi.close();
		}
		si.close();
		
		Iterator vi = pairs.iterator();
		while (vi.hasNext()) {
			Vector pair = (Vector) vi.next();
			Resource subj = (Resource) pair.get(0);
			Resource sameSubj = (Resource) pair.get(1);
			model.add(subj, OWL.sameAs, (RDFNode) sameSubj);
			model.add(sameSubj, OWL.sameAs, (RDFNode) subj);
			model = addSameAs(model, (RDFNode) subj, (RDFNode) sameSubj);
			model = addSameAs(model, (RDFNode) sameSubj, (RDFNode) subj);
			model = addSameAsProperty(model, (RDFNode) subj, (RDFNode) sameSubj);
		}

		HashSet addedClone = (HashSet) added.clone();
		Iterator ai = added.iterator();
		while (ai.hasNext()) {
			Resource preferred = (Resource) ai.next();
			if (addedClone.contains(preferred)) {
				model.add(preferred, RDF.type, Display.PreferredTerm);
				StmtIterator prefsi = model.listStatements(preferred, OWL.sameAs, (RDFNode) null);
				while (prefsi.hasNext()) {
					Resource drop = prefsi.nextStatement().getResource();
					addedClone.remove(drop);
				}
				prefsi.close();
			}
		}
		
		return model;
	}
	
	// ----------------------------------------------------------------------
	
	private Model addEquivalentProperty(Model model, Property property1, Property property2) {
		logger.info(property1 + " -(equivalentTo)-> " + property2);
		StmtIterator statements = model.listStatements((Resource) null, property2, (RDFNode) null);
		
		while (statements.hasNext()) {
			Statement statement = statements.nextStatement();
			statement.getSubject().addProperty(property1, statement.getObject());
		}
		
		return model;
	}
	
	private Model addSuperClass(Model model, Property property, Resource subclass, RDFNode superclass) {
		logger.info(superclass + " -(superClassOf)-> " + subclass);
		StmtIterator statements = model.listStatements((Resource) null, property, subclass);
		
		while (statements.hasNext()) {
			statements.nextStatement().getSubject().addProperty(property, superclass);
		}
		
		return model;
	}
	
	private Model addSameAs(Model model, RDFNode instance1, RDFNode instance2) {
		logger.info(instance1 + " -(sameAs)-> " + instance2);
		Resource resource1 = (Resource) instance1;
		String instance1URI = resource1.getURI();
		Resource resource2 = (Resource) instance2;
		StmtIterator statements = model.listStatements((Resource) null, (Property) null, instance2);
		
		while (statements.hasNext()) {
			Statement statement = statements.nextStatement();
			Property property = statement.getPredicate();      
			String uri = statement.getSubject().getURI();
			if ((uri != null) && (!uri.equals(instance1URI))) {
				statement.getSubject().addProperty(property, instance1);
			}
		}
				
		StmtIterator statements2 = model.listStatements(resource2, (Property) null, (RDFNode) null);
		
		while (statements2.hasNext()) {
			Statement statement = statements2.nextStatement();
			Property property = statement.getPredicate();
			RDFNode object = statement.getObject();
			String objUri = object.canAs(Resource.class) ? ((Resource) object.as(Resource.class)).getURI() : "";
					
			// we don't statements where subject and object are the same
			// and don't propagate disp:PreferredTerm
			if ((objUri != null) && !objUri.equals(instance1URI) && 
				  !(property.getURI().equals(RDF.type.getURI()) && 
				  objUri.equals(Display.PreferredTerm.getURI()))) {
					resource1.addProperty(property, object);
			}
		}
		
		return model;
	}
	
	private Model addSameAsProperty(Model model, RDFNode r1, RDFNode r2) {
		logger.info(r1 + " -(sameAs)-> " + r2);		
		StmtIterator st1 = model.listStatements ((Resource) r1, OWL.sameAs, (RDFNode) null);
		while (st1.hasNext()) {
			RDFNode obj = st1.nextStatement().getObject();
			if (!obj.equals(r2)) {
				model.enterCriticalSection(Model.WRITE);
				try {
					model.add((Resource) r2, OWL.sameAs, obj);
				} finally {
					model.leaveCriticalSection();
				}
			}
		}
		st1.close();

		StmtIterator st2 = model.listStatements ((Resource) r2, OWL.sameAs, (RDFNode) null);
		while (st2.hasNext()) {
			RDFNode obj = st2.nextStatement().getObject();
			if (!obj.equals(r1)) {
				model.enterCriticalSection(Model.WRITE);
				try {
					model.add((Resource) r1, OWL.sameAs, obj);
				} finally {
					model.leaveCriticalSection();
				}
			}
		}
		st2.close();
		
		return model;
	}
	
}
