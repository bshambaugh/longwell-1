/*
 * Created on Jun 18, 2004
 */
package edu.mit.simile.longwell.model.threestore;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.mit.simile.longwell.BrowserConfig;
import edu.mit.simile.longwell.model.RemoteFacetModel;
import edu.mit.simile.vocabularies.Display;


/**
 * @author ryanlee
 */
public class ThreeStoreRDQLModel extends RemoteFacetModel {
    private String _modelURI;
    
    public ThreeStoreRDQLModel(Model model, String indexPath, String modelURI) throws Exception {
        super(model, indexPath);
        this._modelURI = modelURI;
    }
    
    protected void createCachedModel(BrowserConfig bc) throws Exception {
        ThreeStoreQueryHandler handler = new ThreeStoreQueryHandler(this._modelURI);
        
        handler.setQuery("SELECT ?a, ?b WHERE (?a, <" + RDFS.label.getURI() + ">, ?b)");
        Vector labels = handler.execute().getResults();
        Iterator li = labels.iterator();
        while (li.hasNext()) {
            ResultBinding rb = (ResultBinding) li.next();
            Resource a = (Resource) rb.get("a");
            RDFNode b = (RDFNode) rb.get("b");
            this._cachedModel.add(a, RDFS.label, b);
        }

        handler.setQuery("SELECT ?a, ?b WHERE (?a, <" + RDFS.subClassOf.getURI() + ">, ?b)");
        Vector subclasses = handler.execute().getResults();
        Iterator sci = subclasses.iterator();
        while (sci.hasNext()) {
            ResultBinding rb = (ResultBinding) sci.next();
            Resource a = (Resource) rb.get("a");
            RDFNode b = (Resource) rb.get("b");
            this._cachedModel.add(a, RDFS.subClassOf, b);
        }

        Iterator j = bc.getConfiguredClasses().iterator();

        while (j.hasNext()) {
            handler.setQuery("SELECT ?a, ?b, ?c WHERE (?a, <"+ RDF.type.getURI() + ">, <" + (String) j.next() + ">), (?a, ?b, ?c)");
            Vector allprops = handler.execute().getResults();
            for (Iterator iter = allprops.iterator(); iter.hasNext(); ) {
            		ResultBinding rbind = (ResultBinding) iter.next();
            		Object objA = rbind.get("a");
            		Object objB = rbind.get("b");
            		Object objC = rbind.get("c");
            		this._cachedModel.add((Resource) objA, ResourceFactory.createProperty(((Resource) objB).getURI()), (RDFNode) objC);
            }
        }

        // for each configured object type
        Iterator i = bc.getConfiguredClasses().iterator();

        while (i.hasNext()) {
            Resource t = ResourceFactory.createResource((String) i.next());

            // for each data object in the model of that type
            
            // The following is a workaround should 3store barf
            // String query = "SELECT ?a, ?b, ?c WHERE (?a, ?b, ?c) AND ?b = <" + RDF.type.getURI() + "> AND ?c = <"+ t.getURI() + ">";
            String query = "SELECT ?a WHERE (?a, <" + RDF.type.getURI() + ">, <"+ t.getURI() + ">)";
            handler.setQuery(query);
            Iterator ti = handler.execute().getResults().iterator();

            while (ti.hasNext()) {
                ResultBinding rb = (ResultBinding) ti.next();
                Resource r = (Resource) rb.get("a");
                this._objects.add(r.getURI());
            }
        }
    }

