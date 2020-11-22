/*
 * Created on Jan 27, 2005
 */
package edu.mit.simile.downend;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.mit.simile.RDFBrowser;


/**
 * Those British know how to give their towns good software names.
 * 
 * Downend is a 'bank' interface for allowing piggy-bank to push data to a more central,
 * browsable location.  This should be regarded as a very, very dumb prototype that uses
 * POST and the form element name 'data' to receive data and blindly insert it into the
 * existing model.
 * 
 * It was suggested that PUT be used, but the semantics for PUT do not seem to make sense;
 * however, expect this interface to change dramatically to the point where it might.
 * 
 * @author ryanlee
 */
public class Downend extends RDFBrowser {
    static final long serialVersionUID = 4073875975083068800L;
    
    public void init(ServletConfig config) throws ServletException {
		super.init(config);
        logger.info("Initializing Downend...");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		long startTime = System.currentTimeMillis();
		PrintWriter output = response.getWriter();

		String fileContent = request.getParameter("data");
		StringReader rdr = new StringReader(fileContent);

		response.setContentType("text/plain");
		output.println("RDF added: " + fileContent);
		output.close();
		
		model.read(rdr, "");
		
		long endTime = System.currentTimeMillis();
		logger.info("Time to process filton request: " + format(endTime - startTime));
    }
}
