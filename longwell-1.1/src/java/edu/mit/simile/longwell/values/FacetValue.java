package edu.mit.simile.longwell.values;

import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;


/**
 * This class represents a single facet value, with optional narrower terms.
 *
 * @author Mark H. Butler
 */
public class FacetValue extends ControlledTerm {
    HashMap narrowerTerms;

    protected Logger logger = Logger.getLogger(FacetValue.class);

    /**
     * Creates a new FacetValue object.
     *
     * @param termURI The URI for the term.
     * @param termLabel The label for the term.
     */
    public FacetValue(String termURI, String termLabel) {
        super(termURI, termLabel);
        narrowerTerms = null;
    }

    /**
     * Creates a new FacetValue object.
     *
     * @param termURI The URI for the term.
     * @param termLabel The label for the term.
     * @param equivalentTerm Is this term subject to any equivalence relationships?
     */
    public FacetValue(String termURI, String termLabel, boolean equivalentTerm) {
        super(termURI, termLabel, equivalentTerm);
        narrowerTerms = null;
    }

    /**
     * Creates a new FacetValue object.
     *
     * @param termURI The URI for the term.
     * @param termLabel The label for the term.
     * @param narrowerTermURI The URI for the narrower term
     * @param narrowerTermLabel The label for the narrower term
     */
    public FacetValue(String termURI, String termLabel, String narrowerTermURI,
        String narrowerTermLabel) {
        super(termURI, termLabel);
        frequency = 0;
        narrowerTerms = new HashMap();
        narrowerTerms.put(narrowerTermURI, new ControlledTerm(narrowerTermURI, narrowerTermLabel));
    }

    public boolean contains(String narrowerTermURI) {
        if (narrowerTerms == null) {
            return false;
        } else {
            return narrowerTerms.containsKey(narrowerTermURI);
        }
    }

    public void narrowerTermIncrement(String narrowerTermURI) {
        ControlledTerm term = (ControlledTerm) narrowerTerms.get(narrowerTermURI);
        term.increment();
    }

    /**
     * Add a narrower term to this term.
     *
     * @param narrowerTermURI The URI of the narrower term.
     * @param model The current FacetModel.
     */
    public void add(String narrowerTermURI, String label) {
        if (narrowerTerms == null) {
            narrowerTerms = new HashMap();
            narrowerTerms.put(narrowerTermURI, new ControlledTerm(narrowerTermURI, label));
        } else {
            narrowerTerms.put(narrowerTermURI, new ControlledTerm(narrowerTermURI, label));
        }
    }

    /**
     * Does this term contain a specific narrowerTerm?
     *
     * @param narrowerTermURI The URI of a narrower term
     *
     * @return
     */
    public boolean containsKey(String narrowerTermURI) {
        if (narrowerTerms == null) {
            return false;
        } else {
            return narrowerTerms.containsKey(narrowerTermURI);
        }
    }

    /**
     * Get the narrower terms associated with this term.
     *
     * @return
     */
    public Collection getNarrowerTerms() {
        if (narrowerTerms == null) {
            return null;
        } else {
            return narrowerTerms.values();
        }
    }

    /**
     * Are there any narrower terms associated with this term?
     *
     * @return
     */
    public boolean getHasNarrowerTerms() {
        return (narrowerTerms != null);
    }

    /**
     * How many narrower terms are associated with this term?
     *
     * @return
     */
    public int getNumberNarrowerTerms() {
        if (narrowerTerms != null) {
            return narrowerTerms.size();
        } else {
            return 0;
        }
    }

    /**
     * Get the broader term
     *
     * @return
     */
    public ControlledTerm getBroaderTerm() {
        return this;
    }
}
