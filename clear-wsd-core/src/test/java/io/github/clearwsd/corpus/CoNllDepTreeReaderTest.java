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

package io.github.clearwsd.corpus;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;

import static org.junit.Assert.assertEquals;

/**
 * {@link CoNllDepTreeReader} tests.
 *
 * @author jamesgung
 */
public class CoNllDepTreeReaderTest {

    private static final String TEST_PATH = "src/test/resources/test.dep";

    @Test
    public void testReadInstances() throws IOException {
        List<DepTree> depTrees = new CoNllDepTreeReader().readInstances(new FileInputStream(TEST_PATH));
        assertEquals(2, depTrees.size());
        assertEquals(16, depTrees.get(0).size());
        assertEquals(17, depTrees.get(1).size());
        assertEquals("stop", depTrees.get(0).root().feature(FeatureType.Text));
        assertEquals("calling", depTrees.get(1).root().feature(FeatureType.Text));
    }

}