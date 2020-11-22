package edu.mit.simile.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import edu.mit.simile.vocabularies.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class takes nodes that are owl:sameAs and mashes them
 * into one node.  The inferencing should already be finished
 * before running this code.
 *
 * @author Ryan Lee
 */
public class SmushNodes {
    /**
     * Load in the data and proceed with 'smushing.'
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("java SmushNodes <rdf file>");
            System.err.println("Loads <rdf file> and 'smushes' nodes");
            System.exit(0);
        }
        
        String filename = args[0];
        
        System.err.println("File: " + filename);
        
        long startTime = (new Date()).getTime();
        
        try {
            Model m = ModelFactory.createDefaultModel();
            Model mOut = ModelFactory.createDefaultModel();
            
            File file = new File(filename);
            
            String name = file.toString();
            
            InputStream in = new FileInputStream(file);
            System.err.println("Begin loading");
            m.read(in, "");
            System.err.println("Finished loading " + name);
            
            long endTime = (new Date()).getTime();
            System.err.println("Time to read in RDF data and schemas: " + (endTime - startTime) + " milliseconds");
            
            Vector subjects = new Vector();
            Vector nonSubjects = new Vector();
            ResIterator ri = m.listSubjects();
            while(ri.hasNext()) {
                Resource subj = ri.nextResource();
                if(m.contains(subj, RDF.type, Display.PreferredTerm)
                        || !(m.contains(subj, OWL.sameAs, (RDFNode) null)
                                || m.contains((Resource) null, OWL.sameAs, subj))) {
                    subjects.add(subj);
                } else {
                    nonSubjects.add(subj);
                }
            }
            System.err.println(subjects);
            System.err.println(nonSubjects);
            Iterator i = subjects.iterator();
            while(i.hasNext()) {
                Resource subj = (Resource) i.next();
                StmtIterator si = m.listStatements(subj, (Property) null, (RDFNode) null);
                while(si.hasNext()) {
                    Statement s = si.nextStatement();
                    if(!nonSubjects.contains(s.getObject())) {
                        mOut.add(s);
                    }
                }
            }
            mOut.write(System.out);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
