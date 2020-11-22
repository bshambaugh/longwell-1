/**
 * Created on Jun 1, 2004
 */
package edu.mit.simile.longwell.model.kowari;

import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.simile.longwell.model.LocalFacetModel;

/**
 * An implementation of a LocalFacetModel that uses an in-memory Kowari-based model
 * as the local store.
 * 
 * @author ryanlee
 */
public class KowariLocalAPIModel extends LocalFacetModel {
	private String _modelName;
	
	public KowariLocalAPIModel(Model model, String indexPath, String modelName) throws Exception {
		super(model, indexPath);
		this._modelName = modelName;
	}
}
