package io.github.clearwsd.corpus.semeval;

import java.util.stream.Collectors;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.DefaultDepTree;

/**
 * Semeval XML reader.
 *
 * @author jamesgung
 */
public class ParsingSemevalReader extends SemevalReader {

    private NlpParser parser;

    public ParsingSemevalReader(String keyPath, NlpParser parser) {
        super(keyPath);
        this.parser = parser;
    }

    @Override
    protected DepTree processSentence(DefaultDepTree dependencyTree) {
        DefaultDepTree result = (DefaultDepTree) parser.parse(dependencyTree.tokens().stream().
                map(t -> (String) t.feature(FeatureType.Text))
                .collect(Collectors.toList()));
        result.addFeature(FeatureType.Id, dependencyTree.feature(FeatureType.Id));
        result.index(dependencyTree.index());
        return result;
    }

}
