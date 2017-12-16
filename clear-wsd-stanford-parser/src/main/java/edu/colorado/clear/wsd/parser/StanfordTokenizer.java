package edu.colorado.clear.wsd.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.colorado.clear.parser.NlpTokenizer;
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
