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

package io.github.clearwsd.verbnet;

import io.github.clearwsd.feature.annotator.Annotator;
import io.github.clearwsd.feature.util.DepUtils;
import io.github.clearwsd.feature.util.PosUtils;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.utils.LemmaDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static io.github.clearwsd.type.FeatureType.Dep;
import static io.github.clearwsd.type.FeatureType.Lemma;
import static io.github.clearwsd.type.FeatureType.Pos;
import static io.github.clearwsd.type.FeatureType.Predicate;

/**
 * Default predicate annotator implementation. Uses heuristics based on POS-tags and dependency labels to determine whether or not a
 * verb is predicative.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DefaultPredicateAnnotator implements Annotator<DepTree> {

    private static final long serialVersionUID = -2953300591005876159L;

    private static final String HAVE = "have";

    @Getter
    private final LemmaDictionary dictionary;

    @Override
    public DepTree annotate(DepTree instance) {
        int index = -1;
        for (DepNode token : instance) {
            ++index;
            // skip "have to x", since some dependency parsers, like NLP4J, to not mark as an auxiliary
            if (index + 1 < instance.size()
                    && token.feature(Lemma).equals(HAVE)
                    && instance.get(index + 1).feature(Lemma).equals("to")) {
                continue;
            }
            if (PosUtils.isVerb(token.feature(Pos)) && !DepUtils.isAux(token.feature(Dep))) {
                token.addFeature(Predicate, dictionary.apply(token));
            }
        }
        return instance;
    }

    @Override
    public boolean initialized() {
        return true;
    }

}
