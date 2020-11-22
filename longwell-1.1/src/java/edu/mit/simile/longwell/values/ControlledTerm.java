package edu.mit.simile.longwell.values;

/**
 * A controlled term is a URI, a label and a frequency.
 *
 * @author Mark H. Butler
 */
public class ControlledTerm extends LabelledResource {
    protected int frequency = 1;

    /**
     * Creates a new ControlledTerm object.
     *
     * @param uri The URI of the controlled term.
     * @param label The rdfs:label of the controlled term.
     */
    public ControlledTerm(String uri, String label) {
        super(uri, label);
    }

    /**
     * Creates a new ControlledTerm object.
     *
     * @param uri The URI of the controlled term.
     * @param label The rdfs:label of the controlled term.
     * @param equivalentTerm Has this term been used in an owl:sameAs relation?
     */
    public ControlledTerm(String uri, String label, boolean equivalentTerm) {
        super(uri, label, equivalentTerm);
    }

    /**
     * Increment the frequency of this controlled term
     */
    public void increment() {
        frequency++;
    }

    /**
     * Get the frequency of this controlled term
     *
     * @return The controlled term frequency.
     */
    public int getFrequency() {
        return frequency;
    }
}
