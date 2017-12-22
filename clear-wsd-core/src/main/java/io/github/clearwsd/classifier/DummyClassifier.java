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

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

/**
 * Single-label classifier.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class DummyClassifier implements SparseClassifier {

    private static final long serialVersionUID = 4110868818631078034L;
    private int label;

    @Override
    public Integer classify(SparseInstance instance) {
        return label;
    }

    @Override
    public Map<Integer, Double> score(SparseInstance instance) {
        return ImmutableMap.of(label, 1d);
    }

    @Override
    public void train(List<SparseInstance> train, List<SparseInstance> valid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Hyperparameter> hyperparameters() {
        return new ArrayList<>();
    }

    @Override
    public void load(ObjectInputStream inputStream) {
        try {
            label = inputStream.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(ObjectOutputStream outputStream) {
        try {
            outputStream.write(label);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
