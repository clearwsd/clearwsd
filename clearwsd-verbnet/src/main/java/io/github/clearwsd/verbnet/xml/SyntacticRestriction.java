package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet syntactic restriction.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SyntacticRestriction.ROOT_NAME)
public class SyntacticRestriction {

    static final String ROOT_NAME = "SYNRESTR";

    @XmlAttribute(name = "type", required = true)
    private String type;
    @XmlAttribute(name = "Value", required = true)
    private String value;

}
