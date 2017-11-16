package edu.colorodo.clear.wsd.feature.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import edu.colorodo.clear.wsd.feature.resource.MultimapResource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Brown cluster resource initializer used to initialize a {@link MultimapResource} from an {@link InputStream}.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class BrownClusterResourceInitializer<K> implements BiConsumer<MultimapResource<K>, InputStream> {

    private List<Integer> subSequences = Arrays.asList(4, 6, 10, 20);
    private int threshold = 1;

    @Override
    public void accept(MultimapResource<K> resource, InputStream inputStream) {
        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> fields = Arrays.asList(line.split("\t"));
                int count = Integer.parseInt(fields.get(2));
                if (count >= threshold) {
                    for (int sub : subSequences) {
                        String key = resource.keyFunction().apply(fields.get(1));
                        String value = resource.valueFunction().apply(fields.get(0));
                        String result = value.substring(0, Math.min(sub, value.length()));
                        multimap.put(key, result);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Brown clusters resource.", e);
        }
        resource.multimap(ImmutableListMultimap.copyOf(multimap));
    }

}
