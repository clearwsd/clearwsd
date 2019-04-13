package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.SelRes;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link SelRes}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SelectionalRestrictionXml.ROOT_NAME)
public class SelectionalRestrictionXml implements SelRes {

    static final String ROOT_NAME = "SELRESTR";

    @XmlAttribute(name = "type", required = true)
    private String type;
    @XmlAttribute(name = "Value", required = true)
    private String value;

}
