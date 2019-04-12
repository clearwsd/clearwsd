package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Individual VerbNet class.
 *
 * @author jgung
 */
@Data
@EqualsAndHashCode(of = "id")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VNCLS")
public class VerbNetClass {

    @XmlAttribute(name = "ID", required = true)
    private String id;

    @XmlElementWrapper(name = "MEMBERS")
    @XmlElement(name = "MEMBER", required = true)
    private List<VerbNetMember> members = new ArrayList<>();

    @XmlElementWrapper(name = "THEMROLES")
    @XmlElement(name = "THEMROLE", required = true)
    private List<VerbNetThematicRole> thematicRoles = new ArrayList<>();

    @XmlElementWrapper(name = "FRAMES")
    @XmlElement(name = "FRAME", required = true)
    private List<VerbNetFrame> frames = new ArrayList<>();

    @XmlElementWrapper(name = "SUBCLASSES")
    @XmlElement(name = "VNSUBCLASS", required = true)
    private List<VerbNetClass> subclasses = new ArrayList<>();

    private VerbNetClass parentClass;

    public Optional<VerbNetClass> parentClass() {
        return Optional.ofNullable(parentClass);
    }

}
