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

import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.VerbNetMember;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link VerbNetMember}.
 *
 * @author jgung
 */
@Getter
@Setter
@ToString(of = "name")
@EqualsAndHashCode
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VerbNetMemberXml.ROOT_NAME)
public class VerbNetMemberXml implements VerbNetMember {

    static final String ROOT_NAME = "MEMBER";

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String name;

    @XmlAttribute(name = "wn", required = true)
    @XmlJavaTypeAdapter(WordNetKey.WordNetKeyAdapter.class)
    private List<WordNetKey> wn = new ArrayList<>();

    @XmlAttribute(name = "features")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String features = "";

    @XmlAttribute(name = "grouping")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String grouping = "";

    @XmlAttribute(name = "verbnet_key")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String verbnetKey = "";

    private transient VerbNetClass verbClass;

    public VerbNetClass verbClass() {
        return verbClass;
    }

}
