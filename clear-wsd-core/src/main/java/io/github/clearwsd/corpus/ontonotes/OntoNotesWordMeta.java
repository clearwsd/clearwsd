
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
 * OntoNotes sense inventory word metadata.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "WORD_META")
public class OntoNotesWordMeta implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @XmlAttribute(name = "authors", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String authors;
    @XmlAttribute(name = "sample_score")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String sampleScore;

}
