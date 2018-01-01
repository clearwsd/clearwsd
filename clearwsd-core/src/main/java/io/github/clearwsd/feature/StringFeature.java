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

package io.github.clearwsd.feature;

import java.io.Serializable;

import io.github.clearwsd.feature.util.FeatureUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import static io.github.clearwsd.feature.util.FeatureUtils.FEATURE_ID_SEP;

/**
 * String feature, containing an ID and a value.
 *
 * @author jamesgung
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class StringFeature implements Serializable {

    private static final long serialVersionUID = -4634062576141187574L;

    private String id;
    private String value;

    @Override
    public String toString() {
        return id + FeatureUtils.FEATURE_ID_SEP + value;
    }
}
