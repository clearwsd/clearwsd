package io.github.clearwsd.verbnet;

import io.github.clearwsd.verbnet.xml.SelectionalRestrictionsXml;
import io.github.clearwsd.verbnet.xml.VerbNetClassXml;
import io.github.clearwsd.verbnet.xml.VerbNetXmlFactory;
import io.github.clearwsd.verbnet.xml.VerbNetFrameXml;
import io.github.clearwsd.verbnet.xml.VerbNetThematicRoleXml;
import io.github.clearwsd.verbnet.xml.VerbNetXml;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * VerbNet reader unit tests.
 *
 * @author jgung
 */
public class VerbNetTest {

    private static VerbNetXml verbNet;

    @BeforeClass
    public static void init() {
        verbNet = VerbNetXmlFactory.readVerbNet(VerbNetClassXml.class.getClassLoader()
            .getResourceAsStream("verbnet-test.xml"));
    }

    @Test
    public void testMembers() {
        VerbNetClassXml verbNetClass = verbNet.classes().get(0);
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
        assertEquals(0, build.wn().size());
        assertEquals(0, die.wn().size());
    }

    @Test
    public void testThematicRoles() {
        VerbNetClassXml beginClass = verbNet.classes().get(1);
        assertEquals(3, beginClass.thematicRoles().size());
        VerbNetThematicRoleXml agent = beginClass.thematicRoles().get(0);
        VerbNetThematicRoleXml theme = beginClass.thematicRoles().get(1);
        VerbNetThematicRoleXml instrument = beginClass.thematicRoles().get(2);
        assertEquals("Agent", agent.type());
        assertEquals("Theme", theme.type());
        assertEquals("Instrument", instrument.type());
        SelectionalRestrictionsXml selRels = agent.restrictions();
        assertEquals("or", selRels.logic());
        assertEquals("+", selRels.selectionalRestriction().get(0).value());
        assertEquals("animate", selRels.selectionalRestriction().get(0).type());
        assertEquals("+", selRels.selectionalRestriction().get(1).value());
        assertEquals("organization", selRels.selectionalRestriction().get(1).type());
    }

    @Test
    public void testFrames() {
        VerbNetClassXml verbNetClass = verbNet.classes().get(0);
        assertEquals(3, verbNetClass.frames().size());

        VerbNetFrame frame = verbNetClass.frames().get(0);
        assertEquals("The price of oil soared.", frame.examples().get(0));
        assertEquals("2.13.5", frame.description().descriptionNumber());
        assertEquals("NP.attribute V", frame.description().primary());
        assertEquals("Intransitive; Attribute Subject", frame.description().secondary());
        assertEquals("", frame.description().xtag());

        assertEquals(4, frame.syntax().size());
        VerbNetFrameXml.NounPhrase patient = (VerbNetFrameXml.NounPhrase) frame.syntax().get(2);
        assertEquals(SyntaxType.NP, patient.type());
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
