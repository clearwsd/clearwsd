package edu.colorado.clear.wsd.feature.resource;

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

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
public class BrownClusterResourceInitializer<K> extends TsvResourceInitializer<K> {

    private static final long serialVersionUID = -1475047308109219325L;

    private List<Integer> subSequences = Arrays.asList(4, 6, 10, 20);
    private int threshold = 1;

    public BrownClusterResourceInitializer(String key, String path) {
        super(key, path);
    }

    @Override
    protected void apply(List<String> fields, Multimap<String, String> multimap) {
        int count = Integer.parseInt(fields.get(2));
        if (count >= threshold) {
            for (int sub : subSequences) {
                String key = keyFunction.apply(fields.get(1));
                String value = valueFunction.apply(fields.get(0));
                String result = value.substring(0, Math.min(sub, value.length()));
                multimap.put(key, result);
            }
        }
    }


}
