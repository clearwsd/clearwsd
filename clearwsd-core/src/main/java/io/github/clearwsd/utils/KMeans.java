/*
 * Copyright 2017 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple multi-threaded k-Means clustering implementation with k-means++ initialization of clusters.
 *
 * @author jamesgung
 */
@Slf4j
@Accessors(fluent = true)
public class KMeans<T> {

    @Getter
    private List<KmeansPoint<T>> points;
    @Getter
    private List<KmeansCentroid> centroids;

    @Setter
    private int maxEpochs = 100;
    @Setter
    private int outputEvery = 10;
    @Setter
    private File outputPath;
    @Setter
    private int kVal;
    @Setter
    private boolean kMeansPlusPlus = true;

    private Random random;

    /**
     * Initialize a new K-Means instance.
     *
     * @param seed     random seed
     * @param kVal     number of clusters (k)
     * @param pointMap input points and associated identifiers
     */
    public KMeans(int seed, int kVal, Map<T, float[]> pointMap) {
        this.kVal = kVal;
        random = new Random(seed);
        points = pointMap.entrySet().stream().map(e -> new KmeansPoint<>(e.getKey(), e.getValue(), kVal))
                .collect(Collectors.toList());
    }

    private void initialize() {
        Preconditions.checkState(points.size() > kVal,
                "k-value is greater than or equal to number of points (%d >= %d).", kVal, points.size());
        Stopwatch sw = Stopwatch.createStarted();
        List<KmeansPoint<T>> pool = new ArrayList<>(points);
        KmeansPoint<T> sample = VectorMathUtils.sampleUniform(pool, random);
        pool.remove(sample);

        KmeansCentroid point = KmeansCentroid.centroid(sample, 0);
        centroids = Lists.newArrayList(point);
        for (int i = 1; i < kVal; ++i) {
            if (kMeansPlusPlus) {
                KmeansCentroid current = point;
                pool.parallelStream().forEach(p -> distanceToCentroid(p, current));
                sample = samplePoint(i - 1);
            } else {
                sample = VectorMathUtils.sampleUniform(pool, random);
            }
            pool.remove(sample);

            point = KmeansCentroid.centroid(sample, i);
            centroids.add(point);
        }
        log.info("Initialized centroids in {}.", sw);
    }

    /**
     * Run k-means with current configuration, resetting any previous state.
     */
    public void run() {
        Stopwatch overallSw = Stopwatch.createStarted();
        log.info("Beginning k-means with k={}, n={}, and {} iterations", kVal, points.size(), maxEpochs);
        initialize();
        double previousTotal = Double.MAX_VALUE;
        for (int i = 0; i < maxEpochs; ++i) {
            Stopwatch sw = Stopwatch.createStarted();
            // compute distances
            centroids.forEach(centroid -> points.parallelStream().forEach(p -> distanceToCentroid(p, centroid)));
            // update clusters
            points.parallelStream().forEach(KmeansPoint::updateCluster);
            // compute loss
            double total = points.parallelStream().mapToDouble(p -> p.distanceToCentroid[p.clusterId]).sum();
            // compute means
            centroids.parallelStream().forEach(KmeansCentroid::reset);
            points.parallelStream().forEach(p -> centroids.get(p.clusterId).add(p));
            centroids.parallelStream().forEach(KmeansCentroid::mean);

            log.info("Iteration {} dist={}, time={}", i, new DecimalFormat("#.####").format(total / points.size()), sw);
            if (total >= previousTotal) {
                log.info("Terminating k-means due to no improvement in loss");
                break;
            }
            previousTotal = total;
            if (outputEvery > 0 && i % outputEvery == 0 && outputPath != null) {
                save(outputPath);
            }
        }
        log.info("K-means completed in {}, final average distance: {}", overallSw,
                new DecimalFormat("#.####").format(previousTotal / points.size()));
        if (outputPath != null) {
            log.info("Writing cluster file to {}", outputPath.getAbsolutePath());
            save(outputPath);
        }
    }

    private KmeansPoint<T> samplePoint(int cluster) {
        return VectorMathUtils.sample(points, p -> p.distanceToCentroid[cluster] * p.distanceToCentroid[cluster], random);
    }

    private void distanceToCentroid(KmeansPoint point, KmeansCentroid centroid) {
        float total = VectorMathUtils.dot(centroid.point, point.point);
        point.distanceToCentroid[centroid.clusterId] = 1 - total;
    }

    private abstract static class Point {

        @Getter
        float[] point;
        @Getter
        int clusterId;

        Point(float[] point, int clusterId) {
            this.point = point;
            this.clusterId = clusterId;
        }
    }

    static class KmeansCentroid extends Point {

        private int members = 0;

        KmeansCentroid(float[] point, int clusterId) {
            super(point, clusterId);
        }

        void reset() {
            point = new float[point.length];
            members = 0;
        }

        void mean() {
            VectorMathUtils.div(point, members);
            VectorMathUtils.normalize(point);
        }

        void add(KmeansPoint point) {
            members++;
            VectorMathUtils.add(this.point, point.point);
        }

        static KmeansCentroid centroid(KmeansPoint point, int id) {
            return new KmeansCentroid(VectorMathUtils.copy(point.point), id);
        }
    }

    public static class KmeansPoint<InputT> extends Point {

        @Getter
        InputT id;
        double[] distanceToCentroid;

        KmeansPoint(InputT id, float[] point, int kVal) {
            super(point, -1);
            this.id = id;
            this.distanceToCentroid = new double[kVal];
        }

        void updateCluster() {
            double min = Double.MAX_VALUE;
            int minIndex = -1;
            int index = 0;
            for (double dist : distanceToCentroid) {
                if (dist < min) {
                    min = dist;
                    minIndex = index;
                }
                ++index;
            }
            clusterId = minIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            KmeansPoint<?> that = (KmeansPoint<?>) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    /**
     * Output clusters as a TSV file, sorted by cluster and then alphabetically by ID.
     *
     * @param outputFile output file
     */
    public void save(File outputFile) {
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            points.stream()
                    .sorted(Comparator.comparing((KmeansPoint p) -> p.clusterId)
                            .thenComparing((KmeansPoint p) -> p.id.toString()))
                    .forEach(point -> writer.println(String.format("%d\t%s", point.clusterId, point.id.toString())));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error writing clusters to " + outputFile.getAbsolutePath(), e);
        }
    }

}
