package edu.colorado.clear.wsd.feature.resource;

import com.google.common.collect.ImmutableListMultimap;

import java.util.List;

import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.IdentityFeatureExtractor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Multimap-based resource. Can define an arbitrary initializer for loading the multimap.
 * Function applied to keys upon lookup, and function applied to values in multimap are also configurable.
 *
 * @param <K> input key type used during lookup
 * @author jamesgung
 */
@Slf4j
@Setter
@Getter
@Accessors(fluent = true)
public class MultimapResource<K> implements FeatureResource<K, List<String>> {

    private static final long serialVersionUID = 7402042556816462306L;

    private String key;
    private ImmutableListMultimap<String, String> multimap;
    private FeatureExtractor<K, String> mappingFunction = new IdentityFeatureExtractor<>();

    public MultimapResource(String key) {
        this.key = key;
    }

    @Override
    public List<String> lookup(K key) {
        return multimap.get(mappingFunction.extract(key));
    }

}
