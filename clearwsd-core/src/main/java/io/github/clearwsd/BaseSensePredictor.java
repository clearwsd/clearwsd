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

import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.utils.SenseInventory;
import lombok.AllArgsConstructor;

/**
 * Parser wrapper that applies word sense annotations via a {@link WordSenseAnnotator} to inputs following parsing.
 *
 * @param <T> sense type
 * @author jamesgung
 */
@AllArgsConstructor
public abstract class BaseSensePredictor<T> implements NlpParser, SensePredictor<T> {

    protected WordSenseAnnotator annotator;
    protected NlpParser dependencyParser;

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

    public List<SensePrediction<T>> predict(DepTree depTree) {
        List<SensePrediction<T>> predictions = new ArrayList<>();
        for (DepNode token : depTree) {
            String sense = token.feature(FeatureType.Sense);
            if (sense != null) {
                predictions.add(new DefaultSensePrediction<>(
                        token.index(),
                        token.feature(FeatureType.Text),
                        sense, senseInventory().getSense(sense)));
            }
        }
        return predictions;
    }

    /**
     * Return the {@link SenseInventory} associated with this sense predictor.
     */
    public SenseInventory<T> senseInventory() {
        //noinspection unchecked
        return annotator.senseInventory();
    }

}
