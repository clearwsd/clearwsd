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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.corpus.CoNllDepTreeReader;
import io.github.clearwsd.corpus.CorpusReader;
import io.github.clearwsd.type.DefaultNlpFocus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static io.github.clearwsd.type.FeatureType.Gold;
import static io.github.clearwsd.type.FeatureType.Metadata;
import static io.github.clearwsd.type.FeatureType.Predicate;
import static io.github.clearwsd.type.FeatureType.Sense;
import static io.github.clearwsd.type.FeatureType.Text;
import static io.github.clearwsd.corpus.CoNllDepTreeReader.treeToString;

/**
 * VerbNet (parsed) corpus reader.
 *
 * @author jamesgung
 */
public class VerbNetReader implements CorpusReader<NlpFocus<DepNode, DepTree>> {

    private VerbNetCoNllDepReader depReader = new VerbNetCoNllDepReader();

    @Override
    public List<NlpFocus<DepNode, DepTree>> readInstances(InputStream inputStream) {
        List<NlpFocus<DepNode, DepTree>> results = new ArrayList<>();
        int index = 0;
        for (DepTree tree : depReader.readInstances(inputStream)) {
            for (DepNode focus : tree.tokens().stream()
                    .filter(t -> t.feature(Gold) != null)
                    .collect(Collectors.toList())) {
                NlpFocus<DepNode, DepTree> instance = new DefaultNlpFocus<>(index++, focus, tree);
                instance.addFeature(Gold, focus.feature(Gold));
                results.add(instance);
            }
        }
        return results;
    }

    @Override
    public void writeInstances(List<NlpFocus<DepNode, DepTree>> instances, OutputStream outputStream) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            if (instances.size() == 0) {
                return;
            }
            DepTree currentTree = instances.get(0).sequence();
            for (NlpFocus<DepNode, DepTree> instance : instances) {
                if (instance.sequence() != currentTree) {
                    writer.println(treeToString(currentTree, Sense.name()));
                    writer.println();
                    currentTree = instance.sequence();
                }
                String metadata = instance.feature(Metadata);
                if (metadata == null) {
                    writer.println("# " + VerbNetInstanceParser.toString(new VerbNetInstance()
                            .path(Integer.toString(instance.index()))
                            .label(Optional.<String>ofNullable(instance.focus().feature(Gold))
                                    .orElse(instance.focus().feature(Sense)))
                            .sentence(instance.sequence().index())
                            .token(instance.focus().index())
                            .lemma(instance.focus().feature(Predicate))
                            .originalText(Optional.<String>ofNullable(instance.sequence().feature(Text)).orElse(
                                    currentTree.tokens().stream().map(t -> (String) t.feature(Text))
                                            .collect(Collectors.joining(" "))))));
                } else {
                    writer.println("# " + metadata);
                }
                writer.flush();
            }
            writer.println(treeToString(currentTree, Sense.name()));
        }
    }

    public static class VerbNetCoNllDepReader extends CoNllDepTreeReader {
        @Override
        protected void processHeader(List<String> header, DepTree result) {
            for (String headerLine : header) {
                headerLine = headerLine.replaceAll("^#\\s*", "");
                VerbNetInstance instance = new VerbNetInstanceParser().parse(headerLine);
                DepNode focus = result.get(instance.token);
                focus.addFeature(Gold, instance.label);
                focus.addFeature(Sense, instance.label);
                focus.addFeature(Predicate, instance.lemma);
                result.addFeature(Text, instance.originalText);
            }
        }
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class VerbNetInstance {
        private String path;
        private int sentence;
        private int token;
        private String lemma;
        private String label;
        private String originalText;
    }

    public static class VerbNetInstanceParser {

        public VerbNetInstance parse(String input) {
            String[] fields = input.split("\t");
            String[] subFields = fields[0].split(" ");
            return new VerbNetInstance()
                    .path(subFields[0])
                    .sentence(Integer.parseInt(subFields[1]))
                    .token(Integer.parseInt(subFields[2]))
                    .lemma(subFields[3])
                    .label(subFields[4])
                    .originalText(fields[1]);
        }

        public static String toString(VerbNetInstance instance) {
            return String.format("%s %d %d %s %s\t%s", instance.path, instance.sentence, instance.token,
                    instance.lemma, instance.label, instance.originalText);
        }
    }

}
