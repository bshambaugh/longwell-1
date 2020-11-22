import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;
import java.net.URLEncoder;

public class HPResearchReports {

    public static void main(String[] args) {
        System.out.println("@prefix dc:      <http://purl.org/dc/elements/1.1/> .");
        System.out.println("@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .");
        System.out.println("@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .");
        System.out.println();

        queryRootPage("http://www.hpl.hp.com/techreports/90/");
        queryRootPage("http://www.hpl.hp.com/techreports/91/");
        queryRootPage("http://www.hpl.hp.com/techreports/92/");
        queryRootPage("http://www.hpl.hp.com/techreports/93/");
        queryRootPage("http://www.hpl.hp.com/techreports/94/");
        queryRootPage("http://www.hpl.hp.com/techreports/95/");
        queryRootPage("http://www.hpl.hp.com/techreports/96/");
        queryRootPage("http://www.hpl.hp.com/techreports/97/");
        queryRootPage("http://www.hpl.hp.com/techreports/98/");
        queryRootPage("http://www.hpl.hp.com/techreports/1999/");
        queryRootPage("http://www.hpl.hp.com/techreports/2000/");
        queryRootPage("http://www.hpl.hp.com/techreports/2001/");
        queryRootPage("http://www.hpl.hp.com/techreports/2002/");
        queryRootPage("http://www.hpl.hp.com/techreports/2003/");
        queryRootPage("http://www.hpl.hp.com/techreports/2004/");
    }

    static String getPage(String uri) throws Exception {
        StringBuffer input = new StringBuffer();

        URL theURL = new URL(uri);
        InputStream s = theURL.openStream();

        int ch;

        while ((ch = s.read()) != -1) {
            input.append((char) ch);
        }

        s.close();

        return input.toString();
    }

