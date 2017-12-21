package io.github.clearwsd.corpus.ontonotes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Getter;
import lombok.Setter;


/**
 * OntoNotes sense.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "sense")
public class OntoNotesSense implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @XmlAttribute(name = "n", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String number;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute(name = "group", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String group;
    protected String commentary;
    @XmlElement(required = true)
    protected String examples;
    @XmlElement(required = true)
    protected OntoNotesMappings mappings;
    @XmlElement(name = "SENSE_META", required = true)
    protected OntoNotesSenseMeta senseMetaData;

}
