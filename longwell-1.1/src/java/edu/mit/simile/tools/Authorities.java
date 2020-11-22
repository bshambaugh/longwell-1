package edu.mit.simile.tools;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.mit.simile.vocabularies.Person;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.Vector;


public class Authorities {
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        Vector result = new Vector();

        // data files
        result.add("artstor.rdf");
        result.add("21F_027J_VisualizingCultures.rdf");
        result.add("4_651_20th_Century_Art_Fall_2002_Course_Metadata.rdf");
        result.add("21H_433_TheAgeOfReason.rdf");

        Iterator i = result.iterator();

        while (i.hasNext()) {
            File input = new File(args[0] + (String) i.next());

            try {
                model.read(new FileInputStream(input), "");
            } catch (Exception e) {
                System.out.println(e.toString());
                e.printStackTrace();
            }
        }

        StmtIterator j = model.listStatements((Resource) null, VCARD.FN, (RDFNode) null);
        StringBuffer inexact = new StringBuffer();

        while (j.hasNext()) {
            Statement statement = j.nextStatement();
            Resource subject = statement.getSubject();
            String name = statement.getString();
            String given = null;
            String family = null;
            String birth = null;
            String death = null;

            if (subject.hasProperty(VCARD.Given) && subject.hasProperty(VCARD.Family)) {
                given = subject.getProperty(VCARD.Given).getString();
                family = subject.getProperty(VCARD.Family).getString();
            }

            if (subject.hasProperty(Person.birth)) {
                birth = subject.getProperty(Person.birth).getString();
            }

            if (subject.hasProperty(Person.death)) {
                death = subject.getProperty(Person.death).getString();
            }

            String oclcResult = oclcQuery(name, given, family, birth, death);
            String wikipediaResult = wikipediaQuery(name, given, family);

            if ((oclcResult.trim().length() > 0) || (wikipediaResult.trim().length() > 0)) {
              StringBuffer currentResult = new StringBuffer();
                currentResult.append("<" + subject.getURI() + ">\n");
                currentResult.append("  ## " + name + "\n");

                if (oclcResult.trim().length() > 0) {
                    currentResult.append(oclcResult);
                }

                if (wikipediaResult.trim().length() > 0) {
                    currentResult.append(wikipediaResult);
                }

                currentResult.append("  .\n");           
              if (oclcResult.startsWith("  ## oclc matches")) {
                // inexact match
                inexact.append(currentResult);
              } else {
                System.out.println(currentResult);
              }
            }
        }
        System.out.println("## inexact matches #############");
        System.out.println();
        System.out.println(inexact.toString());
    }

    static String wikipediaQuery(String name, String given, String family) {
        StringBuffer input = new StringBuffer();
        StringBuffer result = new StringBuffer();

        try {
            String uri = null;

            if ((given != null) && (family != null)) {
                uri = "http://en.wikipedia.org/wiki/" + given.trim() + "_" + family.trim();
            } else {
                uri = "http://en.wikipedia.org/wiki/" + name.replace(' ', '_');
            }

            URL theURL = new URL(uri);
            InputStream s = theURL.openStream();

            int ch;

            while ((ch = s.read()) != -1) {
                input.append((char) ch);
            }

            s.close();

            String inputString = input.toString();

            if (inputString.indexOf("(There is currently no text in this page)") > 0) {
                // do nothing - no matches
            } else {
                String searchStart = "From Wikipedia, the free encyclopedia.";
                int start = inputString.indexOf(searchStart) + searchStart.length();
                String searchEnd = "</div><br clear=all>";
                int end = inputString.indexOf(searchEnd);
                String description = removeWhitespaces(inputString.substring(start, end)
                                                                  .replaceAll("<[^>]*>", "").trim());
                result.append("  wikipedia:description \"\"\"" + description + "\n");
                result.append("  \"\"\" ;\n");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return result.toString();
    }

    static String oclcQuery(String name, String given, String family, String birth, String death) {
        StringBuffer input = new StringBuffer();
        StringBuffer result = new StringBuffer();

        try {
            String uri = "http://alcme.oclc.org/eprintsUK/services/NACOMatch" +
                "?method=getCompleteSelectedNameAuthority" +
                "&xsl=http%3A%2F%2Falcme.oclc.org%2FeprintsUK%2FeprintsUK.xsl" +
                "&serviceType=rest" + "&name=";

            if ((given != null) && (family != null)) {
                uri += (URLEncoder.encode(family.trim() + ", " + given.trim(), "ISO-8859-1") +
                "&maxList=10&isPersonalName=true");
            } else {
                uri += (URLEncoder.encode(name.trim(), "ISO-8859-1") +
                "&maxList=10&isPersonalName=true");
            }

            URL theURL = new URL(uri);
            InputStream s = theURL.openStream();

            int ch;

            while ((ch = s.read()) != -1) {
                input.append((char) ch);
            }

            s.close();

            String inputString = input.toString();

            if ((inputString.indexOf("<phraseMatches hitCount=\"0\">") > 0) &&
                    (inputString.indexOf("<wordMatches hitCount=\"0\">") > 0)) {
                // do nothing - no matches
            } else {
                String phraseMatchString = "<phraseMatches hitCount=\"";
                int phraseMatchPos = inputString.indexOf(phraseMatchString) +
                    phraseMatchString.length();
                String phaseMatches = inputString.substring(phraseMatchPos,
                        inputString.indexOf("\"", phraseMatchPos));
                String wordMatchString = "<wordMatches hitCount=\"";
                int wordMatchPos = inputString.indexOf(wordMatchString) + wordMatchString.length();
                String wordMatches = inputString.substring(wordMatchPos,
                        inputString.indexOf("\"", wordMatchPos));

                int matches = Integer.parseInt(phaseMatches) + Integer.parseInt(wordMatches);
                StringBuffer nameResult = new StringBuffer();
                boolean allmatches = true;

                if (matches > 1) {
                    // there is more than one match - need to try some heuristics for disambiguation
                    if (birth != null) {
                        // if birth is not null, check the established form contains birth
                        String searchString = inputString;

                        while ((searchString.indexOf("<establishedForm>") > 0) && allmatches) {
                            String establishedForm = getEstablishedForm(searchString);

                            if (establishedForm.indexOf(birth) > 0) {
                                allmatches = false;
                                nameResult.append(getName(searchString));
                            }

                            searchString = searchString.substring(searchString.indexOf("</match>") +
                                    new String("</match>").length());
                        }
                    }

                    // if this yields a single result, use it                  
                    // else if death is not null, check the established form contains death
                    // if this yields a single result, use it
                    // else return all matches, require human intervention
                } else {
                    allmatches = false;
                    nameResult.append(getName(inputString));
                }

                if (allmatches) {
                    String searchString = inputString;
                    int match = 1;
                    nameResult.append("  ## oclc matches\n");

                    while (searchString.indexOf("<establishedForm>") > 0) {
                        nameResult.append("  ## match " + match + "\n");
                        nameResult.append(getName(searchString));
                        searchString = searchString.substring(searchString.indexOf("</match>") +
                                new String("</match>").length());
                        match++;
                    }
                }

                result.append(nameResult);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return result.toString();
    }

    public static String getEstablishedForm(String searchString) {
        String establishedForm = "<establishedForm>";

        return searchString.substring(searchString.indexOf(establishedForm) +
            establishedForm.length(), searchString.indexOf("</establishedForm>"));
    }

    public static String getName(String searchString) {
        StringBuffer nameResult = new StringBuffer();

        String establishedForm = getEstablishedForm(searchString);
        nameResult.append("  oclc:establishedForm \"" + establishedForm + "\" ; \n");

        String personUri = searchString.substring(searchString.indexOf("<uri>") +
                new String("<uri>").length(), searchString.indexOf("</uri>"));
        nameResult.append("  owl:sameAs <" + personUri + "> ;\n");

        String citation = searchString.substring(searchString.indexOf("<citation>") +
                new String("<citation>").length(), searchString.indexOf("</citation>"));
        nameResult.append("  oclc:citation \"" + citation + "\" ;\n");

        return nameResult.toString();
    }

    public static String removeWhitespaces(String diff) {
        // remove unnecessary whitespaces
        if (diff == null) {
            return null;
        }

        // replace multiple whitespace chars with a single space
        diff = diff.replaceAll("[ \n\r\t][ \n\r\t]", " ");

        while (diff.lastIndexOf("  ") != -1) {
            diff = diff.replaceAll("[ \n\r\t][ \n\r\t]", " ");
        }

        // remove leading whitespace 
        diff = diff.replaceAll("^ *", "");

        // remove trailing whitespace 
        diff = diff.replaceAll(" *$", "");

        return diff;
    }
}
