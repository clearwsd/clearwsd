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
import java.util.Collections;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.DefaultDepNode;

import static junit.framework.TestCase.assertEquals;

/**
 * @author jamesgung
 */
public class LookupFeatureExtractorTest {

    @Test
    public void testLookup() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(FeatureType.Text.name());
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature(FeatureType.Text, "cat");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat);
    }

    @Test
    public void testFallback() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(
                Collections.singletonList(FeatureType.Text.name()), new LookupFeatureExtractor<DepNode>(FeatureType.Lemma.name()));
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature(FeatureType.Lemma, "cat");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat);
    }

    @Test
    public void testMultiple() {
        LookupFeatureExtractor<DepNode> lookupExtractor = new LookupFeatureExtractor<>(
                Arrays.asList(FeatureType.Text.name(), FeatureType.Lemma.name(), FeatureType.Pos.name()));
        DefaultDepNode depNode = new DefaultDepNode(0);
        depNode.addFeature(FeatureType.Lemma, "cat");
        depNode.addFeature(FeatureType.Pos, "NN");
        String feat = lookupExtractor.extract(depNode);
        assertEquals("cat", feat); // extract the value for the first key present
    }

}