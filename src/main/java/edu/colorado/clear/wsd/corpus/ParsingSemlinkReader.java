package edu.colorado.clear.wsd.corpus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.corpus.VerbNetReader.VerbNetInstanceParser;
import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.NlpTokenizer;
import edu.colorado.clear.wsd.parser.StanfordDependencyParser;
import edu.colorado.clear.wsd.parser.WhitespaceTokenizer;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.type.FeatureType.Gold;
import static edu.colorado.clear.wsd.type.FeatureType.Lemma;
import static edu.colorado.clear.wsd.type.FeatureType.Metadata;
import static edu.colorado.clear.wsd.type.FeatureType.Predicate;
import static edu.colorado.clear.wsd.type.FeatureType.Text;

/**
 * Corpus reader that reads and parses SemLink-style annotations.
 *
 * @author jamesgung
 */
@Slf4j
@AllArgsConstructor
public class ParsingSemlinkReader implements CorpusReader<FocusInstance<DepNode, DependencyTree>> {

    private DependencyParser dependencyParser;
    private NlpTokenizer tokenizer;

    @Override
    public List<FocusInstance<DepNode, DependencyTree>> readInstances(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            VerbNetInstanceParser parser = new VerbNetInstanceParser();
            List<FocusInstance<DepNode, DependencyTree>> results = new ArrayList<>();
            int index = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                VerbNetReader.VerbNetInstance instance = parser.parse(line);
                DependencyTree depTree = dependencyParser.parse(tokenizer.tokenize(instance.originalText()));

                DepNode focus = depTree.get(instance.token());
                if (!instance.lemma().equalsIgnoreCase(focus.feature(Lemma))) {
                    log.warn("Lemma mismatch ({} vs. {}) between annotation and parser output for instance: {}",
                            instance.lemma(), focus.feature(Lemma), line);
                }
                focus.addFeature(Gold, instance.label());
                focus.addFeature(Predicate, instance.lemma());

                FocusInstance<DepNode, DependencyTree> focusInstance = new FocusInstance<>(index++, focus, depTree);
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
    public void writeInstances(List<FocusInstance<DepNode, DependencyTree>> instances, OutputStream outputStream) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            for (FocusInstance<DepNode, DependencyTree> instance : instances) {
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

    public static void main(String[] args) throws FileNotFoundException {
        List<FocusInstance<DepNode, DependencyTree>> focusInstances = new ParsingSemlinkReader(
                new StanfordDependencyParser(), new WhitespaceTokenizer())
                .readInstances(new FileInputStream("data/all.txt"));
        new VerbNetReader().writeInstances(focusInstances, new FileOutputStream("data/all-new.dep"));
    }

}