   /**
    * This creates a list of equivalent terms so we can hide them in the UI.
    */
   public void createPreferredTerms() {
       ThreeStoreQueryHandler handler = new ThreeStoreQueryHandler(this._modelURI);
       // String query = "SELECT ?a, ?b, ?c WHERE (?a, ?b, ?c) AND ?b = <" + RDF.type.getURI() + "> AND ?c = <"+ Display.PreferredTerm.getURI() + ">";
       String query = "SELECT ?a WHERE (?a, <" + RDF.type.getURI() + ">, <"+ Display.PreferredTerm.getURI() + ">)";
       handler.setQuery(query);
       Vector pt = handler.execute().getResults();
       Iterator pti = pt.iterator();

       while (pti.hasNext()) {
           ResultBinding rb = (ResultBinding) pti.next();
           Resource pref = (Resource) rb.get("a");
           primaryEquivalences.add(pseudoUri(pref));

           // a secondary term is any term that is subject to a sameAs relation
           // but is not a preferred term
           ThreeStoreQueryHandler shandler = new ThreeStoreQueryHandler(this._modelURI);
           shandler.setQuery("SELECT ?b WHERE (<" + pref.getURI() + ">, <" + OWL.sameAs.getURI() + ">, ?b)");
           Vector sa = shandler.execute().getResults();
           Iterator sai = sa.iterator();
           
           while (sai.hasNext()) {
               ResultBinding sarb = (ResultBinding) sai.next();
               Resource sec = (Resource) sarb.get("b");
               secondaryEquivalences.add(pseudoUri(sec));
           }
       }
   }

   public void createIndex() {
       // TO DO write!
   }

    	class ThreeStoreQueryHandler {
    	    private String _queryModelURI;
    	    private String _query;
    	    private final String _queryVar = "?query=";
    	    
    	    public ThreeStoreQueryHandler(String queryModelURI) {
    	        this._queryModelURI = queryModelURI;
    	    }
    	    
    	    public void setQuery(String query) {
    	        try {
    	            this._query = URLEncoder.encode(query, "ISO-8859-1");
    	        } catch (Exception e) {
    	            System.err.println(e.toString());
    	            e.printStackTrace();
    	        }
    	    }
    	    
    	    public ThreeStoreQueryResults execute() {
    	        URL queryURL = null;
    	        SAXParserFactory factory = SAXParserFactory.newInstance();
    	        SAXParser parser = null;
    	        ThreeStoreContentHandler handler = new ThreeStoreContentHandler();
    	        
    	        ThreeStoreQueryResults qr = null;
    	        
    	        try {
    	            parser = factory.newSAXParser();
    	            queryURL = new URL(this._queryModelURI + this._queryVar + this._query);
        	        InputStream is = queryURL.openStream();
        	        parser.parse(is, handler);
        	        is.close();
        	        qr = handler.results();
    	        } catch (Exception e) {
    	            System.err.println(e.toString());
    	            e.printStackTrace();
    	        }
    	        return qr;
    	    }
    	}
    	
    	class ThreeStoreQueryResults {
    	    Vector _results = new Vector();
    	    
    	    public void addRow(ResultBinding rb) {
    	        this._results.add(rb);
    	    }
    	    
    	    public Vector getResults() {
    	        return this._results;
    	    }
    	}
    	
    class ThreeStoreContentHandler extends DefaultHandler {
        boolean isTable, isRow, isColumn;
        String _value = "";
        String _name;
        String _type;
        ResultBinding _rb = new ResultBinding();
        ThreeStoreQueryResults _results = new ThreeStoreQueryResults();
        
        public void startElement(String uri, String localName, String qName, Attributes atts) {
            if (qName.equals("table")) isTable = true;
            else if (qName.equals("row")) isRow = true;
            else if (qName.equals("column")) {
                isColumn = true;
                this._name = atts.getValue("name");
                this._type = atts.getValue("type");
            }
        }
        
        public void endElement(String uri, String localName, String qName) {
            if (qName.equals("table")) isTable = false;
            else if (qName.equals("row")) {
                isRow = false;
                this._results.addRow(this._rb);
                this._rb = new ResultBinding();
            } else if (qName.equals("column")) {
                isColumn = false;
                if (this._type.equals("uri")) {
                    this._rb.add(this._name, ResourceFactory.createResource(this._value));
                } else if (this._type.equals("literal")) {
                    this._rb.add(this._name, ResourceFactory.createPlainLiteral(this._value));
                }
                this._value = "";
            }
        }
        
        public void characters(char[] chars, int start, int length) {
            if (isColumn) {
                this._value += new String(chars, start, length);
            }
        }
        
        public ThreeStoreQueryResults results() {
            return this._results;
        }
    }
}
