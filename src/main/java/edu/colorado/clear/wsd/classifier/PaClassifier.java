package edu.colorado.clear.wsd.classifier;

import com.google.common.base.Stopwatch;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.colorado.clear.wsd.eval.Evaluation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Multi-class passive aggressive classifier (PA-I).
 *
 * @author jamesgung
 */
@Slf4j
@Getter
@Setter
@Accessors(fluent = true)
public class PaClassifier implements SparseClassifier {

    private static final long serialVersionUID = 6490669996534158093L;

    public enum PaParameter implements Hyperparameter<PaClassifier> {

        Averaging("perform parameter averaging", "true", (c, value) -> c.averaging = Boolean.valueOf(value)),
        Aggressiveness("aggressiveness parameter C", "0.0025", (c, value) -> c.aggressiveness = Float.valueOf(value)),
        Epochs("maximum number of epochs", "999", (c, value) -> c.epochs = Integer.valueOf(value)),
        Patience("number of epochs with no change in loss before early stopping", "10",
                (c, value) -> c.patience = Integer.valueOf(value)),
        Shuffle("shuffle data prior to training", "true", (c, value) -> c.shuffle = Boolean.valueOf(value)),
        Seed("random seed for shuffling", "0", (c, value) -> c.seed = Integer.valueOf(value)),
        Verbose("display training logs", "false", (c, value) -> c.verbose = Boolean.valueOf(value)),
        Multithread("compute scores in multiple threads", "true", (c, value) -> c.multithread = Boolean.valueOf(value));

        private Hyperparameter<PaClassifier> parameter;

        PaParameter(String parameter, String defaultValue, BiConsumer<PaClassifier, String> assign) {
            this.parameter = new DefaultHyperparameter<>(this.name(),
                    PaClassifier.class.getSimpleName() + ":" + name(), parameter, defaultValue, assign);
        }

        @Override
        public String description() {
            return parameter.description();
        }

        @Override
        public String key() {
            return parameter.key();
        }

        @Override
        public String defaultValue() {
            return parameter.defaultValue();
        }

        @Override
        public void assignValue(PaClassifier model, String value) {
            parameter.assignValue(model, value);
        }
    }

    private boolean averaging;
    private float aggressiveness;
    private int epochs;
    private int patience;
    private boolean shuffle;
    private int seed;
    private boolean verbose;
    private boolean multithread;

    private Map<Integer, float[]> parameters = new HashMap<>();
    private Map<Integer, float[]> cachedParameters = new HashMap<>();

    public PaClassifier() {
        initialize(new Properties());
    }

    @Override
    public Integer classify(SparseInstance instance) {
        return getMax(instance);
    }

    @Override
    public Map<Integer, Double> score(SparseInstance instance) {
        return entryStream()
                .collect(Collectors.toMap(Map.Entry::getKey, i -> (double) score(instance, i.getValue())));
    }

