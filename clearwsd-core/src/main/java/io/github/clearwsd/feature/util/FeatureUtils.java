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

package io.github.clearwsd.feature.util;

/**
 * Feature extraction constants and utilities.
 *
 * @author jamesgung
 */
public class FeatureUtils {

    // separator for parts of feature extractor IDs
    public static final String KEY_DELIM = ".";
    // separator for concatenated feature values
    public static final String CONCAT_DELIM = "|";
    // separator for combing feature/context keys
    public static final String CONTEXT_FEATURE_SEP = "::";
    // separator for concatenated features from multiple contexts
    public static final String CONTEXT_DELIM = "__";
    // separator between feature ID and feature value
    public static final String FEATURE_ID_SEP = "=";

    public static String computeId(String contextId, String featureId) {
        return contextId + CONTEXT_FEATURE_SEP + featureId;
    }

}
