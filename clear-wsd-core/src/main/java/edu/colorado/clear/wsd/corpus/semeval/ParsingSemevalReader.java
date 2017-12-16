package edu.colorado.clear.wsd.corpus.semeval;

import java.util.stream.Collectors;

import edu.colorado.clear.parser.NlpParser;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.wsd.type.DefaultDepTree;

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
