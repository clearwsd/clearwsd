package io.github.clearwsd.feature.extractor;

/**
 * Marker interface for extractors that return a single {@link String} for runtime type checking.
 *
 * @author jamesgung
 */
public interface StringExtractor<T> extends FeatureExtractor<T, String> {


}
