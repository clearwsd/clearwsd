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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.utils.ExtJwnlWordNet;
import io.github.clearwsd.utils.WordNetFacade;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


/**
 * WordNet feature resource.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class WordNetResource<K extends NlpInstance> implements FeatureResource<K, List<String>> {

    public static final String WN_KEY = "WN";

    @Getter
    private String key = WN_KEY;
    @Getter
    private WordNetFacade wordNet;

    public WordNetResource(WordNetFacade wordNet) {
        this.wordNet = wordNet;
    }

    public WordNetResource() {
        this(new ExtJwnlWordNet());
    }

    @Override
    public List<String> lookup(K key) {
        return new ArrayList<>(hypernyms(key.feature(FeatureType.Lemma), key.feature(FeatureType.Pos)));
    }

    private Set<String> hypernyms(String lemma, String pos) {
        Set<String> words = new HashSet<>();
        words.addAll(wordNet.hypernyms(lemma, pos));
        words.addAll(wordNet.synonyms(lemma, pos));
        return words;
    }

    public static class WordNetInitializer<K extends NlpInstance> implements Supplier<WordNetResource<K>>, Serializable {

        private static final long serialVersionUID = -1210563042105427915L;

        @Override
        public WordNetResource<K> get() {
            return new WordNetResource<>();
        }
    }

}
