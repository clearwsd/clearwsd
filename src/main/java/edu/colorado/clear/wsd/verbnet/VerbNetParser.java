package edu.colorado.clear.wsd.verbnet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.StanfordDependencyParser;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.utils.InteractiveTestLoop;
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

    public static void main(String[] args) throws IOException {
        String inputPath = args[0];
        VerbNetClassifier classifier = new VerbNetClassifier();
        classifier.load(new ObjectInputStream(new FileInputStream(inputPath)));
        VerbNetAnnotator annotator = new VerbNetAnnotator(classifier,
                new DefaultPredicateAnnotator(classifier.predicateDictionary()));
        VerbNetParser parser = new VerbNetParser(annotator, new StanfordDependencyParser());
        InteractiveTestLoop.test(parser, FeatureType.Sense.name());
    }

}
