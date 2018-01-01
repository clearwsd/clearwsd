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

package io.github.clearwsd.eval;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author jamesgung
 */
public class EvaluationTest {

    private static final double DELTA = 0.00001;

    @Test
    public void precision() {
        Evaluation evaluation = new Evaluation();
        evaluation.add("cat", "dog");
        evaluation.add("cat", "cat");
        evaluation.add("dog", "dog");
        assertEquals(2d / 3, evaluation.precision(), DELTA);
        assertEquals(1d / 2, evaluation.precision("cat"), DELTA);
        assertEquals(1d, evaluation.precision("dog"), DELTA);
    }

    @Test
    public void recall() {
        Evaluation evaluation = new Evaluation();
        evaluation.add("cat", "dog");
        evaluation.add("cat", "cat");
        evaluation.add("dog", "dog");
        assertEquals(2d / 3, evaluation.recall(), DELTA);
        assertEquals(1d, evaluation.recall("cat"), DELTA);
        assertEquals(1d / 2, evaluation.recall("dog"), DELTA);
    }

    @Test
    public void f1() {
        Evaluation evaluation = new Evaluation();
        evaluation.add("cat", "dog");
        evaluation.add("cat", "cat");
        evaluation.add("dog", "dog");
        assertEquals(2d / 3, evaluation.f1(), DELTA);
        assertEquals(2d / 3, evaluation.f1("cat"), DELTA);
        assertEquals(2d / 3, evaluation.f1("dog"), DELTA);
    }

}