package edu.mit.simile.longwell.values;


import java.util.Collection;


/**
 * A class to represent a collection of labelled resources
 */
public class LabelledResourceCollection {
    private LabelledResource property;
    private Collection values;

    /**
     * Creates a new LabelledResourceCollection object.
     *
     * @param property A LabelledResource representing the property described by this collection.
     * @param values The collection of values.
     */
    public LabelledResourceCollection(LabelledResource property, Collection values) {
        this.property = property;
        this.values = values;
    }

    /**
     * Get the property.
     *
     * @return The LabelledResource representing the property.
     */
    public LabelledResource getProperty() {
        return property;
    }

    /**
     * Get the values.
     *
     * @return The collection of values.
     */
    public Collection getValues() {
        return values;
    }

    /**
     * How many values are there?
     *
     * @return
     */
    public int getSize() {
        return values.size();
    }
}
