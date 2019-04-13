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

import java.util.List;

/**
 * Collection of {@link SyntRes} linked by a given logical relation.
 *
 * @author jamesgung
 */
public interface SyntResDescription {

    /**
     * Logical relation, e.g. "and" or "or".
     */
    String logic();

    /**
     * Collection of syntactic restrictions linked by the logical relation, {@link SyntResDescription#logic()}.
     */
    List<SyntRes> restrictions();
}
