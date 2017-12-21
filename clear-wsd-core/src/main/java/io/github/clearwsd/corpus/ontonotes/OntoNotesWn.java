
package io.github.clearwsd.corpus.ontonotes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Getter;
import lombok.Setter;


/**
 * OntoNotes WordNet.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "wn")
public class OntoNotesWn implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlAttribute(name = "lemma")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lemma;
    @XmlValue
    protected String value;

}
