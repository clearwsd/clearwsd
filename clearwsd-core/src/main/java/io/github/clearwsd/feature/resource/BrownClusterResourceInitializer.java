/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.feature.resource;

import com.google.common.collect.Multimap;

import java.io.InputStream;
import java.net.URL;
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

    public static final String BWC_KEY = "BWC";

    private static final long serialVersionUID = -1475047308109219325L;

    private List<Integer> subSequences = Arrays.asList(4, 6, 10, 20);
    private int threshold = 1;

    public BrownClusterResourceInitializer(String key, URL path) {
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
