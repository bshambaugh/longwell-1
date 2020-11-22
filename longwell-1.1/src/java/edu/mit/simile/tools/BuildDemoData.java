package edu.mit.simile.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.ModelLoader;

import edu.mit.simile.inferencing.JenaReasoner;
import edu.mit.simile.inferencing.Reasoner;
import edu.mit.simile.inferencing.SimileReasoner;

/**
 * This file creates the demo data with and without inference information for
 * Haystack.
 * 
 * @author Mark H. Butler
 */
public class BuildDemoData {
    /**
     * This class filters the files that contain RDF data.
     */
    class RDFFileFilter implements FilenameFilter {
        public boolean accept(File pathname, String file) {
            return file.matches(".*\\.rdf(s?)|.*\\.n3|.*\\.owl");
        }
    }

    protected static Model readModel(Model model, String file) {
        return readModel(model, new File(file));
    }

    protected static Model readModel(Model model, File file) {
        try {
            String syntax = ModelLoader.guessLang(file.getCanonicalPath());
            FileInputStream fis = new FileInputStream(file);
            model.read(fis, "", syntax);
            fis.close();
        } catch (Exception e) {
            System.out.println("Failed loading model: " + e);
            e.printStackTrace();
        }
        return model;
    }

    protected static Model loadModel(Model model, File data) {
        File[] files = data.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                model = loadModel(model, file);
            } else if (file.getName().matches(".*\\.rdf(s?)|.*\\.n3|.*\\.owl")) {
                model = readModel(model, file);
            }
        }
        return model;
    }
    
    private static Model getModelFromFiles(Properties props) throws Exception {
        Model model = ModelFactory.createDefaultModel();
        
        File data = new File(props.getProperty("data.path", "WEB-INF/data/"));
        
        BuildDemoData b = new BuildDemoData();
        File[] files = data.listFiles();
        
        if (files != null) {
            model = loadModel(model, data);
        } else {
            throw new Exception("You must define a data.path that points to a directory");
        }
        
        return model;
    }

    /**
     * Create three datasets for Haystack, one with no inference, one with just
     * subclass inference, and the other with the Artstor to OCW inference
     * mapping.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: BuildDemoData <dir> <file> [<jenaflag>]");
            System.out.println("where <dir> is the output directory");
            System.out.println("where <file> is a properties file");
            System.out.println("where <jenaflag> is any value");
            System.exit(0);
        }

        Properties dataProps = new Properties();

        try {
            File props = new File(args[1]);
            FileInputStream fis = new FileInputStream(props);
            dataProps.load((InputStream) fis);
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }

        	Model model = ModelFactory.createDefaultModel();
        try {
            model = getModelFromFiles(dataProps);
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        
        System.out.println("Writing no inferencing model...");
        writeModel(model, args[0] + "simileDemoNoInference.rdf");
        System.out.println("Uninferenced model contains " + model.size() + " statements");

        System.out.println("Inferencing...");        	
        if (args.length == 2) {
            Reasoner reasoner = new SimileReasoner();
            writeModel(reasoner.process(model), args[0] + "simileDemoMappingInference.rdf");
        } else {
            Reasoner reasoner = new JenaReasoner();
            writeModel(reasoner.process(model), args[0] + "simileDemoJenaInference.rdf");
        }
        
        System.out.println("Inferenced model contains " + model.size() + " statements");
    }

    private static void writeModel(Model model, String name) {
        try {
            File output = new File(name);
            output.createNewFile();
            model.write(new FileOutputStream(output, false));
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
