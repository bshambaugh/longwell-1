package edu.mit.simile.longwell.values;

import java.io.IOException;

import java.net.URLEncoder;

import com.hp.hpl.jena.rdf.model.AnonId;

/**
 * A class to represent a labelled resource
 */
public class LabelledResource implements Comparable {
	protected String uri;
	protected String label;
	protected AnonId _id;
	protected boolean equivalentTerm;
	protected String displayType;
	protected final String ANONURI = "http://simile.mit.edu/anonymous#";
	
	/**
	 * Creates a new LabelledResource object.
	 * 
	 * @param id     The Jena AnonId of the resource
	 * @param label  The label of the object
	 */
	public LabelledResource(AnonId id, String label) {
		this._id = id;
		this.label = label;
	}
	
	/**
	 * Creates a new LabelledResource object.
	 *
	 * @param uri The URI of the object
	 * @param label The label of the object
	 * @param equivalentTerm Is the object involved in any equivalent terms relationships
	 * @param displayType The way the resource should be displayed
	 */
	public LabelledResource(String uri, String label, boolean equivalentTerm, String displayType) {
		parseUri(uri);
		this.label = label;
		this.equivalentTerm = equivalentTerm;
		this.displayType = displayType;
	}
	
	/**
	 * Creates a new LabelledResource object.
	 *
	 * @param uri The URI of the object
	 * @param label The label of the object
	 * @param displayType The way the resource should be displayed
	 */
	public LabelledResource(String uri, String label, String displayType) {
		parseUri(uri);
		this.label = label;
		this.equivalentTerm = false;
		this.displayType = displayType;
	}
	
	/**
	 * Creates a new LabelledResource object.
	 *
	 * @param uri The URI of the object
	 * @param label The label of the object
	 * @param equivalentTerm Is the object involved in any equivalent terms relationships
	 */
	public LabelledResource(String uri, String label, boolean equivalentTerm) {
		parseUri(uri);
		this.label = label;
		this.equivalentTerm = equivalentTerm;
		this.displayType = null;
	}
	
	/**
	 * Creates a new LabelledResource object.
	 *
	 * @param uri The URI of the object
	 * @param label The label of the object
	 */
	public LabelledResource(String uri, String label) {
		parseUri(uri);
		this.label = label;
		equivalentTerm = false;
		this.displayType = null;
	}
	
	/**
	 * Creates a new LabelledResource object.
	 *
	 * @param label The label of the object
	 */
	public LabelledResource(String label) {
		this.uri = null;
		this.label = label;
		equivalentTerm = false;
		this.displayType = null;
	}
	
	protected void parseUri(String uri) {
		if(uri != null) {
			if(uri.startsWith("http://simile.mit.edu/anonymous#")) {
				this.uri = null;
				this._id = new AnonId(uri.substring(uri.indexOf("#") + 1));
			} else {
				this.uri = uri;
				this._id = null;
			}
		}
	}
	
	
	/**
	 * Is this term involved in an equivalent term relationship?
	 *
	 * @return
	 */
	public boolean getIsEquivalent() {
		return equivalentTerm;
	}
	
	/**
	 * Is this term a literal?
	 *
	 * @return
	 */
	public boolean getIsLiteral() {
		return (uri == null && this._id == null);
	}
	
	public boolean getIsAnonymous() {
		return (uri == null && this._id != null);
	}
	
	/**
	 * Get the URI for this term
	 *
	 * @return
	 */
	public String getResource() {
		if (getIsAnonymous())
			return ANONURI + this._id.toString();
		else
			return uri;
	}
	
	/**
	 * Get the label for this term (use the URI as the label if none was provided).
	 *
	 * @return  A human-readable lable for this resource.
	 */
	public String getLabel() {
		if(null == label && null != uri)
			return uri;
		else if (getIsAnonymous() && null == label)
			return "anonymous[" + this._id.toString() + "]";
		else
			return label;
	}
	
	/**
	 * Get the encoded URI for this term
	 *
	 * @return
	 * @throws IOException
	 */
	public String getEncodedURI() throws IOException {
		return URLEncoder.encode(getResource(), "ISO-8859-1");
	}
	
	/**
	 * Gets the display type for this term
	 *
	 * @return The resource describing what type of display this is.
	 */
	public String getDisplayType() {
		return this.displayType;
	}
	
	/**
	 * Is this term a typed display or the default, whatever that may be?
	 *
	 * @return If the term is a typed display
	 */
	public boolean getIsTypedDisplay() {
		return (null != this.displayType);
	}
	
	/**
	 * Compare this term to another term
	 *
	 * @param f The term to compare this term to.
	 *
	 * @return
	 */
	public int compareTo(Object f) {
		return label.compareTo(((LabelledResource) f).label);
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
		
		return (getResource().equals(_f.getResource()));
	}
	
	/**
	 * The hashcode of this object.
	 *
	 * @return
	 */
	public int hashCode() {
		return getResource().hashCode();
	}
}
