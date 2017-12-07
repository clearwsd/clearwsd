package edu.colorado.clear.wsd.utils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.util.Map;

import static edu.colorado.clear.wsd.utils.EmbeddingIoUtils.readVectors;

/**
 * K-means clustering CLI. Performs K-means clustering on input word embedding files.
 *
 * @author jamesgung
 */
public class KMeansCalculator {

    @Parameter(names = "-k", description = "Number of clusters (k)", required = true)
    private int kVal;
    @Parameter(names = {"-inputPath", "-i"}, description = "Input path to vector file", required = true)
    private String inputPath;
    @Parameter(names = {"-outputPath", "-o"}, description = "Output cluster path")
    private String outputPath;
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
        if (outputPath == null) {
            outputPath = new File(String.format("%s.%d.txt", inputPath, kVal)).getAbsolutePath();
        }
        Map<String, float[]> vectors = readVectors(inputPath, limitPoints, true);
        new KMeans<>(seed, kVal, vectors)
                .outputPath(new File(outputPath))
                .outputEvery(outputEvery)
                .maxEpochs(maxEpochs)
                .kMeansPlusPlus(!randomInitialization)
                .run();
    }

    public static void main(String... args) {
        new KMeansCalculator(args).run();
    }

}
