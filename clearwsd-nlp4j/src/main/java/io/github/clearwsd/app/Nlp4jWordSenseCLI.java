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

package io.github.clearwsd.app;

import io.github.clearwsd.parser.Nlp4jDependencyParser;
import io.github.clearwsd.parser.NlpParser;

/**
 * Word sense classifier CLI with {@link io.github.clearwsd.parser.Nlp4jDependencyParser}.
 *
 * @author jamesgung
 */
public class Nlp4jWordSenseCLI extends WordSenseCLI {

    private Nlp4jWordSenseCLI(String[] args) {
        super(args);
    }

    @Override
    protected NlpParser parser() {
        return new Nlp4jDependencyParser();
    }

    public static void main(String[] args) {
        new Nlp4jWordSenseCLI(args).run();
    }

}
