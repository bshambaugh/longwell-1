package edu.mit.simile.longwell.values;

import java.io.IOException;

/**
 * A class to represent a facet restriction which consists of a LabelledResource
 * to represent the property, a LabelledResource to represent the value
 * and a string representing the subsequent query string if this restriction
 * was removed.
 */
public class FacetRestriction {
    private LabelledResource property;
    private LabelledResource value;

    // remove - the next state of the facets if this restriction is removed
    private String remove;

    /**
    * Create a new facet restriction
    */
    public FacetRestriction(String propertyURI, String propertyLabel, String valueURI,
        String valueLabel, String remove) {
        this.property = new LabelledResource(propertyURI, propertyLabel);
        this.value = new LabelledResource(valueURI, valueLabel);
        this.remove = remove;
    }

    /**
     * Get the property Label
     *
     * @return
     */
    public String getFacetLabel() {
        return property.getLabel();
    }

    /**
     * Get the property URI
     *
     * @return
     */
    public String getFacetURI() {
        return property.getResource();
    }

    /**
     * Get the facet value label
     *
     * @return
     */
    public String getValueLabel() {
        return value.getLabel();
    }

    /**
     * Get the facet value URI
     *
     * @return
     */
    public String getValueURI() {
        return value.getResource();
    }

    /**
     * Get the facet value URI in encoded form.
     *
     * @return
     *
     * @throws IOException
     */
    public String getEncodedValueURI() throws IOException {
        return value.getEncodedURI();
    }
    
    public String getEncodedFacetURI() throws IOException {
      return property.getEncodedURI();
    }

    /**
     * Get the subsequent query string if this facet is removed.
     *
     * @return
     */
    public String getRemove() {
        return remove;
    }
}
