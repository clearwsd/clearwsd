package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet semantic predicate.
 *
 * @author jgung
 */
@Data
@Slf4j
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SemanticPredicate.ROOT_NAME)
public class SemanticPredicate {

    static final String ROOT_NAME = "PRED";

    @XmlAttribute(name = "bool")
    private String bool = "";

    @XmlAttribute(name = "value", required = true)
    private String value;

    @XmlElementWrapper(name = "ARGS")
    @XmlElement(name = SemanticArgument.ROOT_NAME, required = true)
    private List<SemanticArgument> semanticArguments = new ArrayList<>();

}
