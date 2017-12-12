package edu.colorado.clear.wsd.verbnet;

import java.util.List;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.type.DependencyTree;
import lombok.AllArgsConstructor;

/**
 * Parser wrapper that applies VerbNet annotations to inputs.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class VerbNetParser implements DependencyParser {

    private VerbNetAnnotator annotator;
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
