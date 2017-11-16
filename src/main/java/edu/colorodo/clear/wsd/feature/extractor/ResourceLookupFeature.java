package edu.colorodo.clear.wsd.feature.extractor;

import java.util.List;

import edu.colorodo.clear.wsd.feature.resource.FeatureResource;
import edu.colorodo.clear.wsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Feature extractor that looks up the values corresponding to an associated key or keys.
 * Less memory intensive than using {@link edu.colorodo.clear.wsd.feature.annotator.Annotator} as results are not added to
 * instances, but lookups must be performed separately over each context, instead of once.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class ResourceLookupFeature<T extends NlpInstance> extends NlpFeatureExtractor<T, List<String>> {

    private FeatureResource<T, List<String>> featureResource;

    @Override
    public List<String> extract(T instance) {
        return featureResource.lookup(instance);
    }

}
