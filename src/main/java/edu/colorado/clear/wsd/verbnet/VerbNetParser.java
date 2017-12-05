package edu.colorado.clear.wsd.verbnet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.colorado.clear.wsd.classifier.LibLinearClassifier;
import edu.colorado.clear.wsd.feature.model.FeatureModel;
import edu.colorado.clear.wsd.feature.optim.EvolutionaryModelTrainer;
import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.StanfordDependencyParser;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.utils.InteractiveTestLoop;
import lombok.AllArgsConstructor;

import static edu.colorado.clear.wsd.type.FeatureType.Dep;
import static edu.colorado.clear.wsd.type.FeatureType.Sense;

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

        LibLinearClassifier cls = ((LibLinearClassifier) ((EvolutionaryModelTrainer) classifier.classifier()).classifier()
                .sparseClassifier());
        FeatureModel feat = ((EvolutionaryModelTrainer) classifier.classifier()).classifier().featurePipeline().model();
        for (FeatureVector vector : visualize(feat, cls).values()) {
            System.out.println(vector.label + "\t...");
            for (int i = 0; i < 20; ++i) {
                FeatureWeight weight = vector.featureWeights.get(i);
                String feature = feat.feature(weight.index);
                System.out.println(String.format("%d. %s\t%f", i + 1, feature, weight.value));
            }
        }

        VerbNetAnnotator annotator = new VerbNetAnnotator(classifier,
                new DefaultPredicateAnnotator(classifier.predicateDictionary()));
        VerbNetParser parser = new VerbNetParser(annotator, new StanfordDependencyParser());
        InteractiveTestLoop.test(parser, Arrays.asList(Sense.name(), Dep.name()));
    }

    public static Map<String, FeatureVector> visualize(FeatureModel featureModel, LibLinearClassifier libLinearClassifier) {
        Map<String, FeatureVector> vectors = new HashMap<>();
        for (Map.Entry<String, Integer> label : featureModel.labels().indices().entrySet()) {
            List<FeatureWeight> weights = new ArrayList<>();
            for (int i = 0; i < libLinearClassifier.model().getNrFeature(); ++i) {
                weights.add(new FeatureWeight(i, libLinearClassifier.model().getDecfunCoef(i, label.getValue())));
            }
            weights.sort((f1, f2) -> Double.compare(f2.value, f1.value));
            vectors.put(label.getKey(), new FeatureVector(label.getKey(), weights));
        }
        return vectors;
    }

    @AllArgsConstructor
    public static class FeatureVector {
        private String label;
        private List<FeatureWeight> featureWeights = new ArrayList<>();
    }

    @AllArgsConstructor
    public static class FeatureWeight {
        private int index;
        private double value;
    }

}
