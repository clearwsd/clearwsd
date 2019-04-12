package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;

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

/**
 * VerbNet class frame.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "FRAME")
public class VerbNetFrame {

    @XmlElement(name = "DESCRIPTION", required = true)
    private FrameDescription description;

    @XmlElementWrapper(name = "EXAMPLES")
    @XmlElement(name = "EXAMPLE", required = true)
    private List<FrameExample> examples = new ArrayList<>();

    @XmlElementWrapper(name = "SYNTAX")
    @XmlElementRef
    private List<Syntax> syntax = new ArrayList<>();

    @XmlElementWrapper(name = "SEMANTICS")
    @XmlElement(name = "PRED", required = true)
    private List<SemanticPredicate> predicates = new ArrayList<>();

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    public static abstract class Syntax {

        protected String type;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "NP")
    public static class NounPhrase extends Syntax {

        @XmlAttribute(name = "value", required = true)
        private String value;
        @XmlElement(name = "SYNRESTRS")
        private SyntacticRestrictions syntacticRestrictions;
        @XmlElement(name = "SELRESTRS")
        private SelectionalRestrictions selectionalRestrictions;

        public NounPhrase() {
            super("NP");
        }

    }

    @XmlRootElement(name = "VERB")
    public static class Verb extends Syntax {

        public Verb() {
            super("VERB");
        }
    }

    @XmlRootElement(name = "ADJ")
    public static class Adjective extends Syntax {

        public Adjective() {
            super("ADJ");
        }
    }

    @XmlRootElement(name = "ADV")
    public static class Adverb extends Syntax {

        public Adverb() {
            super("ADV");
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "PREP")
    public static class Preposition extends Syntax {

        @XmlElement(name = "SELRESTRS")
        private SelectionalRestrictions selectionalRestrictions;
        @XmlAttribute(name = "value")
        private String value;

        public Preposition() {
            super("PREP");
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
            super("LEX");
        }
    }


}
