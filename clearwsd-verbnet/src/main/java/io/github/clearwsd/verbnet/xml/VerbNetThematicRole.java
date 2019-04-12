package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet thematic role.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "THEMROLE")
public class VerbNetThematicRole {

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlElement(name = "SELRESTRS", required = true)
    private SelectionalRestrictions selectionalRestrictions;

}
