package edu.colorado.clear.wsd;

import java.util.List;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.type.DependencyTree;
import lombok.AllArgsConstructor;

/**
 * Parser wrapper that applies word sense annotations via a {@link WordSenseAnnotator} to inputs following parsing.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class SenseDisambiguatingParser implements DependencyParser {

    private WordSenseAnnotator annotator;
    private DependencyParser dependencyParser;

    @Override
    public DependencyTree parse(List<String> tokens) {
        return annotator.annotate(dependencyParser.parse(tokens));
    }

    @Override
    public List<String> segment(String input) {
        return dependencyParser.segment(input);
    }

    @Override
    public List<String> tokenize(String sentence) {
        return dependencyParser.tokenize(sentence);
    }

}
