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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import io.github.clearwsd.feature.extractor.FeatureExtractor;
import io.github.clearwsd.feature.extractor.IdentityFeatureExtractor;
import io.github.clearwsd.feature.extractor.string.IdentityStringFunction;
import io.github.clearwsd.feature.extractor.string.StringFunction;
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
    private byte[] data;

    TsvResourceInitializer(String key, URL path) {
        this.key = key;
        try {
            this.data = ByteStreams.toByteArray(path.openStream());
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + e.getMessage(), e);
        }
    }

    @Override
    public MultimapResource<K> get() {
        ListMultimap<String, String> multimap = ArrayListMultimap.create();
        MultimapResource<K> resource = new MultimapResource<>(key);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.data)))) {
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
