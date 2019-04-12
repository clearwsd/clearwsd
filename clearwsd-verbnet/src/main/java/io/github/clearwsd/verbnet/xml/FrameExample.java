package io.github.clearwsd.verbnet.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet class frame example.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = FrameExample.ROOT_NAME)
public class FrameExample {

    static final String ROOT_NAME = "EXAMPLE";

    @XmlValue
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String value = "";

}
