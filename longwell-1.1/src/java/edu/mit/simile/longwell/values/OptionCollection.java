package edu.mit.simile.longwell.values;

import java.util.HashMap;


public class OptionCollection extends HashMap {
    private static final long serialVersionUID = -1788799247411997139L;

    public ResourceImageCollection getOptions(String key) {
        return (ResourceImageCollection) super.get(key);
    }
}
