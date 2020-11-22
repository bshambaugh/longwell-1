package edu.mit.simile.knowle.model;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * A class to represent a labelled resource, modified for Knowle to
 * include an alternate title.
 *
 * @author Mark H. Butler
 * @author Ryan Lee
 */
public class LabelledResource implements Comparable {
    protected String _uri;
    protected String _label;
    protected String _altTitle;

    /**
     * Creates a new LabelledResource object.
     *
     * @param uri The URI of the object
     * @param label The label of the object
     */
    public LabelledResource(String uri, String label, String altTitle) {
        this._uri = uri;
        this._label = label;
        this._altTitle = altTitle;
    }

    /**
     * Creates a new LabelledResource object.
     *
     * @param uri The URI of the object
     * @param label The label of the object
     */
    public LabelledResource(String uri, String label) {
        this._uri = uri;
        this._label = label;
		this._altTitle = label;
    }

    /**
     * Creates a new LabelledResource object.
     *
     * @param label The label of the object
     */
    public LabelledResource(String label) {
        this._uri = null;
        this._label = label;
        this._altTitle = null;
    }

    /**
     * Is this term a literal?
     *
     * @return
     */
    public boolean getIsLiteral() {
        return (null == this._uri);
    }

    /**
     * Get the URI for this term
     *
     * @return
     */
    public String getResource() {
        return this._uri;
    }

    /**
     * Get the label for this term
     *
     * @return
     */
    public String getLabel() {
        return this._label;
    }

    /**
     * Get the alternate title for this term
     *
     * @return
     */
    public String getAltTitle() {
		return this._altTitle;
    }

    /**
     * Get the encoded URI for this term
     *
     * @return
     * @throws IOException
     */
    public String getEncodedURI() throws IOException {
        return URLEncoder.encode(this._uri, "ISO-8859-1");
    }

    /**
     * Compare this term to another term
     *
     * @param f The term to compare this term to.
     *
     * @return
     */
    public int compareTo(Object f) {
        return this._label.compareTo(((LabelledResource) f).getLabel());
    }

    /**
     * Is this term the same as another term?
     *
     * @param f The term to compare this term to.
     *
     * @return
     */
    public boolean equals(Object f) {
       	if (!(f instanceof LabelledResource)) {
            return false;
        }

        LabelledResource _f = (LabelledResource) f;
        
    	if(null == this._uri && null == _f.getResource()) {
    		return true;
    	} else if(null == this._uri) {
    		return false;
    	}

        return (this._uri.equals(_f.getResource()));
    }

    /**
     * The hashcode of this object.
     *
     * @return
     */
    public int hashCode() {
		if(null == this._uri)
	    	return -1;
		else
	    	return this._uri.hashCode();
    }
}
