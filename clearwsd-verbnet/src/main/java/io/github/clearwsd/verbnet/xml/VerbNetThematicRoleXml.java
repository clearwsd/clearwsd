package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.ThematicRole;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link ThematicRole}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = VerbNetThematicRoleXml.ROOT_NAME)
public class VerbNetThematicRoleXml implements ThematicRole {

    static final String ROOT_NAME = "THEMROLE";

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlElement(name = SelectionalRestrictionsXml.ROOT_NAME, required = true)
    private SelectionalRestrictionsXml restrictions;

}
