package edu.colorado.clear.wsd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.type.NlpFocus;
import edu.colorado.clear.wsd.classifier.Classifier;
import edu.colorado.clear.wsd.classifier.Hyperparameter;
import edu.colorado.clear.wsd.utils.LemmaDictionary;
import edu.colorado.clear.wsd.utils.SenseInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import static edu.colorado.clear.type.FeatureType.Predicate;

/**
 * Word sense classifier--restricts predictions to classes provided by a given sense inventory.
 *
 * @author jamesgung
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class WordSenseClassifier implements Classifier<NlpFocus<DepNode, DepTree>, String> {

    private static final long serialVersionUID = -7555582268789530929L;

    private Classifier<NlpFocus<DepNode, DepTree>, String> classifier;
    private SenseInventory senseInventory;
    private LemmaDictionary predicateDictionary;

    public WordSenseClassifier(ObjectInputStream is) {
        load(is);
    }

    @Override
    public String classify(NlpFocus<DepNode, DepTree> instance) {
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
    public Map<String, Double> score(NlpFocus<DepNode, DepTree> instance) {
        return classifier.score(instance);
    }

    @Override
    public void train(List<NlpFocus<DepNode, DepTree>> train, List<NlpFocus<DepNode, DepTree>> valid) {
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
            classifier = (Classifier<NlpFocus<DepNode, DepTree>, String>) inputStream.readObject();
            senseInventory = (SenseInventory) inputStream.readObject();
            predicateDictionary = (LemmaDictionary) inputStream.readObject();
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

    /**
     * Load/initialize a word sense classifier from a provided {@link URL}.
     *
     * @param path path to classifier model
     * @return initialized word sense classifier
     */
    public static WordSenseClassifier load(URL path) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(path.openStream())) {
            return new WordSenseClassifier(objectInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load classifier model at " + path.getPath() + ": " + e.getMessage(), e);
        }
    }

}
