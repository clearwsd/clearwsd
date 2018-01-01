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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import lombok.Getter;
import lombok.Setter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "corpus")
public class SemevalCorpus {

    @Getter
    @XmlElements({
            @XmlElement(name = "text", type = SemevalText.class),
            @XmlElement(name = "sentence", type = SemevalSentence.class),
            @XmlElement(name = "wf", type = SemevalWordForm.class),
            @XmlElement(name = "instance", type = SemevalInstance.class)
    })
    private List<SemevalElement> elements = new ArrayList<>();
    @Getter
    @Setter
    @XmlAttribute(name = "lang")
    private String lang;
    @Getter
    @Setter
    @XmlAttribute(name = "source")
    private String source;
    @Getter
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();

    public List<SemevalSentence> getSentences() {
        return elements.stream()
                .filter(e -> e instanceof SemevalSentence)
                .map(e -> (SemevalSentence) e)
                .collect(Collectors.toList());
    }

    public List<SemevalText> getTexts() {
        return elements.stream()
                .filter(e -> e instanceof SemevalText)
                .map(e -> (SemevalText) e)
                .collect(Collectors.toList());
    }

    public List<SemevalInstance> getInstances() {
        return elements.stream()
                .filter(e -> e instanceof SemevalInstance)
                .map(e -> (SemevalInstance) e)
                .collect(Collectors.toList());
    }

    /**
     * Return all {@link SemevalSentence sentences} in the corpus.
     */
    public List<SemevalSentence> getAllSentences() {
        List<SemevalSentence> sentences = getSentences();
        for (SemevalText text : getTexts()) {
            sentences.addAll(text.getSentences());
        }
        return sentences;
    }

}
