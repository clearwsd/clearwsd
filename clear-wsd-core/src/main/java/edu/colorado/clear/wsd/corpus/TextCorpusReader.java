package edu.colorado.clear.wsd.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.type.DepTree;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.corpus.CoNllDepTreeReader.writeDependencyTrees;

/**
 * Corpus reader over a plain text file. Text is segmented and tokenized, and dependency trees are produced.
 *
 * @author jamesgung
 */
@Slf4j
public class TextCorpusReader implements CorpusReader<DepTree> {

    private DependencyParser parser;

    public TextCorpusReader(DependencyParser parser) {
        this.parser = parser;
    }

    @Override
    public List<DepTree> readInstances(InputStream inputStream) {
        List<DepTree> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                for (String sentence : parser.segment(line)) {
                    DepTree tree = parser.parse(parser.tokenize(sentence));
                    results.add(tree);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return results;
    }

    @Override
    public void writeInstances(List<DepTree> instances, OutputStream outputStream) {
        writeDependencyTrees(instances, outputStream);
    }

}
