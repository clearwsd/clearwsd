package io.github.clearwsd.corpus.ontonotes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
 * OntoNotes sense inventory.
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "inventory")
public class OntoNotesInventory implements Serializable {

    private static final long serialVersionUID = -4107201403356086663L;

    @XmlAttribute(name = "lemma", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String lemma;
    @XmlElement(name = "sense", required = true)
    protected List<OntoNotesSense> senses = new ArrayList<>();
    protected String commentary;
    @XmlElement(name = "WORD_META", required = true)
    protected OntoNotesWordMeta wordMeta;

}
