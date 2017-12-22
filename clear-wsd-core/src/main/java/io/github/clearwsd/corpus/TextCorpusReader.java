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

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepTree;
import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.corpus.CoNllDepTreeReader.writeDependencyTrees;

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
        CoNllDepTreeReader.writeDependencyTrees(instances, outputStream);
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
                        CoNllDepTreeReader.writeDependencyTrees(cache, writer);
                        cache = new ArrayList<>();
                        log.debug("Parsing {} trees/s", processed / sw.elapsed(TimeUnit.SECONDS));
                    }
                }
            }
            if (cache.size() > 0) {
                CoNllDepTreeReader.writeDependencyTrees(cache, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
