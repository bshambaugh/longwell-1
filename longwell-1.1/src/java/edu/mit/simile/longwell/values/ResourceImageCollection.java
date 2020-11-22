package edu.mit.simile.longwell.values;

import java.util.HashMap;


public class ResourceImageCollection extends HashMap {
    private static final long serialVersionUID = -1943112788280339962L;

    public ResourceImage getResourceImage(String key) {
        return (ResourceImage) super.get(key);
    }
}
