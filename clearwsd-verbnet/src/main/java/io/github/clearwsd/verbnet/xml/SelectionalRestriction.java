package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet selectional restriction.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SelectionalRestriction.ROOT_NAME)
public class SelectionalRestriction {

    static final String ROOT_NAME = "SELRESTR";

    @XmlAttribute(name = "type", required = true)
    private String type;
    @XmlAttribute(name = "Value", required = true)
    private String value;

}
