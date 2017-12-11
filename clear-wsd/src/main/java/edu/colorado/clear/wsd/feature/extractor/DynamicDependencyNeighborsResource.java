package edu.colorado.clear.wsd.feature.extractor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.resource.FeatureResource;
import edu.colorado.clear.wsd.feature.util.LuceneWrapper;
import edu.colorado.clear.wsd.feature.util.PosUtils;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.FeatureType;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * DDN (Dynamic Dependency Neighbors) extractor based on work described in
 * Dligach, Dmitriy, and Martha Palmer. "Novel semantic features for verb sense disambiguation."
 * Proceedings of the 46th Annual Meeting of the Association for Computational Linguistics on Human Language Technologies:
 * Short Papers. Association for Computational Linguistics, 2008.
 *
 * @author jamesgung
 */
public class DynamicDependencyNeighborsResource implements FeatureResource<DepNode, List<String>> {

    public static final String KEY = "DDN";
    public static final String OBJECT = "object";

    private final LuceneWrapper ddnIndex;
    private final int maxNeighbors; // Maximum number of DDN features
    private final int maxSearch; // Maximum number of hits when searching Lucene index
    private final Pattern tokenPattern = Pattern.compile("^[a-z]+$");

    private Cache<String, List<String>> ddnCache;

    public DynamicDependencyNeighborsResource(LuceneWrapper ddnIndex, int maxNeighbors, int maxSearch) {
        this.ddnIndex = ddnIndex;
        this.maxNeighbors = maxNeighbors;
        this.maxSearch = maxSearch;
        ddnCache = CacheBuilder.newBuilder().build();
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public List<String> lookup(DepNode token) {
        if (!PosUtils.isNoun(token.feature(FeatureType.Pos)) || !tokenPattern.matcher(
                ((String) token.feature(FeatureType.Text)).toLowerCase()).matches()) {
            return new ArrayList<>();
        }
        String lemma = token.feature(FeatureType.Lemma);
        List<String> ddnFeature = ddnCache.getIfPresent(lemma);
        if (ddnFeature != null) {
            return ddnFeature;
        }
        ddnFeature = ddnIndex.search(lemma, OBJECT, maxSearch)
                .entrySet().stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(Map.Entry::getKey)
                .limit(maxNeighbors)
                .distinct()
                .collect(Collectors.toList());
        ddnCache.put(lemma, ddnFeature);
        return ddnFeature;
    }

    @Accessors(fluent = true)
    public static class DdnResourceInitializer implements Supplier<DynamicDependencyNeighborsResource>, Serializable {

        private static final long serialVersionUID = -5135704509811250797L;

        @Setter
        private int maxNeighbors = 50;
        @Setter
        private int maxSearch = 1000;

        private File indexDirectory;

        public DdnResourceInitializer(File indexDirectory) {
            this.indexDirectory = indexDirectory;
        }

        @Override
        public DynamicDependencyNeighborsResource get() {
            return new DynamicDependencyNeighborsResource(new LuceneWrapper(indexDirectory), maxNeighbors, maxSearch);
        }
    }

}
