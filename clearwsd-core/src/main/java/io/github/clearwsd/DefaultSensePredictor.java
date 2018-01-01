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

/**
 * Parser wrapper that applies word sense annotations via a {@link WordSenseAnnotator} to inputs following parsing.
 *
 * @author jamesgung
 */
public class DefaultSensePredictor<T> extends BaseSensePredictor<T> {

    public DefaultSensePredictor(WordSenseAnnotator annotator, NlpParser dependencyParser) {
        super(annotator, dependencyParser);
    }

    @Override
    public List<SensePrediction<T>> predict(List<String> sentence) {
        DepTree depTree = parse(sentence);
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
     * Initialize a {@link DefaultSensePredictor} from a classpath resource and parser.
     *
     * @param resource classpath resource
     * @return initialized sense predictor
     */
    public static <T> DefaultSensePredictor<T> loadFromResource(String resource, NlpParser parser) {
        return new DefaultSensePredictor<>(WordSenseAnnotator.loadFromResource(resource), parser);
    }

}
