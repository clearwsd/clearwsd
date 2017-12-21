package io.github.clearwsd.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import lombok.extern.slf4j.Slf4j;

import static io.github.clearwsd.utils.VectorMathUtils.normalize;

/**
 * Utility class containing common IO-related methods for handling embeddings.
 *
 * @author jamesgung
 */
@Slf4j
public final class EmbeddingIoUtils {

    private EmbeddingIoUtils() {
        throw new AssertionError("Utility class, should never be instantiated.");
    }

    /**
     * Read a space-separated file containing a list of vectors, with the first column of each row corresponding to an identifier,
     * and the rest of the columns corresponding to the values of the vector associated with the identifier. Handles text files
     * by default, but supports GZIP files as well, provided the path ends with ".gz".
     *
     * @param path      path to vector file
     * @param limit     maximum number of embeddings to read
     * @param normalize normalize vectors w/ l2 norm
     * @return map from identifiers to corresponding embeddings
     */
    public static Map<String, float[]> readVectors(String path, int limit, boolean normalize) {
        try (InputStream inputStream = path.endsWith(".gz") ? new GZIPInputStream(new FileInputStream(path))
                : new FileInputStream(path)) {
            return readVectors(inputStream, limit, normalize);
        } catch (IOException e) {
            throw new RuntimeException("Error loading vectors at " + path, e);
        }
    }

    /**
     * Read a space-separated file containing a list of vectors, with the first column of each row corresponding to an identifier,
     * and the rest of the columns corresponding to the values of the vector associated with the identifier.
     *
     * @param inputStream vector input stream
     * @param limit       maximum number of embeddings to read
     * @param normalize   normalize vectors w/ l2 norm
     * @return map from identifiers to corresponding embeddings
     */
    public static Map<String, float[]> readVectors(InputStream inputStream, int limit, boolean normalize) {
        Map<String, float[]> vectors = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (limit > 0 && vectors.size() >= limit) {
                    break;
                }
                String[] fields = line.split(" ");
                if (fields.length <= 2) {
                    continue;
                }
                float[] result = new float[fields.length - 1];
                for (int i = 0; i < result.length; ++i) {
                    result[i] = Float.parseFloat(fields[i + 1]);
                }
                if (normalize) {
                    VectorMathUtils.normalize(result);
                }
                vectors.put(fields[0], result);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading vectors: " + e.getMessage(), e);
        }
        log.debug("Read {} vectors.", vectors.size());
        return vectors;
    }

    /**
     * Binarize embeddings as described in "Revisiting Embedding Features for Simple Semi-supervised Learning" (Guo et al. 2014).
     * Output is a map of indices, where negative-valued indices are negative, and positive-valued indices are positive. Indices
     * start at 1, so as to avoid loss of information on 0.
     *
     * @param embeddings map from identifiers onto corresponding vectors
     * @return map from identifiers onto indices
     */
    public static Multimap<String, Integer> binarize(Map<String, float[]> embeddings) {
        float[] posMean = filteredMean(embeddings.values(), v -> v >= 0);
        float[] negMean = filteredMean(embeddings.values(), v -> v < 0);
        Multimap<String, Integer> binarizedEmbeddings = HashMultimap.create();
        for (Map.Entry<String, float[]> embedding : embeddings.entrySet()) {
            int index = 0;
            for (float val : embedding.getValue()) {
                if (val > posMean[index]) {
                    binarizedEmbeddings.put(embedding.getKey(), -(index + 1));
                } else if (val < negMean[index]) {
                    binarizedEmbeddings.put(embedding.getKey(), index + 1);
                }
                ++index;
            }
        }
        return binarizedEmbeddings;
    }

    private static float[] filteredMean(Collection<float[]> vectors, Predicate<Float> valueFilter) {
        float[] mean = new float[vectors.iterator().next().length];
        int[] counts = new int[mean.length];
        for (int index = 0; index < mean.length; ++index) {
            for (float[] vector : vectors) {
                float val = vector[index];
                if (valueFilter.test(val)) {
                    mean[index] += val;
                    counts[index]++;
                }
            }
        }
        for (int i = 0; i < mean.length; ++i) {
            mean[i] /= counts[i];
        }
        return mean;
    }

    /**
     * Output string multimap to a given file in TSV format (identifier followed by tab,
     * followed by a tab-separated list of values).
     *
     * @param multimap   multimap with string keys
     * @param outputFile output file
     * @param <T>        multimap value type
     */
    public static <T> void printMultimapTsv(Multimap<String, T> multimap, File outputFile) {
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            for (Map.Entry<String, Collection<T>> values : multimap.asMap().entrySet()) {
                writer.println(values.getKey() + "\t" + values.getValue().stream()
                        .sorted()
                        .map(Object::toString)
                        .collect(Collectors.joining("\t")));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while writing multimap: " + e.getMessage(), e);
        }
    }

}
