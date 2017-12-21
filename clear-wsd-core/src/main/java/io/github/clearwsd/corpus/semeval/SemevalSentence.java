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
