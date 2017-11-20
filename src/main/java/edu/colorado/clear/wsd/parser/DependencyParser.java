package edu.colorado.clear.wsd.parser;

import java.util.List;

import edu.colorado.clear.wsd.type.DependencyTree;

/**
 * Dependency parser interface.
 *
 * @author jamesgung
 */
public interface DependencyParser {

    /**
     * Split an input into sentences.
     *
     * @param input input text
     * @return list of sentence strings
     */
    List<String> segment(String input);

    /**
     * Parse a sentence into an {@link DependencyTree}.
     *
     * @param input input sentence
     * @return dependency tree
     */
    DependencyTree parse(String input);

}
