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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Sparse vector implementation.
 *
 * @author jamesgung
 */
@Getter
@Setter
@AllArgsConstructor
@Accessors(fluent = true)
public class DefaultSparseVector implements SparseVector {

    private static final long serialVersionUID = -3910126278417663876L;

    private int[] indices;

    private float[] data;

    @Override
    public float l2() {
        float sum = 0;
        for (int i = 0; i < indices.length; ++i) {
            sum += data[i] * data[i];
        }
        return (float) Math.sqrt(sum);
    }
}
