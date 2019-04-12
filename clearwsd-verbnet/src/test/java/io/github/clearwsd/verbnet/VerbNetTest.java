package io.github.clearwsd.verbnet;

import org.junit.BeforeClass;
import org.junit.Test;

import io.github.clearwsd.verbnet.xml.SelectionalRestrictions;
import io.github.clearwsd.verbnet.xml.SemanticArgument;
import io.github.clearwsd.verbnet.xml.SemanticPredicate;
import io.github.clearwsd.verbnet.xml.VerbNet;
import io.github.clearwsd.verbnet.xml.VerbNetClass;
import io.github.clearwsd.verbnet.xml.VerbNetFactory;
import io.github.clearwsd.verbnet.xml.VerbNetFrame;
import io.github.clearwsd.verbnet.xml.VerbNetMember;
import io.github.clearwsd.verbnet.xml.VerbNetThematicRole;

import static org.junit.Assert.assertEquals;

/**
 * VerbNet reader unit tests.
 *
 * @author jgung
 */
public class VerbNetTest {

    private static VerbNet verbNet;

    @BeforeClass
    public static void init() {
        verbNet = VerbNetFactory.readVerbNet(VerbNetClass.class.getClassLoader()
                .getResourceAsStream("verbnet-test.xml"));
    }

    @Test
    public void testMembers() {
        VerbNetClass verbNetClass = verbNet.classes().get(0);
        assertEquals(2, verbNetClass.members().size());
        VerbNetMember build = verbNetClass.members().get(0);
        VerbNetMember die = verbNetClass.members().get(1);
        assertEquals("+increase", build.features());
        assertEquals("+decrease", die.features());
        assertEquals("build.02", build.grouping());
        assertEquals("die.02", die.grouping());
        assertEquals("build", build.name());
        assertEquals("die", die.name());
        assertEquals("build#3", build.verbnetKey());
        assertEquals("die#2", die.verbnetKey());
        assertEquals("", build.wn());
        assertEquals("", die.wn());
    }

    @Test
    public void testThematicRoles() {
        VerbNetClass beginClass = verbNet.classes().get(1);
        assertEquals(3, beginClass.thematicRoles().size());
        VerbNetThematicRole agent = beginClass.thematicRoles().get(0);
        VerbNetThematicRole theme = beginClass.thematicRoles().get(1);
        VerbNetThematicRole instrument = beginClass.thematicRoles().get(2);
        assertEquals("Agent", agent.type());
        assertEquals("Theme", theme.type());
        assertEquals("Instrument", instrument.type());
        SelectionalRestrictions selRels = agent.selectionalRestrictions();
        assertEquals("or", selRels.logic());
        assertEquals("+", selRels.selectionalRestriction().get(0).value());
        assertEquals("animate", selRels.selectionalRestriction().get(0).type());
        assertEquals("+", selRels.selectionalRestriction().get(1).value());
        assertEquals("organization", selRels.selectionalRestriction().get(1).type());
    }

    @Test
    public void testFrames() {
        VerbNetClass verbNetClass = verbNet.classes().get(0);
        assertEquals(3, verbNetClass.frames().size());

        VerbNetFrame frame = verbNetClass.frames().get(0);
        assertEquals("The price of oil soared.", frame.examples().get(0).value());
        assertEquals("2.13.5", frame.description().descriptionNumber());
        assertEquals("NP.attribute V", frame.description().primary());
        assertEquals("Intransitive; Attribute Subject", frame.description().secondary());
        assertEquals("", frame.description().xtag());

        assertEquals(4, frame.syntax().size());
        VerbNetFrame.NounPhrase patient = (VerbNetFrame.NounPhrase) frame.syntax().get(2);
        assertEquals("NP", patient.type());
        assertEquals("Patient", patient.value());

        assertEquals(5, frame.predicates().size());
        SemanticPredicate semanticPredicate = frame.predicates().get(1);
        assertEquals("change_value", semanticPredicate.value());

        assertEquals(5, semanticPredicate.semanticArguments().size());
        SemanticArgument verbSpecific = semanticPredicate.semanticArguments().get(2);
        assertEquals("VerbSpecific", verbSpecific.type());
        assertEquals("V_Direction", verbSpecific.value());
    }

}
