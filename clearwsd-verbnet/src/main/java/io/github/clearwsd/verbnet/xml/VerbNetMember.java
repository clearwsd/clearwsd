package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Individual VerbNet class member.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VerbNetMember.ROOT_NAME)
public class VerbNetMember {

    static final String ROOT_NAME = "MEMBER";

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String name;

    @XmlAttribute(name = "wn", required = true)
    @XmlJavaTypeAdapter(WordNetKey.WordNetKeyAdapter.class)
    private List<WordNetKey> wn = new ArrayList<>();

    @XmlAttribute(name = "features")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String features = "";

    @XmlAttribute(name = "grouping")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String grouping = "";

    @XmlAttribute(name = "verbnet_key")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String verbnetKey = "";

    private VerbNetClass verbClass;

    public VerbNetClass verbClass() {
        return verbClass;
    }

}
