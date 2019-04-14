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

import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnMember;
import io.github.clearwsd.verbnet.WnKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link VnMember}.
 *
 * @author jgung
 */
@Getter
@Setter
@ToString(of = "name")
@EqualsAndHashCode
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VnMemberXml.ROOT_NAME)
public class VnMemberXml implements VnMember {

    static final String ROOT_NAME = "MEMBER";

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String name;

    @XmlAttribute(name = "wn", required = true)
    @XmlJavaTypeAdapter(WordNetKeyAdapter.class)
    private List<WnKey> wn = new ArrayList<>();

    @XmlAttribute(name = "features")
    @XmlJavaTypeAdapter(ValueSetAdapter.class)
    private List<String> features;

    @XmlAttribute(name = "grouping")
    @XmlJavaTypeAdapter(GroupingsSetAdapter.class)
    private List<String> groupings;

    @XmlAttribute(name = "verbnet_key")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String verbnetKey = "";

    private transient VnClass verbClass;

    public VnClass verbClass() {
        return verbClass;
    }

    public static class ValueSetAdapter extends XmlAdapter<String, List<String>> {

        @Override
        public List<String> unmarshal(String value) {
            return Arrays.stream(value.split("\\s+"))
                .map(feature -> feature.replaceFirst("\\+", "").trim().toLowerCase())
                .distinct()
                .collect(Collectors.toList());

        }

        @Override
        public String marshal(List<String> value) {
            return value.stream().map(val -> "+" + val).collect(Collectors.joining(" "));
        }
    }

    public static class GroupingsSetAdapter extends XmlAdapter<String, List<String>> {

        @Override
        public List<String> unmarshal(String value) {
            return Arrays.stream(value.split("\\s+"))
                .distinct()
                .collect(Collectors.toList());

        }

        @Override
        public String marshal(List<String> value) {
            return String.join(" ", value);
        }
    }

    public static class WordNetKeyAdapter extends XmlAdapter<String, List<WnKey>> {

        @Override
        public List<WnKey> unmarshal(String value) {
            if (null == value || value.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.stream(value.split("\\s+"))
                .map(WnKey::parseWordNetKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        }

        @Override
        public String marshal(List<WnKey> value) {
            if (null == value) {
                return null;
            }
            return value.stream()
                .map(key -> String.format("%s%%%s:%d:%d", key.lemma(), key.type().ordinal(), key.lexicalFileNumber(),
                    key.lexicalId()))
                .collect(Collectors.joining(" "));
        }
    }

}
