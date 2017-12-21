package io.github.clearwsd.corpus.ontonotes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Getter;
import lombok.Setter;


/**
 * OntoNotes sense metadata.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SENSE_META")
public class OntoNotesSenseMeta implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @Getter
    @Setter
    @XmlAttribute(name = "clarity")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String clarity;

}
