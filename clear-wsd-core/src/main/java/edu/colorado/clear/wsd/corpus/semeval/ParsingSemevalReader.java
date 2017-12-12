package edu.colorado.clear.wsd.corpus.semeval;

import java.util.stream.Collectors;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.type.BaseDependencyTree;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;

/**
 * Semeval XML reader.
 *
 * @author jamesgung
 */
public class ParsingSemevalReader extends SemevalReader {

    private DependencyParser parser;

    public ParsingSemevalReader(String keyPath, DependencyParser parser) {
        super(keyPath);
        this.parser = parser;
    }

    @Override
    protected DependencyTree processSentence(BaseDependencyTree dependencyTree) {
        BaseDependencyTree result = (BaseDependencyTree) parser.parse(dependencyTree.tokens().stream().
                map(t -> (String) t.feature(FeatureType.Text))
                .collect(Collectors.toList()));
        result.addFeature(FeatureType.Id, dependencyTree.feature(FeatureType.Id));
        result.index(dependencyTree.index());
        return result;
    }

}
