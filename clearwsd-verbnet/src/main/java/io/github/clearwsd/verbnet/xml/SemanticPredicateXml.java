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

import io.github.clearwsd.verbnet.SemanticArgument;
import io.github.clearwsd.verbnet.SemanticPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * XML binding implementation of {@link SemanticPredicate}.
 *
 * @author jgung
 */
@Data
@Slf4j
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SemanticPredicateXml.ROOT_NAME)
public class SemanticPredicateXml implements SemanticPredicate {

    static final String ROOT_NAME = "PRED";

    @XmlAttribute(name = "bool")
    private String bool = "";

    @XmlAttribute(name = "value", required = true)
    private String value;

    @XmlElementWrapper(name = "ARGS")
    @XmlElement(name = SemanticArgumentXml.ROOT_NAME, required = true)
    private List<SemanticArgumentXml> args = new ArrayList<>();

    @Override
    public List<SemanticArgument> semanticArguments() {
        return args.stream().map(arg -> (SemanticArgument) arg).collect(Collectors.toList());
    }
}
