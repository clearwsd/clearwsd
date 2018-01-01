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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link StanfordDependencyParser} test.
 *
 * @author jamesgung
 */
public class StanfordDependencyParserTest {

    private static NlpParser parser;

    private List<String> tokens;
    private DepTree parse;

    @BeforeClass
    public static void initialize() {
        parser = new StanfordDependencyParser();
    }

    @Before
    public void parse() {
        tokens = parser.tokenize("My dog has fleas.");
        parse = parser.parse(tokens);
    }

    @Test
    public void testText() {
        Iterator<String> tokenIterator = tokens.iterator();
        for (DepNode depNode : parse) {
            assertEquals(tokenIterator.next(), depNode.feature(FeatureType.Text));
        }
    }

    @Test
    public void testPos() {
        for (DepNode depNode : parse) {
            assertNotNull(depNode.feature(FeatureType.Pos));
        }
    }

    @Test
    public void testLemmas() {
        for (DepNode depNode : parse) {
            assertNotNull(depNode.feature(FeatureType.Lemma));
        }
    }

    @Test
    public void testHeadLabels() {
        for (DepNode depNode : parse) {
            assertNotNull(depNode.feature(FeatureType.Dep));
        }
    }

    @Test
    public void testHeads() {
        assertEquals(1, parse.get(0).head().index());
        assertEquals(2, parse.get(1).head().index());
        assertNull(parse.get(2).head());
        assertEquals(2, parse.get(3).head().index());
        assertEquals(2, parse.get(4).head().index());
    }

    @Test
    public void testRoot() {
        assertTrue(parse.get(2).isRoot());
    }

    @Test
    public void testChildren() {
        assertEquals(0, parse.get(0).children().size());
        assertEquals(1, parse.get(1).children().size());
        assertEquals(3, parse.get(2).children().size());
        assertEquals(0, parse.get(3).children().size());
        assertEquals(0, parse.get(4).children().size());
    }

}