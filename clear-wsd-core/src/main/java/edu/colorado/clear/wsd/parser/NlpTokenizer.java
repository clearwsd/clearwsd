package edu.colorado.clear.wsd.parser;

import java.util.List;

/**
 * Tokenizer interface.
 *
 * @author jamesgung
 */
public interface NlpTokenizer {

    /**
     * Split an input into sentences.
     *
     * @param input input text
     * @return list of sentence strings
     */
    List<String> segment(String input);

    /**
     * Split an input into tokens.
     *
     * @param sentence single sentence
     * @return list of tokens
     */
    List<String> tokenize(String sentence);

}
