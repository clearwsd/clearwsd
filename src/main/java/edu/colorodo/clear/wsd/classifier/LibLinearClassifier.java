package edu.colorodo.clear.wsd.classifier;

import com.google.common.base.Stopwatch;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Classifier wrapper for LibLinear (http://www.csie.ntu.edu.tw/~cjlin/liblinear/).
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class LibLinearClassifier implements SparseClassifier {

    private static final long serialVersionUID = -2620708926923172510L;

    public enum LibLinearParameter implements Hyperparameter<LibLinearClassifier> {

        Solver("LibLinear solver", SolverType.L2R_L2LOSS_SVC.name(),
                (c, value) -> c.solverType = SolverType.valueOf(value)),
        Cost("cost of constraints violation", "1", (c, value) -> c.cost = Double.valueOf(value)),
        Epsilon("stopping criterion", "0.01", (c, value) -> c.eps = Double.valueOf(value));

        private Hyperparameter<LibLinearClassifier> parameter;

        LibLinearParameter(String parameter, String defaultValue, BiConsumer<LibLinearClassifier, String> assign) {
            this.parameter = new DefaultHyperparameter<>(this.name(), parameter, defaultValue, assign);
        }

        public String description() {
            return parameter.description();
        }

        @Override
        public String defaultValue() {
            return parameter.defaultValue();
        }

        @Override
        public void assignValue(LibLinearClassifier model, String value) {
            parameter.assignValue(model, value);
        }
    }

    @Getter
    private Model model;

    private SolverType solverType;
    private double cost;
    private double eps;

    public LibLinearClassifier() {
        initialize(new Properties());
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return Arrays.asList(LibLinearParameter.values());
    }

    @Override
    public Map<Integer, Double> score(SparseInstance instance) {
        double[] scores = new double[model.getNrClass()];
        if (model.isProbabilityModel()) {
            Linear.predictProbability(model, getFeatureArray(instance), scores);
        } else {
            Linear.predictValues(model, getFeatureArray(instance), scores);
        }
        Map<Integer, Double> results = new HashMap<>();
        for (double score : scores) {
            results.put(results.size(), score);
        }
        return results;
    }

    @Override
    public Integer classify(SparseInstance instance) {
        Feature[] feat = getFeatureArray(instance);
        return (int) Linear.predict(model, feat);
    }

    @Override
    public void train(List<SparseInstance> train, List<SparseInstance> valid) {
        Problem problem = new Problem();
        problem.l = train.size();
        problem.n = getMaxIndex(train);
        problem.x = getFeatures(train);
        problem.y = getLabels(train);
        problem.bias = -1; // don't include bias
        Stopwatch sw = Stopwatch.createStarted();
        log.debug("Commencing training on {} examples with {} features.", problem.l, problem.n);
        model = Linear.train(problem, new Parameter(solverType, cost, eps));
        log.debug("Training completed successfully in {}.", sw.toString());
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            model = (Model) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double[] getLabels(List<SparseInstance> instances) {
        return instances.stream()
                .mapToDouble(SparseInstance::target)
                .toArray();
    }

    private Feature[][] getFeatures(List<SparseInstance> vectors) {
        Feature[][] features = new FeatureNode[vectors.size()][];
        int index = 0;
        for (SparseInstance vector : vectors) {
            features[index++] = getFeatureArray(vector);
        }
        return features;
    }

    private Feature[] getFeatureArray(SparseVector vector) {
        Feature[] features = new FeatureNode[vector.data().length];
        int[] indices = vector.indices();
        float[] values = vector.data();
        for (int i = 0; i < features.length; ++i) {
            features[i] = new FeatureNode(indices[i] + 1, values[i]);
        }
        return features;
    }

    private int getMaxIndex(List<? extends SparseInstance> instances) {
        int maxIndex = 0;
        for (SparseInstance instance : instances) {
            for (int index : instance.indices()) {
                maxIndex = Math.max(maxIndex, index);
            }
        }
        return maxIndex + 1;
    }

}
