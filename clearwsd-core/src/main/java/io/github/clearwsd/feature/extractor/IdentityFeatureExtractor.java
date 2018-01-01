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

package io.github.clearwsd.feature.extractor;

/**
 * Identity string feature extractor.
 *
 * @author jamesgung
 */
public class IdentityFeatureExtractor<K> implements StringExtractor<K> {

    public static final String ID = "ID";

    private static final long serialVersionUID = 2852870228451742787L;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String extract(K instance) {
        return instance.toString();
    }

}
