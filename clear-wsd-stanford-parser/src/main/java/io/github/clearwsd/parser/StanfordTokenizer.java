/*
 * Copyright (C) 2017  James Gung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.clearwsd.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import io.github.clearwsd.parser.NlpTokenizer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * Stanford CoreNLP-based {@link NlpTokenizer} implementation.
 *
 * @author jamesgung
 */
public class StanfordTokenizer implements NlpTokenizer {

    private static final String NO_ESCAPING = "ptb3Escaping=false";

    private TokenizerFactory tokenizer;

    public StanfordTokenizer() {
        tokenizer = PTBTokenizer.coreLabelFactory();
    }

    @Override
    public List<String> segment(String input) {
        DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(input));
        List<String> results = new ArrayList<>();
        for (List<HasWord> sentence : preprocessor) {
            results.add(SentenceUtils.listToOriginalTextString(sentence));
        }
        return results;
    }

    @Override
    public List<String> tokenize(String sentence) {
        Tokenizer tokenizer = this.tokenizer.getTokenizer(new StringReader(sentence), NO_ESCAPING);
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasNext()) {
            tokens.add(((HasWord) tokenizer.next()).word());
        }
        return tokens;
    }

}
