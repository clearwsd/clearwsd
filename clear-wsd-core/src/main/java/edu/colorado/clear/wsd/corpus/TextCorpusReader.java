package edu.colorado.clear.wsd.corpus;

import com.google.common.base.Stopwatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.colorado.clear.parser.NlpParser;
import edu.colorado.clear.type.DepTree;
import lombok.extern.slf4j.Slf4j;

import static edu.colorado.clear.wsd.corpus.CoNllDepTreeReader.writeDependencyTrees;

/**
 * Corpus reader over a plain text file. Text is segmented and tokenized, and dependency trees are produced.
 *
 * @author jamesgung
 */
@Slf4j
public class TextCorpusReader implements CorpusReader<DepTree> {

    private NlpParser parser;

    public TextCorpusReader(NlpParser parser) {
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

    /**
     * Simultaneously parse and write dependency trees to an output stream. Memory usage can be controlled with
     * a provided cache size parameter, which controls the number of trees to parse before writing them (and GC).
     *
     * @param inputStream  input stream
     * @param outputStream output stream
     * @param maxCache     maximum number of trees to parse before writing/flushing
     */
    public void parseAndWrite(InputStream inputStream, OutputStream outputStream, int maxCache) {
        List<DepTree> cache = new ArrayList<>();
        int processed = 0;
        Stopwatch sw = Stopwatch.createStarted();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             PrintWriter writer = new PrintWriter(outputStream)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                for (String sentence : parser.segment(line)) {
                    DepTree tree = parser.parse(parser.tokenize(sentence));
                    cache.add(tree);
                    ++processed;
                    if (cache.size() >= maxCache) {
                        writeDependencyTrees(cache, writer);
                        cache = new ArrayList<>();
                        log.debug("Parsing {} trees/s", processed / sw.elapsed(TimeUnit.SECONDS));
                    }
                }
            }
            if (cache.size() > 0) {
                writeDependencyTrees(cache, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
