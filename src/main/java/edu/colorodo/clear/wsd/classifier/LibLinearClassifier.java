package edu.colorodo.clear.wsd.classifier;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import edu.colorodo.clear.wsd.feature.model.Vocabulary;
import edu.colorodo.clear.wsd.feature.model.BaseFeatureModel;
import edu.colorodo.clear.wsd.feature.model.FeatureModel;
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
public class LibLinearClassifier implements SparseClassifier<LibLinearClassifier.LiblinearModel> {

    public static class LiblinearModel implements FeatureModel, ModelParameters {

        private Model liblinearModel;
        private FeatureModel featureModel;

        public LiblinearModel() {
            featureModel = new BaseFeatureModel();
        }

        @Override
        public Vocabulary features() {
            return featureModel.features();
        }

        @Override
        public LiblinearModel features(Vocabulary features) {
            featureModel.features(features);
            return this;
        }

        @Override
        public Vocabulary labels() {
            return featureModel.labels();
        }

        @Override
        public LiblinearModel labels(Vocabulary labels) {
            featureModel.labels(labels);
            return this;
        }

        @Override
        public String label(int index) {
            return featureModel.label(index);
        }

        @Override
        public Integer labelIndex(String label) {
            return featureModel.labelIndex(label);
        }

        @Override
        public String feature(int index) {
            return featureModel.feature(index);
        }

        @Override
        public Integer featureIndex(String feature) {
            return featureModel.featureIndex(feature);
        }
    }

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
    private LiblinearModel parameters;

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
    public Map<String, Double> score(SparseVector instance) {
        double[] scores = new double[parameters.labels().indices().size()];
        if (parameters.liblinearModel.isProbabilityModel()) {
            Linear.predictProbability(parameters.liblinearModel, getFeatureArray(instance), scores);
        } else {
            Linear.predictValues(parameters.liblinearModel, getFeatureArray(instance), scores);
        }
        Map<String, Double> results = new HashMap<>();
        for (Map.Entry<String, Integer> entry : parameters.labels().indices().entrySet()) {
            results.put(entry.getKey(), scores[entry.getValue()]);
        }
        return results;
    }


    @Override
    public void initialize(LiblinearModel parameters) {
        this.parameters = parameters;
    }

    @Override
    public String classify(SparseVector instance) {
        Feature[] feat = getFeatureArray(instance);
        return parameters.label((int) Linear.predict(parameters.liblinearModel, feat));
    }

    @Override
    public LiblinearModel train(List<StringInstance> train, List<StringInstance> valid) {
        Preconditions.checkNotNull(parameters, "Model parameters must not be null. Initialize before training.");
        Problem problem = new Problem();
        problem.l = train.size();
        problem.n = parameters.features().indices().size();
        problem.x = getFeatures(train);
        problem.y = getLabels(train);
        problem.bias = -1; // don't include bias
        Stopwatch sw = Stopwatch.createStarted();
        log.debug("Commencing training on {} examples with {} labels and {} features.", problem.l, parameters.labels().indices().size(),
                problem.n);
        parameters.liblinearModel = Linear.train(problem, new Parameter(solverType, cost, eps));
        log.debug("Training completed successfully in {}.", sw.toString());
        return parameters;
    }

    @Override
    public void load(InputStream inputStream) {
        try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            parameters = (LiblinearModel) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Unable to load LibLinear classifier model.", e);
        }
    }

    @Override
    public void save(OutputStream outputStream) {
        try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
            oos.writeObject(parameters);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save LibLinear classifier model.", e);
        }
    }

    private double[] getLabels(List<? extends StringInstance> instances) {
        return instances.stream()
                .mapToDouble(StringInstance::target)
                .toArray();
    }

    private Feature[][] getFeatures(List<? extends StringInstance> vectors) {
        Feature[][] features = new FeatureNode[vectors.size()][];
        int index = 0;
        for (StringInstance vector : vectors) {
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

}
