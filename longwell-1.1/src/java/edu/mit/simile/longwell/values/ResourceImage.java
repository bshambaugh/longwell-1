package edu.mit.simile.longwell.values;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;

import edu.mit.simile.vocabularies.Display;


/**
 * A class used to record information for replacing a resource with an
 * an image.
 *
 * @author Ryan Lee
 * @see BrowserConfig
 */
public class ResourceImage {
    private String _resourceUri;
    private String _location;
    private int _width;
    private int _height;
    private String _title;

    public ResourceImage(Model m, Resource r) {
        setResourceUri(r.getURI());

        if (m.contains(r, Display.imageSrc)) {
            setLocation(m.getProperty(r, Display.imageSrc).getString());
        }

        if (m.contains(r, Display.imageWidth)) {
            setWidth(m.getProperty(r, Display.imageWidth).getInt());
        }

        if (m.contains(r, Display.imageHeight)) {
            setHeight(m.getProperty(r, Display.imageHeight).getInt());
        }

        if (m.contains(r, DC.title)) {
            setTitle(m.getProperty(r, DC.title).getString());
        }
    }

    public String getResourceUri() {
        return this._resourceUri;
    }

    public String getLocation() {
        return this._location;
    }

    public int getWidth() {
        return this._width;
    }

    public int getHeight() {
        return this._height;
    }

    public boolean getHasTitle() {
        return (null != this._title);
    }

    public String getTitle() {
        if (getHasTitle()) {
            return this._title;
        } else {
            return this._resourceUri;
        }
    }

    public void setResourceUri(String resuri) {
        this._resourceUri = resuri;
    }

    public void setLocation(String location) {
        this._location = location;
    }

    public void setWidth(int width) {
        this._width = width;
    }

    public void setHeight(int height) {
        this._height = height;
    }

    public void setTitle(String title) {
        this._title = title;
    }
}
