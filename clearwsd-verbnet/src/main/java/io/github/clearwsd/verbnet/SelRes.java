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
 * VerbNet selectional restriction, which provides more information about the nature of a given thematic role.
 *
 * @author jgung
 */
public interface SelRes {

    /**
     * Type of selectional restriction, e.g. "concrete".
     */
    String type();

    /**
     * Value of selectional restriction, e.g. "+" or "-".
     */
    String value();

}
