package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.SemanticArgument;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link SemanticArgument}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SemanticArgumentXml.ROOT_NAME)
public class SemanticArgumentXml implements SemanticArgument {

    static final String ROOT_NAME = "ARG";

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlAttribute(name = "value", required = true)
    private String value;

}
