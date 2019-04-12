package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet frame description.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DESCRIPTION")
public class FrameDescription {

    @XmlAttribute(name = "primary", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String primary;

    @XmlAttribute(name = "secondary")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String secondary = "";

    @XmlAttribute(name = "descriptionNumber", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String descriptionNumber;

    @XmlAttribute(name = "xtag", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String xtag;

}
