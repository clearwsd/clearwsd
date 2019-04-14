/*
 * Copyright 2019 James Gung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.syntax.LexicalPhrase;
import io.github.clearwsd.verbnet.syntax.NounPhrase;
import io.github.clearwsd.verbnet.syntax.Preposition;
import io.github.clearwsd.verbnet.restrictions.Restrictions;
import io.github.clearwsd.verbnet.semantics.SemanticPredicate;
import io.github.clearwsd.verbnet.syntax.SyntacticPhrase;
import io.github.clearwsd.verbnet.syntax.SyntaxType;
import io.github.clearwsd.verbnet.VerbNetClass;
import io.github.clearwsd.verbnet.VerbNetFrame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import static io.github.clearwsd.verbnet.syntax.SyntaxType.ADJ;
import static io.github.clearwsd.verbnet.syntax.SyntaxType.ADV;
import static io.github.clearwsd.verbnet.syntax.SyntaxType.LEX;
import static io.github.clearwsd.verbnet.syntax.SyntaxType.NP;
import static io.github.clearwsd.verbnet.syntax.SyntaxType.PREP;
import static io.github.clearwsd.verbnet.syntax.SyntaxType.VERB;

/**
 * XML binding implementation of {@link VerbNetFrame}.
 *
 * @author jgung
 */
@Data
@ToString(of = "description")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VerbNetFrameXml.ROOT_NAME)
public class VerbNetFrameXml implements VerbNetFrame {

    static final String ROOT_NAME = "FRAME";

    @Delegate
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

    private transient VerbNetClass verbClass;

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
    public static abstract class Syntax implements SyntacticPhrase {

        protected int index;
        protected SyntaxType type;

        Syntax(SyntaxType syntaxType) {
            this.type = syntaxType;
        }

    }

    @Data
    @ToString(of = {"thematicRole"}, callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "NP")
    public static class NounPhraseXml extends Syntax implements NounPhrase {

        @XmlAttribute(name = "value", required = true)
        private String thematicRole;
        @XmlElement(name = SyntacticRestrictionsXml.ROOT_NAME)
        private SyntacticRestrictionsXml syntacticRestrictions;
        @XmlElement(name = SelectionalRestrictionXml.ROOT_NAME)
        private SelectionalRestrictionsXml selectionalRestrictions;

        public NounPhraseXml() {
            super(NP);
        }

        @Override
        public List<Restrictions<String>> selectionalRestrictions() {
            return selectionalRestrictions.restrictions();
        }

        @Override
        public List<Restrictions<String>> syntacticRestrictions() {
            return syntacticRestrictions.restrictions();
        }
    }

    @XmlRootElement(name = "VERB")
    public static class VerbXml extends Syntax {

        public VerbXml() {
            super(VERB);
        }
    }

    @XmlRootElement(name = "ADJ")
    public static class AdjectiveXml extends Syntax {

        public AdjectiveXml() {
            super(ADJ);
        }
    }

    @XmlRootElement(name = "ADV")
    public static class AdverbXml extends Syntax {

        public AdverbXml() {
            super(ADV);
        }
    }

    public static class ValueSetAdapter extends XmlAdapter<String, Set<String>> {

        @Override
        public Set<String> unmarshal(String value) {
            return Arrays.stream(value.split("\\|"))
                .map(prep -> prep.trim().split("\\s+"))
                .flatMap(Arrays::stream)
                .map(prep -> prep.trim().toLowerCase())
                .collect(Collectors.toSet());

        }

        @Override
        public String marshal(Set<String> value) {
            return String.join(" | ", value);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "PREP")
    public static class PrepXml extends Syntax implements Preposition {

        @XmlAttribute(name = "value")
        @XmlJavaTypeAdapter(ValueSetAdapter.class)
        private Set<String> types;
        @XmlElement(name = SelectionalRestrictionsXml.ROOT_NAME)
        private SelectionalRestrictionsXml selectionalRestrictions;

        public PrepXml() {
            super(PREP);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "LEX")
    public static class LexXml extends Syntax implements LexicalPhrase {

        @XmlAttribute(name = "value", required = true)
        private String value;

        public LexXml() {
            super(LEX);
        }
    }


}
