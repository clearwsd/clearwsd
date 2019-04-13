/*
 * Copyright 2019 James Gung
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

package io.github.clearwsd.verbnet;

/**
 * VerbNet frame description.
 *
 * @author jgung
 */
public interface FrameDescription {

    /**
     * Returns the primary type for this frame, e.g. "NP.attribute V".
     */
    String primary();

    /**
     * Returns the secondary type for this frame, e.g. "Intransitive; Attribute Subject".
     */
    String secondary();

    /**
     * Returns the description number for this frame, e.g. "2.13.5".
     */
    String descriptionNumber();

    /**
     * Returns the xtag for this frame, e.g. "0.2".
     */
    String xtag();

}
