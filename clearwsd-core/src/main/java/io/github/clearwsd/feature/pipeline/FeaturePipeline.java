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

package io.github.clearwsd.feature.pipeline;

import java.io.Serializable;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import io.github.clearwsd.classifier.SparseInstance;
import io.github.clearwsd.feature.model.FeatureModel;

/**
 * Trainable feature pipeline.
 *
 * @param <I> input instance type
 * @author jamesgung
 */
public interface FeaturePipeline<I extends NlpInstance> extends Serializable {

    /**
     * Return the model associated with this feature pipeline.
     */
    FeatureModel model();

    /**
     * Compute features for a single instance (used at test time).
     *
     * @param inputInstance input instance
     * @return output instance (input to a classification algorithm)
     */
    SparseInstance process(I inputInstance);

    /**
     * Extract features and perform any necessary training-specific processing.
     *
     * @param instances list of input training instances
     * @return list of training instances (input to a classification algorithm)
     */
    List<SparseInstance> train(List<I> instances);

}
