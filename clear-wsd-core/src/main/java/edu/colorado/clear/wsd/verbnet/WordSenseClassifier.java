package edu.colorado.clear.wsd.verbnet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import edu.colorado.clear.wsd.utils.SenseInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import static edu.colorado.clear.wsd.type.FeatureType.Predicate;

/**
 * Word sense classifier--restricts predictions to classes provided by a given sense inventory.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class WordSenseClassifier implements Classifier<FocusInstance<DepNode, DependencyTree>, String> {

    private static final long serialVersionUID = -7555582268789530929L;

    private Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier;
    private SenseInventory senseInventory;
    private PredicateDictionary predicateDictionary;

    public WordSenseClassifier(ObjectInputStream is) {
        load(is);
    }

    @Override
    public String classify(FocusInstance<DepNode, DependencyTree> instance) {
        String lemma = instance.focus().feature(Predicate);
        Set<String> options = senseInventory.senses(instance.focus().feature(Predicate));
        Map<String, Double> scores = classifier.score(instance);
        return scores.entrySet().stream()
                .filter(e -> options.contains(e.getKey()))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst() // get highest scoring sense for a given predicate
                .orElse(senseInventory.defaultSense(lemma)); // or return the default sense for the predicate
    }

    @Override
    public Map<String, Double> score(FocusInstance<DepNode, DependencyTree> instance) {
        return classifier.score(instance);
    }

    @Override
    public void train(List<FocusInstance<DepNode, DependencyTree>> train, List<FocusInstance<DepNode, DependencyTree>> valid) {
        predicateDictionary.train(true);
        Stream.concat(train.stream(), valid.stream()).forEach(instance -> {
            predicateDictionary.apply(instance.focus());
            senseInventory.addSense(instance.focus().feature(FeatureType.Lemma), instance.focus().feature(FeatureType.Gold));
        });
        predicateDictionary.train(false);
        classifier.train(train, valid);
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return classifier.hyperparameters();
    }

    @Override
    public void initialize(Properties properties) {
        classifier.initialize(properties);
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            classifier = (Classifier<FocusInstance<DepNode, DependencyTree>, String>) inputStream.readObject();
            senseInventory = (SenseInventory) inputStream.readObject();
            predicateDictionary = (PredicateDictionary) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to load classifier: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(senseInventory);
            outputStream.writeObject(predicateDictionary);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save classifier: " + e.getMessage(), e);
        }
    }

}
