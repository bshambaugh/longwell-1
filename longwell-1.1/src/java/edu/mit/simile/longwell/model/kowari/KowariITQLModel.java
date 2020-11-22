package edu.mit.simile.longwell.model.kowari;

import java.util.Iterator;

import org.kowari.itql.ItqlInterpreterBean;
import org.kowari.query.Answer;
import org.kowari.query.rdf.LiteralImpl;
import org.kowari.query.rdf.URIReferenceImpl;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.vocabularies.Display;

/**
 * Created on Apr 12, 2004
 * See http://www.kowari.org/docs/developer/integration/index.php#bean
 * 
 * @author ryanlee
 * @version $Id$
 */
public class KowariITQLModel extends RemoteFacetModel {
	
	private String _modelName;

	/**
	 * @param model
	 * @param bc
	 * @param indexPath
	 */
	public KowariITQLModel(Model model, String indexPath, String modelName) throws Exception {
		super(model, indexPath);
		this._modelName = modelName;
	}
    
    protected void createCachedModel(BrowserConfig bc) throws Exception {		
    		this._cachedModel = ModelFactory.createDefaultModel();

    		ItqlInterpreterBean interpreter = new ItqlInterpreterBean();

		String labelQuery =
			"select $subject $label from "
				+ _modelName
				+ " where $subject <"
				+ RDFS.label.getURI()
				+ "> $label;";

		Answer answer = interpreter.executeQuery(labelQuery);

		answer.beforeFirst();

		while(answer.next()) {
			String subject = ((URIReferenceImpl) answer.getObject(0)).getURI().toString();
			String label = ((LiteralImpl) answer.getObject(1)).getLexicalForm();
			Resource r = this._cachedModel.createResource(subject);
			com.hp.hpl.jena.rdf.model.Literal l = this._cachedModel.createLiteral(label);
			this._cachedModel.add(r, RDFS.label, l);
		}
		
		answer.close();

		String subClassQuery =
			"select $subject $superclass from "
				+ _modelName
				+ " where $subject <"
				+ RDFS.subClassOf.getURI()
				+ "> $superclass;";

		answer = interpreter.executeQuery(subClassQuery);
		
		answer.beforeFirst();

		while (answer.next()) {
			String subject = ((URIReferenceImpl) answer.getObject(0)).getURI().toString();
			String superclass = ((URIReferenceImpl) answer.getObject(1)).getURI().toString();
			Resource r = this._cachedModel.createResource(subject);
			Resource o = this._cachedModel.createResource(superclass);
			this._cachedModel.add(r, RDFS.subClassOf, o);
		}
		
		answer.close();

	    // get all objects of registered types
        // for each configured object type
        Iterator j = bc.getConfiguredClasses().iterator();

        while (j.hasNext()) {
            String queryString = "select $a $b $c from "
            		+ this._modelName
				+ " where $a <"
				+ RDF.type.getURI()
				+ "> <"
				+ ((String) j.next())
				+ "> and $a $b $c;";

            answer = interpreter.executeQuery(queryString);

            answer.beforeFirst();
            
		    while (answer.next()) {
				String subject = ((URIReferenceImpl) answer.getObject(0)).getURI().toString();
				String predicate = ((URIReferenceImpl) answer.getObject(1)).getURI().toString();
				Object object = answer.getObject(2);
				Resource sub = this._cachedModel.createResource(subject);
				Property pred = this._cachedModel.createProperty(predicate);
				if (object instanceof URIReferenceImpl) {
					Resource obj = this._cachedModel.createResource(((URIReferenceImpl) object).getURI().toString());
					this._cachedModel.add(sub, pred, obj);
				} else if (object instanceof LiteralImpl) {
					Literal obj = this._cachedModel.createLiteral(((LiteralImpl) object).getLexicalForm());
					this._cachedModel.add(sub, pred, obj);						
				}
			}
			
			answer.close();
        }

        // for each configured object type
        Iterator i = bc.getConfiguredClasses().iterator();

        while (i.hasNext()) {
            Resource t = this._cachedModel.getResource((String) i.next());

            // for each data object in the model of that type
            ResIterator objectIter = this._cachedModel.listSubjectsWithProperty(RDF.type, t);

            while (objectIter.hasNext()) {
                Resource r = objectIter.nextResource();
                this._objects.add(r.getURI());
            }
        }		
	}
    
	public void createIndex() {
		// fixme
	}
	
	/**
	 * This creates a list of equivalent terms so we can hide them in the UI.
	 */
	public void createPreferredTerms() {
		ItqlInterpreterBean interpreter = new ItqlInterpreterBean();
		try {
			String queryString = "select $a $b from " + this._modelName
					+ " where $a <" + OWL.sameAs.getURI() + "> $b and $a <"
					+ RDF.type.getURI() + "> <"
					+ Display.PreferredTerm.getURI() + ">;";
			Answer answer = interpreter.executeQuery(queryString);
			answer.beforeFirst();
			while (answer.next()) {
				String p = ((URIReferenceImpl) answer.getObject(0)).getURI().toString();
				String s = ((URIReferenceImpl) answer.getObject(1)).getURI().toString();
				primaryEquivalences.add(p);
				secondaryEquivalences.add(s);
			}
			answer.close();
		} catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}
}
