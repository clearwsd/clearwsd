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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "wf")
@NoArgsConstructor
class SemevalWordForm extends SemevalElement {

    @Getter
    @Setter
    @XmlAttribute(name = "lemma", required = true)
    private String lemma;
    @Getter
    @Setter
    @XmlAttribute(name = "pos", required = true)
    private String pos;

    @Getter
    @Setter
    @XmlAttribute(name = "slemma")
    private String predictedLemma;
    @Getter
    @Setter
    @XmlAttribute(name = "spos")
    private String predictedPos;
    @Getter
    @Setter
    @XmlAttribute(name = "sdep")
    private String dep;
    @Getter
    @Setter
    @XmlAttribute(name = "shead")
    private String head;

    SemevalWordForm(String value) {
        super(value);
    }
}
