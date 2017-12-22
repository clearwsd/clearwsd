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

package io.github.clearwsd;

import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.utils.SenseInventory;
import lombok.AllArgsConstructor;

/**
 * Parser wrapper that applies word sense annotations via a {@link WordSenseAnnotator} to inputs following parsing.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DefaultSensePredictor implements NlpParser, SensePredictor<String> {

    private WordSenseAnnotator annotator;
    private NlpParser dependencyParser;

    @Override
    public DepTree parse(List<String> tokens) {
        return annotator.annotate(dependencyParser.parse(tokens));
    }

    @Override
    public List<String> segment(String input) {
        return dependencyParser.segment(input);
    }

    @Override
    public List<String> tokenize(String sentence) {
        return dependencyParser.tokenize(sentence);
    }

    @Override
    public List<String> predict(List<String> sentence) {
        DepTree depTree = parse(sentence);
        return depTree.tokens().stream()
                .map(token -> token.feature(FeatureType.Sense))
                .map(sense -> sense == null ? SenseInventory.DEFAULT_SENSE : (String) sense)
                .collect(Collectors.toList());
    }

    /**
     * Initialize a {@link DefaultSensePredictor} from a classpath resource and parser.
     *
     * @param resource classpath resource
     * @return initialized sense predictor
     */
    public static DefaultSensePredictor loadFromResource(String resource, NlpParser parser) {
        return new DefaultSensePredictor(WordSenseAnnotator.loadFromResource(resource), parser);
    }

}
