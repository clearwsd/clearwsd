package io.github.clearwsd;

import java.util.List;
import java.util.stream.Collectors;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.utils.SenseInventory;
import lombok.AllArgsConstructor;

/**
 * Parser wrapper that applies word sense annotations via a {@link WordSenseAnnotator} to inputs following parsing.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DefaultSensePredictor implements NlpParser, SensePredictor<String> {

    private WordSenseAnnotator annotator;
    private NlpParser dependencyParser;

    @Override
    public DepTree parse(List<String> tokens) {
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

    @Override
    public List<String> predict(List<String> sentence) {
        DepTree depTree = parse(sentence);
        return depTree.tokens().stream()
                .map(token -> token.feature(FeatureType.Sense))
                .map(sense -> sense == null ? SenseInventory.DEFAULT_SENSE : (String) sense)
                .collect(Collectors.toList());
    }

    /**
     * Initialize a {@link DefaultSensePredictor} from a classpath resource and parser.
     *
     * @param resource classpath resource
     * @return initialized sense predictor
     */
    public static DefaultSensePredictor loadFromResource(String resource, NlpParser parser) {
        return new DefaultSensePredictor(WordSenseAnnotator.loadFromResource(resource), parser);
    }

}
