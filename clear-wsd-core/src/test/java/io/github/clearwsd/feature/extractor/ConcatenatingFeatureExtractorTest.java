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

import org.junit.Test;

import java.util.Arrays;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.feature.util.FeatureUtils;
import io.github.clearwsd.type.DefaultDepNode;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class ConcatenatingFeatureExtractorTest {

    @Test
    public void testConcatenate() {
        ConcatenatingFeatureExtractor<DepNode> extractor = new ConcatenatingFeatureExtractor<>(Arrays.asList(
                new LookupFeatureExtractor<>(FeatureType.Pos.name()),
                new LookupFeatureExtractor<>(FeatureType.Dep.name())));
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature(FeatureType.Pos, "NN");
        depNode.addFeature(FeatureType.Dep, "nsubj");
        assertEquals(String.format("NN%snsubj", FeatureUtils.CONCAT_DELIM), extractor.extract(depNode));
    }

}