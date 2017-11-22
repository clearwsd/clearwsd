package edu.colorado.clear.wsd.parser;

import java.util.Arrays;
import java.util.List;

/**
 * Whitespace tokenizer. Tokens are split on whitespace, and sentences are split on newlines.
 *
 * @author jamesgung
 */
public class WhitespaceTokenizer implements NlpTokenizer {

    @Override
    public List<String> segment(String input) {
        return Arrays.asList(input.split("\\n"));
    }

    @Override
    public List<String> tokenize(String sentence) {
        return Arrays.asList(sentence.split("\\s+"));
    }
}
