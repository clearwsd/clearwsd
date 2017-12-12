package edu.colorado.clear.wsd.corpus.semeval;

import java.util.ArrayList;
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
@XmlRootElement(name = "text")
public class SemevalText extends SemevalElement {

    @Getter
    @XmlElements({
            @XmlElement(name = "sentence", type = SemevalSentence.class),
            @XmlElement(name = "wf", type = SemevalWordForm.class),
            @XmlElement(name = "instance", type = SemevalInstance.class)
    })
    private List<SemevalElement> elements = new ArrayList<>();

    @Getter
    @Setter
    @XmlAttribute(name = "id", required = true)
    private String id;

    public List<SemevalSentence> getSentences() {
        return getElements().stream()
                .filter(e -> e instanceof SemevalSentence)
                .map(e -> (SemevalSentence) e)
                .collect(Collectors.toList());
    }

}
