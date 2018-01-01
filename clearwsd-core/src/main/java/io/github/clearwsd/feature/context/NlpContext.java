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

package io.github.clearwsd.feature.context;

import java.util.Collections;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Context over tokens used in feature extraction.
 *
 * @author jamesgung
 */
@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class NlpContext<T extends NlpInstance> {

    private String identifier;
    private List<T> tokens;

    public NlpContext(String identifier, T token) {
        this.identifier = identifier;
        this.tokens = Collections.singletonList(token);
    }

}
