package edu.colorado.clear.wsd.feature.resource;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import edu.colorado.clear.wsd.feature.extractor.FeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.IdentityFeatureExtractor;
import edu.colorado.clear.wsd.feature.extractor.string.IdentityStringFunction;
import edu.colorado.clear.wsd.feature.extractor.string.StringFunction;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TSV resource initializer.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public abstract class TsvResourceInitializer<K> implements StringResourceInitializer<MultimapResource<K>> {

    private static final long serialVersionUID = 7969133672311229L;

    @Setter
    protected StringFunction valueFunction = new IdentityStringFunction();
    @Setter
    protected StringFunction keyFunction = new IdentityStringFunction();
    @Setter
    protected FeatureExtractor<K, String> mappingFunction = new IdentityFeatureExtractor<>();

    private final String key;
    private final String path;

    public TsvResourceInitializer(String key, String path) {
        this.key = key;
        this.path = path;
    }

    @Override
    public MultimapResource<K> get() {
        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        MultimapResource<K> resource = new MultimapResource<>(key);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> fields = Arrays.asList(line.split("\t"));
                apply(fields, multimap);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing TSV resource.", e);
        }
        resource.multimap(ImmutableListMultimap.copyOf(multimap));
        resource.mappingFunction(mappingFunction);
        return resource;
    }

    protected abstract void apply(List<String> fields, Multimap<String, String> multimap);

}
