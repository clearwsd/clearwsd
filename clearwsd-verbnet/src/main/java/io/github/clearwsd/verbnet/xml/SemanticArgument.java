package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Verbnet semantic predicate argument.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SemanticArgument.ROOT_NAME)
public class SemanticArgument {

    static final String ROOT_NAME = "ARG";

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlAttribute(name = "value", required = true)
    private String value;

}
