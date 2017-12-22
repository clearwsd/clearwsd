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

package io.github.clearwsd.corpus.semeval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sentence")
public class SemevalSentence extends SemevalElement implements Iterable<SemevalWordForm> {

    @Getter
    @XmlElements({
            @XmlElement(name = "wf", type = SemevalWordForm.class),
            @XmlElement(name = "instance", type = SemevalInstance.class)
    })
    private List<SemevalWordForm> elements = new ArrayList<>();
    @Getter
    @Setter
    @XmlAttribute(name = "id", required = true)
    private String id;

    public List<SemevalInstance> getInstances() {
        return elements.stream()
                .filter(e -> e instanceof SemevalInstance)
                .map(e -> (SemevalInstance) e)
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<SemevalWordForm> iterator() {
        return elements.iterator();
    }

}
