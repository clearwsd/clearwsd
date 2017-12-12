package edu.colorado.clear.wsd.corpus.semeval;

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
