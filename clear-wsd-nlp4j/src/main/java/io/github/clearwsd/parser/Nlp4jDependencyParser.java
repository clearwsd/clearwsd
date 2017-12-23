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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.emory.mathcs.nlp.common.util.IOUtils;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;
import edu.emory.mathcs.nlp.component.tokenizer.Tokenizer;
import edu.emory.mathcs.nlp.component.tokenizer.token.Token;
import edu.emory.mathcs.nlp.decode.NLPDecoder;
import io.github.clearwsd.type.DefaultDepNode;
import io.github.clearwsd.type.DefaultDepTree;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;

/**
 * Dependency parser implementation wrapping <a href="https://emorynlp.github.io/nlp4j/">NLP4J</a>. Applies part-of-speech
 * annotation and performs lemmatization during parsing, adding {@link FeatureType#Pos} and {@link FeatureType#Lemma} features.
 *
 * @author jamesgung
 */
public class Nlp4jDependencyParser implements NlpParser {

    private static final String DEFAULT_CONFIG = "io/github/clearwsd/parser/nlp4j/default-config.xml";

    private NLPDecoder nlp4j;
    private Tokenizer tokenizer;

    /**
     * Initialize from input path, either on classpath or file system.
     *
     * @param inputPath input path
     */
    public Nlp4jDependencyParser(String inputPath) {
        nlp4j = new NLPDecoder(IOUtils.getInputStream(inputPath));
        tokenizer = nlp4j.getTokenizer();
    }

    /**
     * Initialize from default config.
     */
    public Nlp4jDependencyParser() {
        this(DEFAULT_CONFIG);
    }

    @Override
    public DepTree parse(List<String> tokens) {
        NLPNode[] tree = nlp4j.decode(nlp4j.toNodeArray(tokens.stream().map(Token::new).collect(Collectors.toList())));
        List<DepNode> depNodes = new ArrayList<>();
        Map<Integer, Integer> headMap = new HashMap<>();
        for (int i = 1; i < tree.length; ++i) {
            NLPNode node = tree[i];
            DefaultDepNode depNode = new DefaultDepNode(i - 1);
            depNode.addFeature(FeatureType.Text, node.getWordForm());
            depNode.addFeature(FeatureType.Pos, node.getPartOfSpeechTag());
            depNode.addFeature(FeatureType.Dep, node.getDependencyLabel());
            depNode.addFeature(FeatureType.Lemma, node.getLemma());
            headMap.put(i - 1, node.getDependencyHead().getID() - 1);
            depNodes.add(depNode);
        }
        DepNode root = null;
        for (Map.Entry<Integer, Integer> entry : headMap.entrySet()) {
            DepNode depNode = depNodes.get(entry.getKey());
            if (entry.getValue() < 0) {
                root = depNode;
            } else {
                DepNode head = depNodes.get(entry.getValue());
                ((DefaultDepNode) depNode).head(head);
            }
        }
        return new DefaultDepTree(0, depNodes, root);
    }

    @Override
    public List<String> segment(String input) {
        return tokenizer.segmentize(input).stream()
                .map(sentence -> sentence.stream()
                        .map(Token::getWordForm)
                        .collect(Collectors.joining(" ")))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> tokenize(String sentence) {
        return tokenizer.tokenize(sentence).stream()
                .map(Token::getWordForm)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        Nlp4jDependencyParser parser = new Nlp4jDependencyParser();
        List<String> tokens = parser.tokenize("This is a test");
        DepTree parse = parser.parse(tokens);
        System.out.println(parse.toString());
    }

}
