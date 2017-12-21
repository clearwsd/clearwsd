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
