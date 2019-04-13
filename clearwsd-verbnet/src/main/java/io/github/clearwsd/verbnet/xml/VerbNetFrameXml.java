package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.SemanticPredicate;
import io.github.clearwsd.verbnet.SyntacticPhrase;
import io.github.clearwsd.verbnet.SyntaxType;
import io.github.clearwsd.verbnet.VerbNetFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static io.github.clearwsd.verbnet.SyntaxType.ADJ;
import static io.github.clearwsd.verbnet.SyntaxType.ADV;
import static io.github.clearwsd.verbnet.SyntaxType.LEX;
import static io.github.clearwsd.verbnet.SyntaxType.NP;
import static io.github.clearwsd.verbnet.SyntaxType.PREP;
import static io.github.clearwsd.verbnet.SyntaxType.VERB;

/**
 * XML binding implementation of {@link VerbNetFrame}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VerbNetFrameXml.ROOT_NAME)
public class VerbNetFrameXml implements VerbNetFrame {

    static final String ROOT_NAME = "FRAME";

    @XmlElement(name = "DESCRIPTION", required = true)
    private FrameDescriptionXml description;

    @XmlElementWrapper(name = "EXAMPLES")
    @XmlElement(name = FrameExampleXml.ROOT_NAME, required = true)
    private List<FrameExampleXml> exampleElements = new ArrayList<>();

    @XmlElementWrapper(name = "SYNTAX")
    @XmlElementRef
    private List<Syntax> syntaxElements = new ArrayList<>();

    @XmlElementWrapper(name = "SEMANTICS")
    @XmlElement(name = SemanticPredicateXml.ROOT_NAME, required = true)
    private List<SemanticPredicateXml> preds = new ArrayList<>();

    @Override
    public List<String> examples() {
        return exampleElements.stream()
            .map(FrameExampleXml::value)
            .collect(Collectors.toList());
    }

    @Override
    public List<SyntacticPhrase> syntax() {
        return syntaxElements.stream()
            .map(pred -> (SyntacticPhrase) pred)
            .collect(Collectors.toList());
    }

    @Override
    public List<SemanticPredicate> predicates() {
        return preds.stream()
            .map(pred -> (SemanticPredicate) pred)
            .collect(Collectors.toList());
    }

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    public static abstract class Syntax implements SyntacticPhrase {

        protected SyntaxType type;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "NP")
    public static class NounPhrase extends Syntax {

        @XmlAttribute(name = "value", required = true)
        private String value;
        @XmlElement(name = SyntacticRestrictionsXml.ROOT_NAME)
        private SyntacticRestrictionsXml syntacticRestrictions;
        @XmlElement(name = SelectionalRestrictionXml.ROOT_NAME)
        private SelectionalRestrictionsXml selectionalRestrictions;

        public NounPhrase() {
            super(NP);
        }

    }

    @XmlRootElement(name = "VERB")
    public static class Verb extends Syntax {

        public Verb() {
            super(VERB);
        }
    }

    @XmlRootElement(name = "ADJ")
    public static class Adjective extends Syntax {

        public Adjective() {
            super(ADJ);
        }
    }

    @XmlRootElement(name = "ADV")
    public static class Adverb extends Syntax {

        public Adverb() {
            super(ADV);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "PREP")
    public static class Preposition extends Syntax {

        @XmlElement(name = SelectionalRestrictionsXml.ROOT_NAME)
        private SelectionalRestrictionsXml selectionalRestrictions;
        @XmlAttribute(name = "value")
        private String value;

        public Preposition() {
            super(PREP);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "LEX")
    public static class Lexical extends Syntax {

        @XmlAttribute(name = "value", required = true)
        private String value;

        public Lexical() {
            super(LEX);
        }
    }


}
