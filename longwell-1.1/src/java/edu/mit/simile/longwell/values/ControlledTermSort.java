package edu.mit.simile.longwell.values;

import java.util.Comparator;

/**
 * A class that sorts controlled terms by frequency
 */
public class ControlledTermSort implements Comparator {
    boolean isFrequencySort = true;

    ControlledTermSort(boolean isFrequencySort) {
        this.isFrequencySort = isFrequencySort;
    }

    /**
     * Compare two controlled terms
     *
     * @param a The first controlled term.
     * @param b The second controlled term.
     * @return Is this term greater, equal of less than the other term.
     */
    public int compare(Object a, Object b) {
        if (!(a instanceof ControlledTerm) || !(b instanceof ControlledTerm)) {
            throw new ClassCastException();
        }

        ControlledTerm c = (ControlledTerm) a;
        ControlledTerm d = (ControlledTerm) b;

        int frequencyResult = isFrequencySort ? new Integer(d.getFrequency()).compareTo(new Integer(c.getFrequency())) : 0;      

        if (frequencyResult == 0) {
            return c.getLabel().toLowerCase().compareTo(d.getLabel().toLowerCase());
        } else {
            return frequencyResult;
        }
    }
}
