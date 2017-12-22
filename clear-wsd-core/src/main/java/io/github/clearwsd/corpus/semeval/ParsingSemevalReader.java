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

package io.github.clearwsd.corpus.semeval;

import java.util.stream.Collectors;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.DefaultDepTree;

/**
 * Semeval XML reader.
 *
 * @author jamesgung
 */
public class ParsingSemevalReader extends SemevalReader {

    private NlpParser parser;

    public ParsingSemevalReader(String keyPath, NlpParser parser) {
        super(keyPath);
        this.parser = parser;
    }

    @Override
    protected DepTree processSentence(DefaultDepTree dependencyTree) {
        DefaultDepTree result = (DefaultDepTree) parser.parse(dependencyTree.tokens().stream().
                map(t -> (String) t.feature(FeatureType.Text))
                .collect(Collectors.toList()));
        result.addFeature(FeatureType.Id, dependencyTree.feature(FeatureType.Id));
        result.index(dependencyTree.index());
        return result;
    }

}
