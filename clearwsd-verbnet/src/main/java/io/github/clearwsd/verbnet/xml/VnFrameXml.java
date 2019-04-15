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

import io.github.clearwsd.verbnet.VnClass;
import io.github.clearwsd.verbnet.VnFrame;
import io.github.clearwsd.verbnet.restrictions.VnRestrictions;
import io.github.clearwsd.verbnet.semantics.VnSemanticPredicate;
import io.github.clearwsd.verbnet.syntax.VnLex;
import io.github.clearwsd.verbnet.syntax.VnNounPhrase;
import io.github.clearwsd.verbnet.syntax.VnPrep;
import io.github.clearwsd.verbnet.syntax.VnSyntax;
import io.github.clearwsd.verbnet.syntax.VnSyntaxType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.ADJ;
import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.ADV;
import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.LEX;
import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.NP;
import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.PREP;
import static io.github.clearwsd.verbnet.syntax.VnSyntaxType.VERB;

/**
 * XML binding implementation of {@link VnFrame}.
 *
 * @author jgung
 */
@Data
@ToString(of = "description")
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = VnFrameXml.ROOT_NAME)
public class VnFrameXml implements VnFrame {

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

    private transient VnClass verbClass;

    @Override
    public List<String> examples() {
        return exampleElements.stream()
            .map(FrameExampleXml::value)
            .collect(Collectors.toList());
    }

    @Override
    public List<VnSyntax> syntax() {
        return syntaxElements.stream()
            .map(pred -> (VnSyntax) pred)
            .collect(Collectors.toList());
    }

    @Override
    public List<VnSemanticPredicate> predicates() {
        return preds.stream()
            .map(pred -> (VnSemanticPredicate) pred)
            .collect(Collectors.toList());
    }

    @Data
    @Accessors(fluent = true)
    public static abstract class Syntax implements VnSyntax {

        protected int index;
        protected VnSyntaxType type;

        Syntax(VnSyntaxType syntaxType) {
            this.type = syntaxType;
        }

    }

    @Data
    @ToString(of = {"thematicRole"}, callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "NP")
    public static class NounPhraseXml extends Syntax implements VnNounPhrase {

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
        public List<VnRestrictions<String>> selectionalRestrictions() {
            if (selectionalRestrictions == null) {
                return Collections.emptyList();
            }
            return selectionalRestrictions.restrictions();
        }

        @Override
        public List<VnRestrictions<String>> syntacticRestrictions() {
            if (syntacticRestrictions == null) {
                return Collections.emptyList();
            }
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
    public static class PrepXml extends Syntax implements VnPrep {

        @XmlAttribute(name = "value")
        @XmlJavaTypeAdapter(ValueSetAdapter.class)
        private Set<String> types = new HashSet<>();
        @XmlElement(name = SelectionalRestrictionsXml.ROOT_NAME)
        private SelectionalRestrictionsXml selectionalRestrictions;

        public PrepXml() {
            super(PREP);
        }

        @Override
        public List<VnRestrictions<String>> restrictions() {
            if (selectionalRestrictions == null) {
                return Collections.emptyList();
            }
            return selectionalRestrictions.restrictions();
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Accessors(fluent = true)
    @XmlRootElement(name = "LEX")
    public static class LexXml extends Syntax implements VnLex {

        @XmlAttribute(name = "value", required = true)
        private String value;

        public LexXml() {
            super(LEX);
        }
    }


}
