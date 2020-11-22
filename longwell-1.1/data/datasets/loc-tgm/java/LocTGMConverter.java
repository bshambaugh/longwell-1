import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import java.net.URLEncoder;

/**
 * @author Mark H. Butler
 */
public class LocTGMConverter {
  
  // For more info on SKOS see http://www.w3.org/2001/sw/Europe/reports/thes/
  // http://www.w3.org/2001/sw/Europe/reports/thes/1.0/guide/
  
  // here we extend SKOS so we have URIs for alternative terms
  
    public static void main(String[] args) throws Exception {
    	
        System.out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        System.out.println("<rdf:RDF");
        System.out.println("  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
        System.out.println("  xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"");
        System.out.println("  xmlns:owl=\"http://www.w3.org/2002/07/owl#\"");
        System.out.println("  xmlns:skosext=\"http://simile.mit.edu/2004/04/ontologies/skosext#\"");
        System.out.println("  xmlns:smap=\"http://www.w3c.rl.ac.uk/2003/11/21-skos-mappings#\"");
        System.out.println("  xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\">");
        
        System.out.println("  <skos:ConceptScheme rdf:about=\"http://simile.mit.edu/2004/04/loc/tgm#\" xml:lang=\"en\">");

        /* Looking at the SKOS vocabularies produced by OCLC e.g.
           http://staff.oclc.org/~vizine/SKOS/gsafd-lcsh.2004-05-24.skos.xml
           it would be a good idea to add the following metadata
           also we need to differentiate between LOC TGM I and LOC TGM II

        System.out.println("<!--");
        
        System.out.println("<dc:identifier rdf:datatype=\"http://www.w3.org/2001/XMLSchema#anyURI\">");
        System.out.println("the identifier");
        System.out.println("</dc:identifier>");

        System.out.println("<dc:title rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("the title");
        System.out.println("</dc:title>");

        System.out.println("<dcterms:alternate rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("alternate");
        System.out.println("</dcterms:alternate>");

        System.out.println("<dcterms:abstract rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("abstract");
        System.out.println("</dcterms:abstract>");

        System.out.println("<dc:description rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("description");
        System.out.println("</dc:description>");

        System.out.println("<dc:creator rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("creator");
        System.out.println("</dc:creator>");

        System.out.println("<dc:contributor rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("contributors");
        System.out.println("</dc:contributor>");

        System.out.println("<dc:mediator rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">");
        System.out.println("mediators");
        System.out.println("</dc:mediator>");

        System.out.println("<dc:language>");
        System.out.println("Language");
        System.out.println("</dc:language>");

        System.out.println("<dc:format>");
        System.out.println("format");
        System.out.println("</dc:format>");

        System.out.println("<dcterms:extent rdf:parseType=\"Resource\">");
        System.out.println("size");
        System.out.println("</dcterms:extent>");

        System.out.println("<dcterms:modified rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">");
        System.out.println("date time modified");
        System.out.println("</dcterms:modified>");

        System.out.println("<dcterms:conformsTo rdf:resource=\"http://www.loc.gov/marc/authority/\"/>");
        System.out.println("-->");

		*/
        
        System.out.println("</skos:ConceptScheme>");

        String mainterm = null;
        boolean recordhasAUse = false; 
        boolean newRecord = false;
        String preferredLabel = null;
        
        BufferedReader input = new BufferedReader(new FileReader(args[0]));
        String line = null;
        String nextline = null;
        
        while (true) {
        		if (nextline == null) {
                    line = input.readLine();
                    nextline = input.readLine();        			
        		} else {
                    line = nextline;
                    nextline = input.readLine();        			
        		}
        		
            if (line == null) break;

            // see http://lcweb.loc.gov/rr/print/tgm1/ic.html for more details of TGM
            
            if (line.startsWith("MT: ")) {
              
				// MT: main term
				newRecord = true;
				mainterm = line.substring(4).trim();
				  
				if (nextline.startsWith("USE: ")) {
					// this is an alternative term
					System.out.println("\n<rdf:Description rdf:about="+ createTermURI(mainterm) +">");
					System.out.println("  <rdfs:label>" + createString(mainterm) + "</rdfs:label>");
					System.out.println("  <rdfs:type rdf:resource=\"http://simile.mit.edu/2004/04/ontologies/skosext#Term\"/>");  
				} else {
					System.out.println("\n<rdf:Description rdf:about="+ createURI(line.substring(4)) +">");
					System.out.println("  <skos:inScheme rdf:resource=\"http://simile.mit.edu/2004/04/loc/tgm#\"/>");
					System.out.println("  <skos:prefLabel rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + createString(mainterm) + "</skos:prefLabel>");
					System.out.println("  <skosext:preferredTerm>");
					System.out.println("   <skosext:Term rdf:about=" + createTermURI(mainterm) +">");
					System.out.println("    <rdfs:label>" + createString(mainterm) + "</rdfs:label>");
					System.out.println("   </skosext:Term>");
					System.out.println("  </skosext:preferredTerm>");
					System.out.println("  <rdf:type rdf:resource=\"http://www.w3.org/2004/02/skos/core#Concept\"/>");  
				}
				
				recordhasAUse = true;
            } else  if (line.startsWith("USE: ")) {
				// USE:
				// A cross reference that points catalogers and searchers to an authorized term. 
				// USE references may be made from synonyms, near synonyms, antonyms, inverted phrases, or 
				// other closely related terms or phrases. The reciprocal is UF. 
				
				// Thesauri have relations in both directions, but in SKOS we only want them in one direction. 
				// so if a term has a USE: then we omit it, because it will be mentioned elsewhere
				 System.out.println("  <skosext:definedByConcept rdf:resource="+createURI(line.substring(4))+"/>");
            } else if (line.startsWith("CN: ")) {
	            // CN: Cataloguer's note.
	            // The cataloger's note (CN) clarifies how to use a term or when to use it in conjunction with 
	            // another term ("double indexing"). 
	              System.out.println("  <skos:scopeNote rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + createString(line.substring(4).trim()) + "</skos:scopeNote>");
            } else if (line.startsWith("PN: ")) {
	            // PN: Public note.
	            // The public note (PN) defines a term, explains its scope, or helps a user understand the 
	            // structure of the thesaurus. 
	              System.out.println("  <skos:definition rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + createString(line.substring(4).trim()) + "</skos:definition>");
            } else if (line.startsWith("RT: ")) {            
	            // RT: A related term. An authorized term which is closely related to the term under 
	            // which it is listed, but the relationship is not a hierarchical one. The reciprocal is also RT. 
	              System.out.println("  <skos:related rdf:resource=" + createURI(line.substring(4)) + "/>");
            } else if (line.startsWith("BT: ")) {
	            // BT: Broader term. An authorized term which indicates the more general class to which a 
	            // term belongs. Everything that is true of a term is also true of its broader term(s). 
	              System.out.println("  <skos:broader rdf:resource=" + createURI(line.substring(4)) + "/>");
            } else if (line.startsWith("NT: ")) {  
	            // NT: Narrower term. An authorized term which is narrower in scope and a member of the 
	            // general class represented by the broader term under which it is listed. The reciprocal is BT.
	              System.out.println("  <skos:narrower rdf:resource=" + createURI(line.substring(4)) + "/>");
            } else if (line.startsWith("UF: ")) {  
	            // UF: A term that is not authorized for indexing. 
	            // UF terms are listed primarily for editorial purposes, but they may help searchers by 
	            // clarifying the scope or meaning of a term. The reciprocal is USE. 
	              System.out.println("  <skos:altLabel rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + createString(line.substring(4)) + "</skos:altLabel>");
	              System.out.println("  <skosext:alternativeTerm rdf:resource=" + createTermURI(line.substring(4)) + "/>");
            } else if (line.length() == 0) {
              if (newRecord) {
                System.out.println("</rdf:Description>");
                newRecord = false;
              }
            } else if (line.startsWith("newsection:")) {
              // do nothing
            } else if (line.startsWith("servedOn:")) {
              // do nothing
            } else if (line.startsWith("Facet:")) {
              // do nothing
            } else if (line.startsWith("HN:")) {
              // HN : Historical note
              System.out.println("  <skosext:historicalNote rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + createString(line.substring(4).trim()) + "</skosext:historicalNote>");
            } else {
              System.out.println("<!-- ERROR: "+ line +" -->");
            }                   
        }

        input.close();
        
        System.out.println("</rdf:RDF>");
    }
    
    public static String createString(String term) {
      return term.replaceAll("&", "and");      
    }
    
    public static String createURI(String term) throws Exception {
        return "\"http://simile.mit.edu/2004/04/loc/tgm#" +  URLEncoder.encode(createString(term).trim(), "ISO-8859-1") + "\"";
    }
    
    public static String createTermURI(String term) throws Exception {
        return "\"http://simile.mit.edu/2004/04/loc/tgm#" +  URLEncoder.encode(term.trim(), "ISO-8859-1") + "\"";
    }
}
