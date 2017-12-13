package edu.colorado.clear.wsd.feature.extractor;

import java.util.List;

/**
 * Marker interface for extractors that return a list of strings for runtime type checking.
 *
 * @author jamesgung
 */
public interface StringListExtractor<T> extends FeatureExtractor<T, List<String>> {


}
