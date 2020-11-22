package edu.mit.simile.inferencing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;

/**
 * A simple inference engine that generates the inferences we need to map between the collections.
 *
 * @author Mark H. Butler
 */
public class JenaReasoner implements Reasoner {
	
	public Model process(Model model) {
		Model m = ModelFactory.createDefaultModel();
		Resource configuration = m.createResource(GenericRuleReasonerFactory.URI);
		configuration.addProperty(ReasonerVocabulary.PROPruleSet,"simile.rules");
		// fixme(SM): this doesn't work, how can we load the rules thru the classloader instead that from file?
		
		GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(configuration);
		
		return ModelFactory.createInfModel(reasoner, model);
	}
	
}
