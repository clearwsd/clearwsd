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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Abstract SemEval element.
 *
 * @author jamesgung
 */
@NoArgsConstructor
public abstract class SemevalElement {

    @Getter
    @XmlValue
    protected String value;
    @Getter
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();

    SemevalElement(String value) {
        this.value = value;
    }

}
