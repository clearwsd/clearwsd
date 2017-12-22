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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import io.github.clearwsd.corpus.OntoNotesConverter.TreebankTreeNode;

import static org.junit.Assert.assertEquals;

/**
 * {@link OntoNotesConverter} tests.
 *
 * @author jamesgung
 */
public class OntoNotesConverterTest {

    private static final String PTB_TEST = "src/test/resources/ptb_test.txt";
    private static final String TOKENIZED = "src/test/resources/ptb_test.tokenized.txt";

    @Test
    public void testParse() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get(PTB_TEST)));
        String expected = new String(Files.readAllBytes(Paths.get(TOKENIZED)));
        String result = OntoNotesConverter.parse(input).stream().map(tree -> tree.allChildren().stream()
                .filter(TreebankTreeNode::isLeaf)
                .map(TreebankTreeNode::value)
                .collect(Collectors.joining(" ")))
                .collect(Collectors.joining("\n"));
        assertEquals(expected, result);
    }

}