package edu.colorodo.clear.wsd.feature.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Feature vocabulary used to convert features to one-hot representations.
 *
 * @author jamesgung
 */
public interface Vocabulary extends Serializable {

    /**
     * Return a map of features to corresponding indices.
     */
    Map<String, Integer> indices();

    /**
     * Return an index for a given feature, or a default value if it is not found.
     *
     * @param value input feature
     * @return corresponding index
     */
    int index(String value);

    /**
     * Return a feature for a given index, or null if none is found.
     *
     * @param index input index
     * @return feature
     */
    String value(int index);

}
