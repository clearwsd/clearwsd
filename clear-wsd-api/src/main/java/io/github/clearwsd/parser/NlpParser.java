package io.github.clearwsd.parser;

import java.util.List;

import io.github.clearwsd.type.DepTree;

/**
 * Syntactic dependency parser, used to produce {@link DepTree DepTrees} from raw text. Extends from {@link NlpTokenizer} to ensure
 * that any pre-tokenized/segmented text can be handled, while still handling raw un-tokenized text, such as from a document.
 *
 * @author jamesgung
 */
public interface NlpParser extends NlpTokenizer {

    /**
     * Parse a single tokenized sentence, producing an {@link DepTree}.
     *
     * @param tokens tokenized text of a single sentence
     * @return syntactic dependency tree
     */
    DepTree parse(List<String> tokens);

}
