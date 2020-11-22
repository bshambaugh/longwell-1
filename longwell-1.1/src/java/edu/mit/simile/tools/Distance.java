package edu.mit.simile.tools;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.values.LabelledResource;
import edu.mit.simile.vocabularies.Display;
import edu.mit.simile.vocabularies.SkosCore;

public class Distance {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        Vector result = new Vector();
        int inputStart = 0;
        
        if (args.length < 1) {
            System.err.println("Usage: distance [--config file] input [input2 ... ]");
            System.err.println("Config defaults to distancefields.n3 in current directory");
            System.exit(0);
        }
        
        // config file
        Model browserConfig = ModelFactory.createDefaultModel();
        File input = null;
        if (args[0].equals("--config")) {
            if (args.length < 3) {
                System.err.println("Expecting input files following config option");
                System.exit(0);
            }
            input = new File(args[1]);
            inputStart = 2;
        } else {
            input = new File("distancefields.n3");
        }
        
        try {
            browserConfig.read(new FileInputStream(input),
                    "http://error.com/arp/does/not/like/blank/base/uris.rdf", "N3");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        
        // data files
        for (int i = inputStart; i < args.length; i++) {
            result.add(args[i]);
        }
        
        Iterator i = result.iterator();
        
        while (i.hasNext()) {
            input = new File((String) i.next());
            
            try {
                model.read(new FileInputStream(input), "");
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }
        
        Vector properties = new Vector();
        StmtIterator si = browserConfig.listStatements((Resource) null, RDF.type, Display.BrowserConfiguration);
        Resource config = si.nextStatement().getSubject();
        StmtIterator ci = browserConfig.listStatements(config, Display.displayFacets, (Resource) null);
        
        while (ci.hasNext()) {
            // a Seq
            Seq displaySeq = browserConfig.getSeq((Resource) ci.nextStatement().getObject());
            NodeIterator ni = displaySeq.iterator();
            
            while (ni.hasNext()) {
                Resource r = (Resource) ni.next();
                properties.add(r.getURI());
            }
        }
        
        // need to scan literals here and calculate all the levenshtein distances
        // only search literals that are facets e.g. objects of RDFS.label
        Vector literalList = new Vector();
        StmtIterator s = model.listStatements((Resource) null, RDFS.label, (RDFNode) null);
        literalList = getLiterals(literalList, s, model, properties);
        
        s = model.listStatements((Resource) null, SkosCore.prefLabel, (RDFNode) null);
        literalList = getLiterals(literalList, s, model, properties);
        
        int maxDistance = 0;
        
        for (int x = 0; x < (literalList.size() - 1); x++) {
            boolean firstTime = true;
            
            for (int y = x + 1; y < literalList.size(); y++) {
                LabelledResource first = (LabelledResource) literalList.get(x);
                LabelledResource second = (LabelledResource) literalList.get(y);
                
                if (!first.getResource().startsWith("http://simile.mit.edu/libraryOfCongress/") ||
                        !second.getResource().startsWith("http://simile.mit.edu/libraryOfCongress/"))
                {
                    int distance = getLevenshteinDistance(first.getLabel(), second.getLabel());
                    int smallest = (first.getLabel().length() < second.getLabel().length())
                    ? first.getLabel().length() : second.getLabel().length();
                    
                    // there is a bit of a hack here, just to get things working for now
                    
                    if ((distance < (smallest / 3)) && (distance < 4) &&
                            !first.getResource().equals(second.getResource())) {
                        if (firstTime) {
                            System.out.println("<" + first.getResource() + ">");
                            System.out.println("  rdf:type disp:PreferredTerm ;");
                            firstTime = false;
                        }
                        
                        // System.out.println("  ## " + first.getLabel() + " , " + " : " + second.getLabel() +" : " + distance);
                        System.out.println("  owl:sameAs " + "<" + second.getResource() + "> ;");
                        
                        // avoid duplicate preferred terms
                        literalList.remove(y);
                    }
                }
            }
            
            if (!firstTime) {
                System.out.println("  .");
                System.out.println();
            }
        }
    }
    
    public static Vector getLiterals(Vector literalList, StmtIterator s, Model model,
            Vector properties) {
        while (s.hasNext()) {
            Statement theStatement = s.nextStatement();
            Resource subject = theStatement.getSubject();
            
            boolean isAConfiguredProperty = false;
            boolean pureThesaurus = true;
            
            StmtIterator s2 = model.listStatements((Resource) null, (Property) null,
                    (RDFNode) subject);
            
            while (s2.hasNext() && !isAConfiguredProperty) {
                pureThesaurus = false;
                Statement parentStatement = s2.nextStatement();
                
                if (properties.contains(parentStatement.getPredicate().getURI())) {
                    isAConfiguredProperty = true;
                }
            }
            
            if (isAConfiguredProperty || pureThesaurus) {
                LabelledResource lr = new LabelledResource(subject.getURI(),
                        theStatement.getLiteral().getString());
                literalList.add(lr);
            }
        }
        
        return literalList;
    }
    
    public static int getLevenshteinDistance(String s, String t) {
        if ((s == null) || (t == null)) {
            throw new IllegalArgumentException("Strings must not be null");
        }
        
        // this source code is taken from
        // http://www.merriampark.com/ld.htm
        
        /*
         The difference between this impl. and the previous is that, rather
         than creating and retaining a matrix of size s.length()+1 by t.length()+1,
         we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
         is the 'current working' distance array that maintains the newest distance cost
         counts as we iterate through the characters of String s.  Each time we increment
         the index of String t we are comparing, d is copied to p, the second int[].  Doing so
         allows us to retain the previous cost counts as required by the algorithm (taking
         the minimum of the cost count to the left, up one, and diagonally up and to the left
         of the current cost count being calculated).  (Note that the arrays aren't really
         copied anymore, just switched...this is clearly much better than cloning an array
         or doing a System.arraycopy() each time  through the outer loop.)
         
         Effectively, the difference between the two implementations is this one does not
         cause an out of memory condition when calculating the LD over two very large strings.
         */
        int n = s.length(); // length of s
        int m = t.length(); // length of t
        
        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }
        
        int[] p = new int[n + 1]; //'previous' cost array, horizontally
        int[] d = new int[n + 1]; // cost array, horizontally
        int[] _d; //placeholder to assist in swapping p and d
        
        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t
        
        char t_j; // jth character of t
        
        int cost; // cost
        
        for (i = 0; i <= n; i++) {
            p[i] = i;
        }
        
        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            
            for (i = 1; i <= n; i++) {
                cost = (s.charAt(i - 1) == t_j) ? 0 : 1;
                
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            
            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }
        
        // our last action in the above loop was to switch d and p, so p now 
        // actually has the most recent cost counts
        return p[n];
    }
}
