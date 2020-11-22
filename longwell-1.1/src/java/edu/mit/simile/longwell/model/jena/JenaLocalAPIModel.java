package edu.mit.simile.longwell.model.jena;

import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.simile.longwell.model.LocalFacetModel;


/**
 * A FacetModel that uses the Jena API to query the RDF model.
 *
 * @author Mark H. Butler
 */
public class JenaLocalAPIModel extends LocalFacetModel {
	
	/**
	 * Creates a new JenaLocalAPIModel object.
	 *
	 * @param model The RDF model containing the dataset.
	 * @param bc The Browser Configuration.
	 */
	public JenaLocalAPIModel(Model model, String indexPath) throws Exception {
		super(model, indexPath);
	}
}
