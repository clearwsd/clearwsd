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

package io.github.clearwsd.corpus.semlink;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.clearwsd.corpus.CorpusReader;
import io.github.clearwsd.corpus.semlink.VerbNetReader.VerbNetInstanceParser;
import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.parser.NlpTokenizer;
import io.github.clearwsd.parser.WhitespaceTokenizer;
import io.github.clearwsd.type.DefaultNlpFocus;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.type.FeatureType.Gold;
import static io.github.clearwsd.type.FeatureType.Lemma;
import static io.github.clearwsd.type.FeatureType.Metadata;
import static io.github.clearwsd.type.FeatureType.Predicate;
import static io.github.clearwsd.type.FeatureType.Text;

/**
 * Corpus reader that reads and parses SemLink-style annotations.
 *
 * @author jamesgung
 */
@Slf4j
public class ParsingSemlinkReader implements CorpusReader<NlpFocus<DepNode, DepTree>> {

    private NlpParser dependencyParser;
    private NlpTokenizer tokenizer;

    @Setter
    private boolean writeSemlink = false;
    @Setter
    private boolean cacheTrees = true;

    public ParsingSemlinkReader(NlpParser dependencyParser, NlpTokenizer tokenizer) {
        this.dependencyParser = dependencyParser;
        this.tokenizer = tokenizer;
    }

    /**
     * Initialize a {@link ParsingSemlinkReader} with a {@link WhitespaceTokenizer}. Assumes instances are pre-tokenized.
     *
     * @param dependencyParser dependency parser
     */
    public ParsingSemlinkReader(NlpParser dependencyParser) {
        this(dependencyParser, new WhitespaceTokenizer());
    }

    @Override
    public List<NlpFocus<DepNode, DepTree>> readInstances(InputStream inputStream) {
        Map<String, DepTree> parseCache = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            VerbNetInstanceParser parser = new VerbNetInstanceParser();
            List<NlpFocus<DepNode, DepTree>> results = new ArrayList<>();
            int index = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                VerbNetReader.VerbNetInstance instance = parser.parse(line);

                DepTree depTree;
                if (cacheTrees) {
                    depTree = parseCache.computeIfAbsent(instance.originalText(),
                            k -> dependencyParser.parse(tokenizer.tokenize(instance.originalText())));
                } else {
                    depTree = dependencyParser.parse(tokenizer.tokenize(instance.originalText()));
                }

                DepNode focus = depTree.get(instance.token());
                if (!instance.lemma().equalsIgnoreCase(focus.feature(Lemma))) {
                    log.warn("Lemma mismatch ({} vs. {}) between annotation and parser output for instance: {}",
                            instance.lemma(), focus.feature(Lemma), line);
                }
                focus.addFeature(Gold, instance.label());
                focus.addFeature(Predicate, instance.lemma());

                NlpFocus<DepNode, DepTree> focusInstance = new DefaultNlpFocus<>(index++, focus, depTree);
                focusInstance.addFeature(Gold, instance.label());
                focusInstance.addFeature(Metadata, line);
                if (index % 1000 == 0) {
                    log.debug("VerbNet parsing progress: {} instances", index);
                }
                results.add(focusInstance);
            }
            log.debug("Read {} instances with {}.", results.size(), this.getClass().getSimpleName());
            return results;
        } catch (IOException e) {
            throw new RuntimeException("Error reading annotations.", e);
        }
    }

    @Override
    public void writeInstances(List<NlpFocus<DepNode, DepTree>> instances, OutputStream outputStream) {
        if (!writeSemlink) {
            new VerbNetReader().writeInstances(instances, outputStream);
        }
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            for (NlpFocus<DepNode, DepTree> instance : instances) {
                String result = instance.feature(Metadata);
                if (result == null) {
                    result = String.format("%d %d %d %s %s\t%s",
                            instance.index(),
                            instance.index(),
                            instance.focus().index(),
                            instance.focus().feature(Predicate),
                            instance.focus().feature(Gold),
                            instance.sequence().tokens().stream()
                                    .map(t -> (String) t.feature(Text))
                                    .collect(Collectors.joining(" ")));
                }
                writer.println(result);
            }
        }
    }

    public static List<NlpFocus<DepNode, DepTree>> getFocusInstances(List<DepTree> dependencyTrees) {
        List<NlpFocus<DepNode, DepTree>> instances = new ArrayList<>();
        for (DepTree dependencyTree : dependencyTrees) {
            for (DepNode depNode : dependencyTree) {
                if (depNode.feature(Predicate) != null) {
                    instances.add(new DefaultNlpFocus<>(instances.size(), depNode, dependencyTree));
                }
            }
        }
        return instances;
    }

}
