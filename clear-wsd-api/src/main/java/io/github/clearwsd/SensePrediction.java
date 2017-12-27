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

package io.github.clearwsd;

/**
 * Sense prediction.
 *
 * @param <T> sense inventory item
 * @author jamesgung
 */
public interface SensePrediction<T> {

    /**
     * Return the word index for this sense prediction.
     */
    int index();

    /**
     * Return the original text of the input word.
     */
    String originalText();

    /**
     * Return the predicted sense's unique identifier.
     */
    String id();

    /**
     * Return the sense inventory entry corresponding to the prediction.
     */
    T sense();

}
