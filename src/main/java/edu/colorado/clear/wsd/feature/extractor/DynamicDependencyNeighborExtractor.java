package edu.colorado.clear.wsd.feature.extractor;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.feature.util.LuceneWrapper;
import edu.colorado.clear.wsd.feature.util.PosUtils;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.FeatureType;
import lombok.Getter;
import lombok.Setter;

/**
 * DDN (Dynamic Dependency Neighbors) extractor based on work described in
 * Dligach, Dmitriy, and Martha Palmer. "Novel semantic features for verb sense disambiguation."
 * Proceedings of the 46th Annual Meeting of the Association for Computational Linguistics on Human Language Technologies:
 * Short Papers. Association for Computational Linguistics, 2008.
 *
 * @author jamesgung
 */
public class DynamicDependencyNeighborExtractor implements FeatureExtractor<DepNode, List<String>> {

    public static final String KEY = "DDN";
    public static final String OBJECT = "object";

    private static final long serialVersionUID = 2473639936326103840L;

    private final Pattern tokenPattern = Pattern.compile("^[a-z]+$");
    @Setter
    private int maxDDNs = 50; // Maximum number of DDN features
    @Setter
    private int maxSearch = 1000; // Maximum number of hits when searching Lucene index
    @Getter
    private LuceneWrapper ddnIndex;

    private Cache<String, List<String>> ddnCache;

    public DynamicDependencyNeighborExtractor(LuceneWrapper ddnIndex) {
        this.ddnIndex = ddnIndex;
        ddnCache = CacheBuilder.newBuilder().build();
    }

    @Override
    public String id() {
        return KEY;
    }

    @Override
    public List<String> extract(DepNode token) {
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
                .limit(maxDDNs)
                .distinct()
                .collect(Collectors.toList());
        ddnCache.put(lemma, ddnFeature);
        return ddnFeature;
    }

}
