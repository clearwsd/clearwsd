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

package io.github.clearwsd.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Lemma dictionary. Maps NLP instances onto the corresponding predicate lemma. Necessary, as lemmas at the sense level
 * are not always consistent w/ lemmas output by a lemmatizer.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
public class LemmaDictionary implements Function<NlpInstance, String>, Serializable {

    private static final long serialVersionUID = 126144127217207499L;

    private Map<LemmaKey, String> mappings = new HashMap<>();
    private boolean train = false;

    @Override
    public String apply(NlpInstance instance) {
        String lemma = instance.feature(FeatureType.Lemma).toString().toLowerCase();
        LemmaKey key = new LemmaKey(lemma, instance.feature(FeatureType.Pos));
        if (train) {
            String predicate = instance.feature(FeatureType.Predicate);
            if (!mappings.containsKey(key)) {
                mappings.put(key, predicate);
            }
            return predicate;
        }
        return mappings.getOrDefault(key, lemma);
    }

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    private static final class LemmaKey implements Serializable {

        private static final long serialVersionUID = -8067578806656259547L;

        private String form;
        private String pos;

    }

}
