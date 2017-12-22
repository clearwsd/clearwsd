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

/**
 * Parameter/setting chosen prior to learning (not learned during training).
 *
 * @author jamesgung
 */
public interface Hyperparameter<T> {

    /**
     * Readable identifier.
     */
    String name();

    /**
     * Unique identifier.
     */
    String key();

    /**
     * Description of parameter.
     */
    String description();

    /**
     * Default value of parameter when none is provided.
     */
    String defaultValue();

    /**
     * Parse and assign the value to a model.
     *
     * @param model target model
     * @param value input value
     */
    void assignValue(T model, String value);

}
