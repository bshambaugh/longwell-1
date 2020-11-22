package edu.mit.simile.longwell.model;

import java.util.HashMap;
import java.util.Vector;

import edu.mit.simile.longwell.values.LabelledResource;

/**
 * A class for representing each individual result for browsing.
 *
 * @author Ryan Lee
 */
public class Result {
    ///// Member variables

    /**
     * A string representation of the URI for the result.
     */
    private LabelledResource _self;

    /**
     * A HashMap of properties of the result's subject; the properties
     * themselves are LabelledResourceCollections
     */
    private HashMap _properties;

    /**
     * A Vector of subjects that link to this result subject.
     */
    private Vector _inbound;
    
    ///// Constructor

    /**
     * Creates a new result.
     *
     * @param self        The URI of the result resource
     * @param properties  The HashMap mapping property resources to
     *                    LabelledResourceCollections
     * @param inbound     The Vector of inbound links to the result subject
     */
    public Result(LabelledResource self, HashMap properties, Vector inbound) {
        setSelf(self);
        setProperties(properties);
        setInbound(inbound);
    }

    ///// Bean setters

    /**
     * Returns the URI of the result.
     *
     * @return The URI of the result as a <code>LabelledResource</code>.
     */
    public LabelledResource getSelf() {
        return this._self;
    }

    /**
     * Returns the properties of this result.
     *
     * @return The properties of this result as a <code>HashMap</code>.
     */
    public HashMap getProperties() {
        return this._properties;
    }

    /**
     * Returns the inbound links to this result.
     * 
     * @return The inbound links to this result as a <code>Vector</code>.
     */
    public Vector getInbound() {
        return this._inbound;
    }
    
    ///// Bean accessors

    /**
     * Sets the URI for the result.
     *
     * @param self  The URI of the result resource.
     */
    private void setSelf(LabelledResource self) {
        this._self = self;
    }

    /**
     * Sets the properties for the result.
     *
     * @param properties  The <code>HashMap</code> of properties.
     */
    private void setProperties(HashMap properties) {
        this._properties = properties;
    }
    
    /**
     * Sets the inbound links for the result.
     * 
     * @param inbound  The <code>Vector</code> of inbound links.
     */
    private void setInbound(Vector inbound) {
        this._inbound = inbound;
    }
}
