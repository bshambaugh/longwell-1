package edu.mit.simile.longwell.values;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * A class to represent a collection of labelled resources
 */
public class MultiOrderLabelledResourceCollection {
    private LabelledResource property;
    private Collection values;

    /**
     * Creates a new LabelledResourceCollection object.
     *
     * @param property A LabelledResource representing the property described by this collection.
     * @param values The collection of values.
     */
    public MultiOrderLabelledResourceCollection(LabelledResource property, Collection values,
        int resultsSetSize, String order) {
        this.property = property;

        // remove items that return the same results set
        Iterator i = values.iterator();

        while (i.hasNext()) {
            ControlledTerm value = (ControlledTerm) i.next();

            if (value.getFrequency() == resultsSetSize) {
                i.remove();
            }
        }

        if (order.equals("alphabetic")) {
            TreeSet alphabeticResults = new TreeSet(new ControlledTermSort(false));
            alphabeticResults.addAll(values);
            this.values = (Collection) alphabeticResults;
        } else {
            TreeSet frequencyResults = new TreeSet(new ControlledTermSort(true));
            frequencyResults.addAll(values);
            this.values = (Collection) frequencyResults;
        }
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