    static String queryRootPage(String uri) {
        try {
            String inputString = getPage(uri);

            int next = 0;

            String search = "HPL-";

            while ((next = inputString.indexOf(search, next)) != -1) {
                int end = inputString.indexOf("\"", next + search.length());
                String newURI = inputString.substring((next + search.length()) - 4, end);

                if (newURI.endsWith(".html")) {
                    queryChildPage(uri, newURI);
                }

                next = end;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return null;
    }

    static String queryChildPage(String rootUri, String uri) {
        try {
            String inputString = getPage(rootUri + uri);
            String reportURI = rootUri + uri;
            String lowerCase = inputString.toLowerCase();

            String search = "HPL-";
            int next = lowerCase.indexOf("full");

            if (next == -1) {
                next = lowerCase.indexOf("hp labs technical reports");
            } else {
                next = inputString.indexOf(search, next);

                int end = inputString.indexOf("\"", next + search.length());
                reportURI = rootUri + inputString.substring((next + search.length()) - 4, end);
                next = lowerCase.indexOf("<p>", next);
            }

            next = lowerCase.indexOf("<b>", next) + 3;
            String title = inputString.substring(next, lowerCase.indexOf("</b>", next)).trim().replaceAll("\"", "");
            System.out.println("<" + reportURI + ">");
            System.out.println("  dc:title \"" + title + "\" ;");
            next = lowerCase.indexOf("<i>", next) + 3;

            String authors = splitAuthor(inputString.substring(next,
                        lowerCase.indexOf("</i", next)).trim());

            next = inputString.indexOf("HPL-", next);
            next = lowerCase.indexOf("<br>", next);

            String date = inputString.substring(next + 4, lowerCase.indexOf("<br>", next + 1)).trim();
            System.out.println("  dc:date \"" + date + "\" ;");

            int failsafe = next;
            String subject = null;
            next = lowerCase.indexOf("keyword(s)", next);

            if (next != -1) {
                next = lowerCase.indexOf("</b>", next) + 4;
                
                // markup is broken on some pages e.g. http://www.hpl.hp.com/techreports/2001/HPL-2001-52.html 
                // so make sure we don't end up in abstract
                
                int test = lowerCase.indexOf("abstract:");
                int end = lowerCase.indexOf("<p>", next);
                
                if (test != -1 && end != -1 && test < end) {
                } else  {
                  String subjecterms = inputString.substring(next, end);
                  if (subjecterms.indexOf("No keywords available") == -1) {
                    subject = splitSubject(subjecterms);
                    next = end;
                  }
                }
            } else {
                next = failsafe;
            }

            next = lowerCase.indexOf("abstract:", next);
            next = lowerCase.indexOf("</b>", next) + 4;

            String description = removeWhitespaces(inputString.substring(next,
                        lowerCase.indexOf("<p>", next)).trim().replaceAll("<p>", " ").replaceAll("<P>", " ").replaceAll("\"",
                        "").replaceAll("<[^>]*>",""));
            System.out.println("  dc:abstract \"" + description + "\" ;");
            failsafe = next;
            next = lowerCase.indexOf("pages", next);

            if (next != -1) {
                String noPages = lowerCase.substring(lowerCase.lastIndexOf("<p>", next) + 3,
                        next - 1).trim();
                        // if it contains abstract, don't print because something has gone wrong                                        
                if (noPages.indexOf("abstract")==-1 && noPages.indexOf("<b>")==-1) {             
                  System.out.println("  dc:format \"" + noPages + " pages\" ;");
                }
            } else {
                next = failsafe;
            }

            System.out.println(".");
            System.out.println();
            System.out.println(authors);

            if (subject != null) {
                System.out.println(subject);
            }
        } catch (Exception e) {
            // do nothing
        }

        return null;
    }

    static String splitAuthor(String author) throws Exception {
        int start = 0;
        int next = start;
        StringBuffer result = new StringBuffer();

        while ((next = author.indexOf(";", next)) != -1) {
            String authorname = author.substring(start, next).trim();
            String authorURL = "http://www.hp.com/people/" +
                URLEncoder.encode(authorname, "UTF-8");
            System.out.println("  dc:creator <" + authorURL + "> ;");
            result.append("<" + authorURL + ">\n");
            result.append("  rdfs:label \"" + authorname + "\" .\n\n");
            next = next + 1;
            start = next;
        }

        String authorname = author.substring(start, author.length()).trim();
        String authorURL = "http://www.hp.com/people/" + URLEncoder.encode(authorname, "UTF-8");
        System.out.println("  dc:creator <" + authorURL + "> ;");
        result.append("<" + authorURL + ">\n");
        result.append("  rdfs:label \"" + authorname + "\" .\n\n");

        return result.toString();
    }

    static String splitSubject(String subject) throws Exception {
        if (subject == null) {
            return null;
        }

        if (subject.trim().length() == 0) {
            return null;
        }

        int start = 0;
        int next = start;
        StringBuffer result = new StringBuffer();

        while ((subject.indexOf(";", next) != -1) || (subject.indexOf(",", next) != -1)) {
          if (subject.indexOf(",", next) != -1) {
            next = subject.indexOf(",", next);
          } else {
            next = subject.indexOf(";", next);
          }
                 
            String subjectterm = subject.substring(start, next).trim();
            String subjectURL = "http://www.hp.com/reports/subjecterms/" +
                URLEncoder.encode(subjectterm, "UTF-8");
            System.out.println("  dc:subject <" + subjectURL + "> ;");
            result.append("<" + subjectURL + ">\n");
            result.append("  rdfs:label \"" + subjectterm + "\" .\n\n");
            next = next + 1;
            start = next;
        }

        String subjectterm = subject.substring(start, subject.length()).trim();
        String subjectURL = "http://www.hp.com/reports/subjecterms/" +
            URLEncoder.encode(subjectterm, "UTF-8");
        System.out.println("  dc:subject <" + subjectURL + "> ;");
        result.append("<" + subjectURL + ">\n");
        result.append("  rdfs:label \"" + subjectterm + "\" .\n\n");

        return result.toString();
    }

    public static String removeWhitespaces(String diff) {
        // remove unnecessary whitespaces
        if (diff == null) {
            return null;
        }

        diff = diff.replaceAll("[\n\r\t]", " ");

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
