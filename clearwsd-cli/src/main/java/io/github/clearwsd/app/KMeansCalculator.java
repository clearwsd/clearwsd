/*
 * Copyright (C) 2017  James Gung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.clearwsd.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.github.clearwsd.utils.KMeans;

import static io.github.clearwsd.utils.EmbeddingIoUtils.readVectors;

/**
 * K-means clustering CLI. Performs K-means clustering on input word embedding files.
 *
 * @author jamesgung
 */
public class KMeansCalculator {

    @Parameter(names = "-k", description = "Number of clusters (k) -- can provide comma separated list for multiple granularities",
            required = true)
    private List<Integer> kVals;
    @Parameter(names = {"-inputPath", "-i"}, description = "Input path to vector file", required = true)
    private String inputPath;
    @Parameter(names = {"-outputExt", "-o"}, description = "Output extension")
    private String outputExt = "txt";
    @Parameter(names = {"-maxIter"}, description = "Maximum number of iterations")
    private int maxEpochs = 100;
    @Parameter(names = "-outputEvery", description = "Output clusters every x iterations")
    private int outputEvery = 10;
    @Parameter(names = "-seed", description = "RNG seed")
    private int seed = 0;
    @Parameter(names = "-limit", description = "Maximum number of points to cluster")
    private int limitPoints = -1;
    @Parameter(names = "--uniformInit", description = "Initialize centroids uniformly at random")
    private boolean randomInitialization = false;

    private KMeansCalculator(String... args) {
        JCommander cmd = new JCommander(this);
        cmd.setProgramName(this.getClass().getSimpleName());
        try {
            cmd.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }
    }

    private void run() {
        for (int kVal : kVals) {
            String outputPath = new File(String.format("%s.%d.%s", inputPath, kVal, outputExt)).getAbsolutePath();
            Map<String, float[]> vectors = readVectors(inputPath, limitPoints, true);
            new KMeans<>(seed, kVal, vectors)
                    .outputPath(new File(outputPath))
                    .outputEvery(outputEvery)
                    .maxEpochs(maxEpochs)
                    .kMeansPlusPlus(!randomInitialization)
                    .run();
        }
    }

    public static void main(String... args) {
        new KMeansCalculator(args).run();
    }

}
