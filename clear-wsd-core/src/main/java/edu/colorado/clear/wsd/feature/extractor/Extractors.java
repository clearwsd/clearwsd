package edu.colorado.clear.wsd.feature.extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.extractor.string.LowercaseFunction;

/**
 * Utilities for simple instantiation of commonly-used extractors.
 *
 * @author jamesgung
 */
public class Extractors {

    private Extractors() {
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> lookup(FeatureType featureType) {
        //noinspection unchecked
        return (LookupFeatureExtractor<T>) new LookupFeatureExtractor<>(featureType.name());
    }

    public static <T extends NlpInstance> ListLookupFeatureExtractor<T> listLookup(Collection<String> keys) {
        return new ListLookupFeatureExtractor<>(new ArrayList<>(keys));
    }

    public static <T extends NlpInstance> ListLookupFeatureExtractor<T> listLookup(String... keys) {
        return new ListLookupFeatureExtractor<>(Arrays.asList(keys));
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> form() {
        return lookup(FeatureType.Text);
    }

    public static <T extends NlpInstance> LookupFeatureExtractor<T> lemma() {
        return lookup(FeatureType.Lemma);
    }

    public static <T extends NlpInstance> StringFunctionExtractor<T> lowerForm() {
        return new StringFunctionExtractor<>(form(), new LowercaseFunction());
    }

    public static <T extends NlpInstance> StringFunctionExtractor<T> lowerLemma() {
        return new StringFunctionExtractor<>(lemma(), new LowercaseFunction());
    }

    @SafeVarargs
    public static <T extends NlpInstance> ConcatenatingFeatureExtractor<T> concat(FeatureExtractor<T, String>... extractors) {
        return new ConcatenatingFeatureExtractor<>(extractors);
    }

    public static <T extends NlpInstance> List<StringExtractor<T>> concat(FeatureExtractor<T, String> extractor,
                                                                          List<FeatureExtractor<T, String>> extractors) {
        return extractors.stream().map(e -> concat(e, extractor)).collect(Collectors.toList());
    }

    public static <T extends NlpInstance> ListConcatenatingFeatureExtractor<T> listConcat(StringListExtractor<T> base,
                                                                                          StringExtractor<T> extractors) {
        return new ListConcatenatingFeatureExtractor<>(base, extractors);
    }

    public static <T extends NlpInstance> List<StringListExtractor<T>> listConcat(StringExtractor<T> extractor,
                                                                                  List<StringListExtractor<T>> extractors) {
        return extractors.stream().map(e -> listConcat(e, extractor)).collect(Collectors.toList());
    }

}
