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

package io.github.clearwsd.classifier;

import java.io.Serializable;

/**
 * A compact representation for one-dimensional arrays where most of the values are zero. Contains indices and associated values for
 * every non-zero value.
 *
 * @author jamesgung
 */
public interface SparseVector extends Serializable {

    /**
     * Indices corresponding to each value in the vector, in ascending order and with no duplicates.
     */
    int[] indices();

    /**
     * Sparse vector of non-zero values.
     */
    float[] data();

    float l2();

}
