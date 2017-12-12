package edu.colorado.clear.wsd.corpus.semeval;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "instance")
@NoArgsConstructor
class SemevalInstance extends SemevalWordForm {

    @XmlAttribute(name = "id", required = true)
    private String id;

    SemevalInstance(String value) {
        super(value);
    }
}
