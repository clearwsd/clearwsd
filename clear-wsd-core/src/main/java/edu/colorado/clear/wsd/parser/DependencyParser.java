package edu.colorado.clear.wsd.parser;

import java.util.List;

import edu.colorado.clear.wsd.type.DepTree;

/**
 * Dependency parser interface.
 *
 * @author jamesgung
 */
public interface DependencyParser extends NlpTokenizer {

    /**
     * Parse a sentence into an {@link DepTree}.
     *
     * @param tokens input sentence tokens
     * @return dependency tree
     */
    DepTree parse(List<String> tokens);

}
