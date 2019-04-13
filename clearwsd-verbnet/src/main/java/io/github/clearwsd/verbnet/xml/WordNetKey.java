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

package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * WordNet key for a particular lemma/synset.
 *
 * @author jgung
 */
@Getter
@EqualsAndHashCode
@Accessors(fluent = true)
@AllArgsConstructor
public class WordNetKey {

    private static final Pattern WN_KEY_REGEX = Pattern.compile("\\??(\\p{ASCII}+)%(\\d):(\\d+):(\\d+)(:\\p{ASCII}+:(\\d\\d))?");

    private String lemma;
    private SynsetType type;
    private int lexicalFileNumber;
    private int lexicalId;
    private boolean uncertain;

    public static class WordNetKeyAdapter extends XmlAdapter<String, List<WordNetKey>> {

        @Override
        public List<WordNetKey> unmarshal(String value) {
            if (null == value || value.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.stream(value.split("\\s+"))
                .map(WordNetKey::parseWordNetKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }

        @Override
        public String marshal(List<WordNetKey> value) {
            if (null == value) {
                return null;
            }
            return value.stream()
                .map(key -> String.format("%s%%%s:%d:%d", key.lemma, key.type.ordinal(), key.lexicalFileNumber, key.lexicalId))
                .collect(Collectors.joining(" "));
        }
    }

    public static Optional<WordNetKey> parseWordNetKey(@NonNull String key) {
        String trimmedKey = key.trim();
        Matcher matcher = WN_KEY_REGEX.matcher(trimmedKey);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String lemma = matcher.group(1);
        SynsetType type = SynsetType.values()[Math.max(Integer.parseInt(matcher.group(2)), SynsetType.OTHER.ordinal())];
        return Optional.of(new WordNetKey(lemma, type, Integer.parseInt(matcher.group(3)), Integer.parseInt(matcher.group(4)),
            key.startsWith("?")));
    }

    public enum SynsetType {
        NOUN,
        VERB,
        ADJECTIVE,
        ADVERB,
        ADJECTIVE_SATELLITE,
        OTHER
    }


}
