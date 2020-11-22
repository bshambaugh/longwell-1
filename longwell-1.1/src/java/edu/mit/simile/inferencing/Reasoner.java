
package edu.mit.simile.inferencing;

import com.hp.hpl.jena.rdf.model.Model;

public interface Reasoner {

	public Model process(Model model);
	
}
