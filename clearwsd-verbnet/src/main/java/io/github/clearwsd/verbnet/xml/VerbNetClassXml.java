package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.ThematicRole;
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.VerbNetFrame;
import io.github.clearwsd.verbnet.VerbNetId;
import io.github.clearwsd.verbnet.VerbNetMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link VerbNetClass}.
 *
 * @author jgung
 */
@Data
@EqualsAndHashCode(of = "verbNetId")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VerbNetClassXml.ROOT_NAME)
public class VerbNetClassXml implements VerbNetClass {

    static final String ROOT_NAME = "VNCLASS";

    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(VerbNetIdXmlAdapter.class)
    private VerbNetId verbNetId;

    @XmlElementWrapper(name = "MEMBERS")
    @XmlElement(name = VerbNetMemberXml.ROOT_NAME, required = true)
    private List<VerbNetMemberXml> memberElements = new ArrayList<>();

    @XmlElementWrapper(name = "THEMROLES")
    @XmlElement(name = VerbNetThematicRoleXml.ROOT_NAME, required = true)
    private List<VerbNetThematicRoleXml> thematicRoles = new ArrayList<>();

    @XmlElementWrapper(name = "FRAMES")
    @XmlElement(name = VerbNetFrameXml.ROOT_NAME, required = true)
    private List<VerbNetFrameXml> frameElements = new ArrayList<>();

    @XmlElementWrapper(name = "SUBCLASSES")
    @XmlElement(name = "VNSUBCLASS", required = true)
    private List<VerbNetClassXml> children = new ArrayList<>();

    private transient VerbNetClass parentClass;

    @Override
    public List<VerbNetMember> members() {
        return memberElements.stream()
            .map(member -> (VerbNetMember) member)
            .collect(Collectors.toList());
    }

    @Override
    public List<ThematicRole> roles() {
        return thematicRoles.stream()
            .map(role -> (ThematicRole) role)
            .collect(Collectors.toList());
    }

    @Override
    public List<VerbNetFrame> frames() {
        return frameElements.stream()
            .map(frame -> (VerbNetFrame) frame)
            .collect(Collectors.toList());
    }

    @Override
    public List<VerbNetClass> subclasses() {
        return children.stream()
            .map(cls -> (VerbNetClass) cls)
            .collect(Collectors.toList());
    }

    public Optional<VerbNetClass> parentClass() {
        return Optional.ofNullable(parentClass);
    }

    public static class VerbNetIdXmlAdapter extends XmlAdapter<String, VerbNetId> {

        @Override
        public VerbNetId unmarshal(String value) {
            if (null == value) {
                return null;
            }
            return VerbNetId.parse(value);
        }

        @Override
        public String marshal(VerbNetId value) {
            if (null == value) {
                return null;
            }
            return value.toString();
        }
    }

}
