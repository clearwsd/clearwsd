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

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Utility class for performing common linear algebraic operations on float arrays. Most operations are unsafe (do not provide
 * checks for length of vectors, or division by zero).
 *
 * @author jamesgung
 */
public final class VectorMathUtils {

    private VectorMathUtils() {
        throw new AssertionError("Utility class, should never be instantiated.");
    }

    /**
     * Compute the Euclidean (l2) norm of a given vector.
     *
     * @param vector input vector
     * @return euclidean norm
     */
    public static float euclideanNorm(float[] vector) {
        float sum = 0;
        for (float val : vector) {
            sum += val * val;
        }
        return (float) Math.sqrt(sum);
    }

    /**
     * Normalize a vector in place with its L2 norm.
     *
     * @param vector input vector
     */
    public static void normalize(float[] vector) {
        float l2 = euclideanNorm(vector);
        for (int i = 0; i < vector.length; ++i) {
            vector[i] /= l2;
        }
    }

    /**
     * Compute the dot (inner) product between two equal-length vectors.
     *
     * @param v1 first vector
     * @param v2 second vector
     * @return dot product
     */
    public static float dot(float[] v1, float[] v2) {
        float sum = 0;
        int index = 0;
        for (float v2Val : v2) {
            sum += v1[index++] * v2Val;
        }
        return sum;
    }

    /**
     * Divide a vector in place by a specified constant.
     *
     * @param vector input vector
     * @param denom  constant denominator
     */
    public static void div(float[] vector, float denom) {
        int index = 0;
        for (float val : vector) {
            vector[index++] = val / denom;
        }
    }

    /**
     * Add a vector to another vector in place.
     *
     * @param target   resulting vector and first summand
     * @param addition vector to be added
     */
    public static void add(float[] target, float[] addition) {
        int index = 0;
        for (float val : addition) {
            target[index++] += val;
        }
    }

    /**
     * Make a copy of a float array.
     *
     * @param original original array
     * @return copied array
     */
    public static float[] copy(float[] original) {
        float[] copy = new float[original.length];
        System.arraycopy(original, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Sample an input with probability proportional to a real (positive) value associated with that input.
     *
     * @param inputs        inputs to sample from
     * @param valueFunction function to get value used to compute probability distribution
     * @param random        random number generator used for sampling
     * @param <T>           input type
     * @return random sample
     */
    public static <T> T sample(List<T> inputs, Function<T, Double> valueFunction, Random random) {
        double total = inputs.stream().mapToDouble(valueFunction::apply).sum();
        double probability = 0;
        double sample = random.nextDouble();
        for (T input : inputs) {
            probability += valueFunction.apply(input) / total;
            if (sample < probability) {
                return input;
            }
        }
        return inputs.get(inputs.size() - 1);
    }

    /**
     * Sample a value uniformly.
     *
     * @param inputs possible values
     * @param random random number generator
     * @param <T>    value type
     * @return sampled value
     */
    public static <T> T sampleUniform(List<T> inputs, Random random) {
        return inputs.get(random.nextInt(inputs.size()));
    }

}
