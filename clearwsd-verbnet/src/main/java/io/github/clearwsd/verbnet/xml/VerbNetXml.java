package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Collection of VerbNetXml classes.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "VERBNET")
public class VerbNetXml {

    @XmlElement(name = VerbNetClassXml.ROOT_NAME, required = true)
    private List<VerbNetClassXml> classes = new ArrayList<>();

}
