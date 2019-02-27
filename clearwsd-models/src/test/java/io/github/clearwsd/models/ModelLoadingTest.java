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

import net.sf.extjwnl.data.Word;

import org.junit.Test;

import edu.mit.jverbnet.data.IVerbClass;
import io.github.clearwsd.DefaultSensePredictor;
import io.github.clearwsd.SensePredictor;
import io.github.clearwsd.corpus.ontonotes.OntoNotesSense;

import static junit.framework.TestCase.assertNotNull;

/**
 * Tests for loading serialized models.
 *
 * @author jamesgung
 */
public class ModelLoadingTest {

    private static final String SEMLINK_PATH = "models/nlp4j-semlink.bin";
    private static final String ONTONOTES_PATH = "models/nlp4j-ontonotes.bin";
    private static final String SEMCOR_PATH = "models/nlp4j-semcor.bin";
    private static final String VERBNET_PATH = "models/nlp4j-verbnet-1.3.bin";

    @Test
    public void testSemlink() {
        SensePredictor<IVerbClass> predictor = DefaultSensePredictor.loadFromResource(SEMLINK_PATH, null);
        assertNotNull(predictor);
    }

    @Test
    public void testOntoNotes() {
        SensePredictor<OntoNotesSense> predictor = DefaultSensePredictor.loadFromResource(ONTONOTES_PATH, null);
        assertNotNull(predictor);
    }

    @Test
    public void testSemcor() {
        SensePredictor<Word> predictor = DefaultSensePredictor.loadFromResource(SEMCOR_PATH, null);
        assertNotNull(predictor);
    }

    @Test
    public void testVerbNet() {
        SensePredictor<IVerbClass> predictor = DefaultSensePredictor.loadFromResource(VERBNET_PATH, null);
        assertNotNull(predictor);
    }

}
