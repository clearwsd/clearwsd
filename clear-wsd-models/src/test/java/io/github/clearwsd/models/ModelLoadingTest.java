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

package io.github.clearwsd.models;

import org.junit.Test;

import io.github.clearwsd.WordSenseClassifier;

import static junit.framework.TestCase.assertNotNull;

/**
 * Tests for loading serialized models.
 *
 * @author jamesgung
 */
public class ModelLoadingTest {

    private static final String SEMLINK_PATH = "models/semlink.bin";
    private static final String ONTONOTES_PATH = "models/ontonotes.bin";
    private static final String SEMCOR_PATH = "models/semcor.bin";

    @Test
    public void testSemlink() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(SEMLINK_PATH));
        assertNotNull(classifier);
    }

    @Test
    public void testOntoNotes() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(ONTONOTES_PATH));
        assertNotNull(classifier);
    }

    @Test
    public void testSemcor() {
        WordSenseClassifier classifier = WordSenseClassifier.load(this.getClass().getClassLoader().getResource(SEMCOR_PATH));
        assertNotNull(classifier);
    }

}