    @Override
    public void train(List<SparseInstance> train, List<SparseInstance> valid) {
        if (train.size() == 0) {
            log.warn("No training instances provided, skipping training.");
            return;
        }
        initParameters(train);
        if (parameters.size() == 1) {
            log.warn("Only one class provided, skipping training.");
            return;
        }
        int epochsNoChange = 0;
        int count = 1;
        int min = Integer.MAX_VALUE;
        double maxScore = -Double.MAX_VALUE;
        if (verbose) {
            log.debug("Commencing training on {} examples with {} features and {} classes.", train.size(),
                    parameters.get(0).length, parameters.size());
        }
        Random random = new Random(seed);
        Stopwatch sw = Stopwatch.createStarted();
        for (int epoch = 0; epoch < epochs && epochsNoChange < patience; ++epoch) {
            if (shuffle) {
                Collections.shuffle(train, random);
            }
            int incorrect = 0;
            for (SparseInstance instance : train) {
                if (update(instance, count)) {
                    ++incorrect;
                }
            }
            if (verbose) {
                log.debug("Epoch {}: {}/{} correct.", epoch, train.size() - incorrect, train.size());
            }
            if (valid.size() > 0) {
                double validScore = test(valid);
                if (validScore > maxScore) {
                    epochsNoChange = 0;
                    maxScore = validScore;
                    if (!averaging) {
                        saveParameters();
                    }
                } else {
                    ++epochsNoChange;
                }
                if (verbose) {
                    log.debug("Epoch {}: validation data accuracy: {}", epoch, validScore);
                }
            } else {
                if (incorrect < min) {
                    min = incorrect;
                    epochsNoChange = 0;
                    saveParameters();
                } else {
                    ++epochsNoChange;
                }
            }
            if (verbose) {
                log.debug("{} epochs remaining.", patience > 0 && patience < (epochs - epoch)
                        ? String.format("%d or %d", Math.max(0, epochs - epoch),
                        Math.max(0, patience - epochsNoChange)) : Math.max(0, epochs - epoch));
            }
            ++count;
        }
        if (averaging) {
            averageParameters(count);
        } else {
            parameters = cachedParameters;
        }
        if (verbose) {
            log.debug("Training completed successfully in {}.", sw.toString());
        }
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return Arrays.asList(PaParameter.values());
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            //noinspection unchecked
            parameters = (Map<Integer, float[]>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(parameters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double test(List<SparseInstance> instances) {
        Evaluation evaluation = new Evaluation();
        for (SparseInstance instance : instances) {
            evaluation.add(classify(instance).toString(), Integer.toString(instance.target()));
        }
        return evaluation.f1();
    }

    private void initParameters(List<SparseInstance> instances) {
        int features = instances.stream()
                .map(i -> Arrays.stream(i.indices()).max().orElse(0))
                .mapToInt(i -> i)
                .max().orElse(0) + 1;
        Set<Integer> targets = instances.stream()
                .map(SparseInstance::target)
                .distinct().collect(Collectors.toSet());
        parameters = targets.stream().collect(Collectors.toMap(i -> i, i -> new float[features]));
        cachedParameters = targets.stream().collect(Collectors.toMap(i -> i, i -> new float[features]));
    }

    private float score(SparseVector featureVector, float[] weights) {
        float total = 0;
        float[] values = featureVector.data();
        int i = 0;
        for (int index : featureVector.indices()) {
            total += values[i] * weights[index];
        }
        return total;
    }

    private boolean update(SparseInstance instance, int count) {
        float[] correctVec = parameters.get(instance.target());
        double correctScore = score(instance, correctVec);

        Pair<Integer, Float> maxIncorrect = getMaxIncorrect(instance, instance.target());
        float maxScore = maxIncorrect.getRight();

        double loss = Math.max(0, 1 - correctScore + maxScore);
        if (loss > 0) {
            update(loss, instance, instance.target(), maxIncorrect.getLeft(), count);
        }
        return (correctScore < maxScore);
    }

    private Pair<Integer, Float> getMaxIncorrect(SparseVector instance, int correctLabel) {
        return entryStream()
                .filter(p -> p.getKey() != correctLabel)
                .map(p -> new ImmutablePair<>(p.getKey(), score(instance, p.getValue())))
                .max((p1, p2) -> Float.compare(p1.getRight(), p2.getRight()))
                .orElseThrow(() -> new IllegalStateException("No parameters found."));
    }

    private void update(double loss, SparseVector featureVector, int correctLabel, int incorrectLabel, int count) {
        float[] correct = parameters.get(correctLabel);
        float[] incorrect = parameters.get(incorrectLabel);
        double norm = featureVector.l2();
        double tau = loss / (2 * norm * norm);
        tau = Math.min(aggressiveness, tau);
        addToDense(tau, featureVector, correct);
        addToDense(-tau, featureVector, incorrect);
        if (averaging) {
            addToDense(tau * count, featureVector, cachedParameters.get(correctLabel));
            addToDense(-tau * count, featureVector, cachedParameters.get(incorrectLabel));
        }
    }

    private void addToDense(double weight, SparseVector sparseVector, float[] denseVector) {
        int[] indices = sparseVector.indices();
        float[] values = sparseVector.data();
        for (int i = 0; i < indices.length; ++i) {
            denseVector[indices[i]] += values[i] * weight;
        }
    }

    private int getMax(SparseVector featureVector) {
        return entryStream()
                .map(p -> new ImmutablePair<>(p.getKey(), score(featureVector, p.getValue())))
                .max((p1, p2) -> Float.compare(p1.getRight(), p2.getRight()))
                .orElseThrow(() -> new IllegalStateException("No parameters found.")).getLeft();
    }

    private void averageParameters(int count) {
        entryStream().forEach(
                param -> {
                    float[] params = parameters.get(param.getKey());
                    float[] cached = cachedParameters.get(param.getKey());
                    for (int i = 0; i < cached.length; ++i) {
                        params[i] = (params[i] * count - cached[i]) / (count - 1);
                    }
                }
        );
    }

    private void saveParameters() {
        entryStream().forEach(
                param -> {
                    float[] params = parameters.get(param.getKey());
                    float[] cached = cachedParameters.get(param.getKey());
                    System.arraycopy(params, 0, cached, 0, cached.length);
                }
        );
    }

    private Stream<Map.Entry<Integer, float[]>> entryStream() {
        return multithread ? parameters.entrySet().parallelStream() : parameters.entrySet().stream();
    }

}
