package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.SemanticArgument;
import io.github.clearwsd.verbnet.SemanticPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
 * XML binding implementation of {@link SemanticPredicate}.
 *
 * @author jgung
 */
@Data
@Slf4j
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SemanticPredicateXml.ROOT_NAME)
public class SemanticPredicateXml implements SemanticPredicate {

    static final String ROOT_NAME = "PRED";

    @XmlAttribute(name = "bool")
    private String bool = "";

    @XmlAttribute(name = "value", required = true)
    private String value;

    @XmlElementWrapper(name = "ARGS")
    @XmlElement(name = SemanticArgumentXml.ROOT_NAME, required = true)
    private List<SemanticArgumentXml> args = new ArrayList<>();

    @Override
    public List<SemanticArgument> semanticArguments() {
        return args.stream().map(arg -> (SemanticArgument) arg).collect(Collectors.toList());
    }
}
