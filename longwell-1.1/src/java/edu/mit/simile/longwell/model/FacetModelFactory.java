package edu.mit.simile.longwell.model;

import java.util.Properties;

import java.lang.reflect.Constructor;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * A factory class to make FacetModels of the appropriate type.
 *
 * @author Mark H. Butler
 */
public class FacetModelFactory {
    private final static String classRoot = "edu.mit.simile.longwell.model.";
    
    /**
     * A factory class for making FacetModels
     *
     * @param facetModelType The required FacetModel type.
     * @param model The Jena RDF model that contains the data.
     * @param bc The Browser Configuration object.
     * @param indexPath The path to use when creating persistant indexes (only used by Lucene)
     *
     * @return The FacetModel
     *
     * @throws Exception
     */
    public static FacetModel createFacetModel(String facetModelType, Model model, String indexPath, Properties dataProps) 
    throws Exception {
        FacetModel result = null;
        String store = "";
        Class[] paramTypes = null;
        Object[] params = null;
        
        if (facetModelType.equals("JenaLocalAPIModel")) {
            // this query engine uses Jena
            store = "jena.";
            paramTypes = new Class[2]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            params = new Object[2];
            params[0] = model;
            params[1] = indexPath;
        } else if (facetModelType.equals("JenaLocalRDQLModel")) {
            // this hybrid engine retrieves a subset of the model at start-up using RDQL
            // loads it into an in-memory model, then queries that using Jena
            // this is to compare performance to the other engines particularly for
            // persistant stores and HTTP accessed stores
            store = "jena.";
            paramTypes = new Class[2]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            params = new Object[2];
            params[0] = model;
            params[1] = indexPath;
        } else if (facetModelType.equals("KowariITQLModel")) {
            // This query engine uses Kowari
            String modelName = dataProps.getProperty("kowari.model");
            store = "kowari.";
            paramTypes = new Class[3]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            params = new Object[3];
            params[0] = model;
            params[1] = indexPath;
            params[2] = modelName;
        } else if (facetModelType.equals("KowariLocalAPIModel")) {
            // This query engine uses the Jena overlay for Kowari
            String modelName = dataProps.getProperty("kowari.model");
            store = "kowari.";
            paramTypes = new Class[3]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            params = new Object[3];
            params[0] = model;
            params[1] = indexPath;
            params[2] = modelName;
        } else if (facetModelType.equals("JosekiRDQLModel")) {
            String josekiURI = dataProps.getProperty("joseki.uri");
            store = "joseki.";
            paramTypes = new Class[3]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            params = new Object[3];
            params[0] = model;
            params[1] = indexPath;
            params[2] = josekiURI;
        } else if (facetModelType.equals("ThreeStoreRDQLModel")) {
            String modelURI = dataProps.getProperty("3store.uri");
            store = "threestore."; 
            paramTypes = new Class[3]; 
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            params = new Object[3];
            params[0] = model;
            params[1] = indexPath;
            params[2] = modelURI;
        } else if (facetModelType.equals("SesameRDQLModel")) {
            String serverURI = dataProps.getProperty("sesame.uri");
            String repository = dataProps.getProperty("sesame.repository");
            String user = dataProps.getProperty("sesame.user");
            String password = dataProps.getProperty("sesame.password");
            store = "sesame.";
            paramTypes = new Class[6];
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            paramTypes[3] = Class.forName("java.lang.String");
            paramTypes[4] = Class.forName("java.lang.String");
            paramTypes[5] = Class.forName("java.lang.String");
            params = new Object[6];
            params[0] = model;
            params[1] = indexPath;
            params[2] = serverURI;
            params[3] = repository;
            params[4] = user;
            params[5] = password;
        } else if (facetModelType.equals("SesameSeRQLModel")) {
            String serverURI = dataProps.getProperty("sesame.uri");
            String repository = dataProps.getProperty("sesame.repository");
            String user = dataProps.getProperty("sesame.user");
            String password = dataProps.getProperty("sesame.password");
            store = "sesame.";
            paramTypes = new Class[6];
            paramTypes[0] = Class.forName("com.hp.hpl.jena.rdf.model.Model");
            paramTypes[1] = Class.forName("java.lang.String");
            paramTypes[2] = Class.forName("java.lang.String");
            paramTypes[3] = Class.forName("java.lang.String");
            paramTypes[4] = Class.forName("java.lang.String");
            paramTypes[5] = Class.forName("java.lang.String");
            params = new Object[6];
            params[0] = model;
            params[1] = indexPath;
            params[2] = serverURI;
            params[3] = repository;
            params[4] = user;
            params[5] = password;
        } else {
            throw new Exception("Invalid facet model type: " + facetModelType);
        }
        
        Class m = Class.forName(classRoot + store + facetModelType);
        Constructor c = m.getConstructor(paramTypes);
        result = (FacetModel) c.newInstance(params);
        
        return result;
    }
}
