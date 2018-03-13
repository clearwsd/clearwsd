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
import java.util.List;

import lombok.experimental.Accessors;

/**
 * TSV resource initializer used to initialize a {@link MultimapResource} from an {@link InputStream}.
 *
 * @author jamesgung
 */
@Accessors(fluent = true)
public class DefaultTsvResourceInitializer<K> extends TsvResourceInitializer<K> {

    private static final long serialVersionUID = -1044802169525334439L;

    public DefaultTsvResourceInitializer(String key, URL path) {
        super(key, path);
    }

    @Override
    protected void apply(List<String> fields, Multimap<String, String> multimap) {
        String key = keyFunction.apply(fields.get(0));
        fields.subList(1, fields.size()).stream()
                .map(s -> valueFunction.apply(s))
                .forEach(s -> multimap.put(key, s));
    }

}
