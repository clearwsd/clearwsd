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
import edu.colorado.clear.wsd.utils.CountingSenseInventory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * VerbNet classifier--restricts predictions to classes provided by a given VerbNet XML specification.
 *
 * @author jamesgung
 */
@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class VerbNetClassifier implements Classifier<FocusInstance<DepNode, DependencyTree>, String> {

    private static final long serialVersionUID = -7555582268789530929L;

    private Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier;
    private SenseInventory verbIndex = new CountingSenseInventory();
    private PredicateDictionary predicateDictionary = new PredicateDictionary();

    public VerbNetClassifier(Classifier<FocusInstance<DepNode, DependencyTree>, String> classifier) {
        this.classifier = classifier;
    }

    @Override
    public String classify(FocusInstance<DepNode, DependencyTree> instance) {
        Set<String> options = verbIndex.senses(instance.focus().feature(FeatureType.Predicate));
        Map<String, Double> scores = classifier.score(instance);
        return scores.entrySet().stream().filter(e -> options.contains(e.getKey()))
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(options.stream().findFirst().orElse(classifier.classify(instance)));
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
            verbIndex.addSense(instance.focus().feature(FeatureType.Lemma), instance.focus().feature(FeatureType.Gold));
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
            verbIndex = (SenseInventory) inputStream.readObject();
            predicateDictionary = (PredicateDictionary) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(classifier);
            outputStream.writeObject(verbIndex);
            outputStream.writeObject(predicateDictionary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
