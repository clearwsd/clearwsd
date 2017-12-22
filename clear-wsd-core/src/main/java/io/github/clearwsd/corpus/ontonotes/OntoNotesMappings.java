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

package io.github.clearwsd.corpus.ontonotes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;


/**
 * OntoNotes sense inventory mapping.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class OntoNotesMappings implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @XmlElement(name = "gr_sense")
    protected String grSense;
    @XmlElement(name = "wn", required = true)
    protected List<OntoNotesWn> wordNetSenses = new ArrayList<>();
    @XmlElement(required = true)
    protected String omega;
    @XmlElement(required = true)
    protected String pb;
    protected String vn;
    protected String fn;

}
