package edu.colorodo.clear.wsd.eval;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sampling-based cross validation w/ per-class sub-folds for balanced sampling.
 *
 * @author jamesgung
 */
@Slf4j
public class CrossValidation<T> {

    private Random random;

    private Function<T, String> labelFunction;

    public CrossValidation(int seed, Function<T, String> labelFunction) {
        random = new Random(seed);
        this.labelFunction = labelFunction;
    }

    public CrossValidation(Function<T, String> labelFunction) {
        this(0, labelFunction);
    }

    public List<Fold<T>> createFolds(List<T> instances, int numFolds, double ratio) {
        ListMultimap<String, T> partition = partitionToClasses(instances);
        return sampleFolds(partition, numFolds, ratio);
    }

    private List<Fold<T>> sampleFolds(ListMultimap<String, T> partition, int numFolds, double ratio) {
        List<List<Fold<T>>> perClassFolds = partition.keySet().stream().
                map(key -> sampleFolds(partition.get(key), numFolds, ratio))
                .collect(Collectors.toList());
        return combineFolds(perClassFolds, numFolds);
    }

    private List<Fold<T>> combineFolds(List<List<Fold<T>>> folds, int numFolds) {
        List<Fold<T>> combined = new ArrayList<>();
        for (int i = 0; i < numFolds; ++i) {
            Fold<T> fold = new Fold<>();
            for (List<Fold<T>> subsets : folds) {
                fold.add(subsets.get(i));
            }
            combined.add(fold);
        }
        return combined;
    }

    private List<Fold<T>> sampleFolds(List<T> instances, int numSubsets, double ratio) {
        List<Fold<T>> subsets = new ArrayList<>();
        int trainInstances = (int) (ratio * instances.size());
        int testInstances = instances.size() - trainInstances;
        if (testInstances == 0) {
            if (trainInstances > 1) {
                trainInstances--;
                testInstances++;
            } else {
                log.warn("Need at least two instances to create a train/test fold.");
            }
        }
        for (int subset = 0; subset < numSubsets; ++subset) {
            Collections.shuffle(instances, random);
            List<T> train = new ArrayList<>();
            for (int i = 0; i < trainInstances; ++i) {
                train.add(instances.get(i));
            }
            List<T> test = new ArrayList<>();
            for (int i = trainInstances; i < trainInstances + testInstances; ++i) {
                test.add(instances.get(i));
            }
            subsets.add(new Fold<>(train, test));
        }
        return subsets;
    }

    private ListMultimap<String, T> partitionToClasses(List<T> instances) {
        ListMultimap<String, T> map = ArrayListMultimap.create();
        for (T instance : instances) {
            map.put(labelFunction.apply(instance), instance);
        }
        return map;
    }

    public static class Fold<T> {

        @Getter
        private List<T> trainInstances;
        @Getter
        private List<T> testInstances;

        public Fold(List<T> trainInstances, List<T> testInstances) {
            this.trainInstances = trainInstances;
            this.testInstances = testInstances;
        }

        public Fold() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        private void add(Fold<T> fold) {
            trainInstances.addAll(fold.getTrainInstances());
            testInstances.addAll(fold.getTestInstances());
        }

    }

}
