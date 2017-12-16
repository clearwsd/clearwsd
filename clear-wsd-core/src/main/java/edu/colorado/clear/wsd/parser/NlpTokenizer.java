package edu.colorado.clear.wsd.parser;

import java.util.List;

/**
 * Tokenizer/sentence segmenter for natural language text. Splits text into sentences with {@link NlpTokenizer#segment(String)},
 * and further splits an individual sentence into a list of tokens through {@link NlpTokenizer#tokenize(String)}.
 *
 * @author jamesgung
 */
public interface NlpTokenizer {

    /**
     * Split raw input text into a list of sentences. Each {@link String} returned in the list should be a single sentence.
     *
     * @param input input text
     * @return list of sentence strings
     */
    List<String> segment(String input);

    /**
     * Split an input sentence into individual tokens.
     *
     * @param sentence single sentence
     * @return list of tokens
     */
    List<String> tokenize(String sentence);

}
