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
 * Penn Treebank part-of-speech (POS) tag utilities.
 *
 * @author jamesgung
 */
public class PosUtils {

    private PosUtils() {
    }

    public static boolean isNoun(String pos) {
        return pos.startsWith("NN") || pos.equals("PRP") || pos.equals("WP");
    }

    public static boolean isVerb(String pos) {
        return pos.startsWith("VB");
    }

    public static boolean isAdjective(String pos) {
        return pos.startsWith("JJ");
    }

    public static boolean isAdverb(String pos) {
        return pos.startsWith("RB") || pos.equals("WRB");
    }

}
