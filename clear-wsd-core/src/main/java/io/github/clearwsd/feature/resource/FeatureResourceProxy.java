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

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Feature resource proxy.
 *
 * @author jamesgung
 */
public class FeatureResourceProxy<K, T> implements FeatureResource<K, T>, Serializable {

    private static final long serialVersionUID = 2637440121117573645L;

    private String key;
    private Supplier<FeatureResource<K, T>> initializer;
    private transient FeatureResource<K, T> resource;

    public FeatureResourceProxy(String key, Supplier<FeatureResource<K, T>> initializer) {
        this.key = key;
        this.initializer = initializer;
    }

    public void initialize() {
        if (resource == null) {
            resource = initializer.get();
        }
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public T lookup(K key) {
        initialize();
        return resource.lookup(key);
    }

}
