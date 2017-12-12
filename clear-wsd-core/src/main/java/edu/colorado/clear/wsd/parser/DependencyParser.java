package edu.colorado.clear.wsd.parser;

import java.util.List;

import edu.colorado.clear.wsd.type.DependencyTree;

/**
 * Dependency parser interface.
 *
 * @author jamesgung
 */
public interface DependencyParser extends NlpTokenizer {

    /**
     * Parse a sentence into an {@link DependencyTree}.
     *
     * @param tokens input sentence tokens
     * @return dependency tree
     */
    DependencyTree parse(List<String> tokens);

}
