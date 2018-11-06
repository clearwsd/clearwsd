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

package io.github.clearwsd.feature.util;

import com.google.common.base.Stopwatch;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Lucene wrapper.
 *
 * @author jamesgung
 */
@Slf4j
public class LuceneWrapper implements Serializable {

    private static final long serialVersionUID = -3704052454877512322L;

    private static final String VERB = "verb";
    private static final String FREQ = "frequency";

    private static IndexSearcher indexSearcher;
    private static Analyzer analyzer;

    private File index;

    public LuceneWrapper(File indexDir) {
        index = indexDir;
        if (indexSearcher == null) {
            initialize(indexDir);
        }
    }

    private void initialize(File indexDir) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            indexSearcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(indexDir.toPath())));
            analyzer = new StandardAnalyzer();
            log.info("Initialized lucene index at {} ({})", indexDir.getPath(), stopwatch.stop());
        } catch (IOException e) {
            throw new RuntimeException("Unable to locate Lucene index.", e);
        }
    }

    public Map<String, Integer> search(String word, String field, int maxSearch) {
        if (indexSearcher == null) {
            initialize(index);
        }
        Map<String, Integer> verbFreqs = new HashMap<>();
        QueryParser queryParser = new QueryParser(field, analyzer);
        try {
            Query query = queryParser.parse(word);
            TopDocs topDocs = indexSearcher.search(query, maxSearch);
            ScoreDoc[] doc = topDocs.scoreDocs;
            for (int i = 0; i < maxSearch && i < doc.length; ++i) {
                int documentId = doc[i].doc;
                Document document = indexSearcher.doc(documentId);
                String verb = document.get(VERB);
                String frequency = document.get(FREQ);
                verbFreqs.put(verb, Integer.parseInt(frequency));
            }
        } catch (ParseException | IOException e) {
            log.warn("Error searching Lucene index.", e);
        }
        return verbFreqs;
    }

}
