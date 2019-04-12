package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet syntactic restrictions.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SyntacticRestrictions.ROOT_NAME)
public class SyntacticRestrictions {

    static final String ROOT_NAME = "SYNRESTRS";

    @XmlAttribute(name = "logic")
    private String logic = "";

    @XmlElement(name = SyntacticRestriction.ROOT_NAME)
    private List<SyntacticRestriction> syntacticRestrictions = new ArrayList<>();

}
