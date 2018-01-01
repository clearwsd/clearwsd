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

package io.github.clearwsd.parser;

import org.junit.Test;

import java.util.List;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link Nlp4jDependencyParser} unit tests.
 *
 * @author jamesgung
 */
public class Nlp4jDependencyParserTest {

    @Test
    public void parse() {
        Nlp4jDependencyParser parser = new Nlp4jDependencyParser();
        List<String> tokenized = parser.tokenize("My dog has fleas.");
        assertEquals(5, tokenized.size());
        DepTree parse = parser.parse(tokenized);
        DepNode root = parse.root();
        assertNotNull(root);
        assertEquals(2, root.index());
        assertEquals("have", root.feature(FeatureType.Lemma));
        assertEquals(3, root.children().size());
    }

}