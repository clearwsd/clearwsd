package io.github.clearwsd.verbnet;

import io.github.clearwsd.verbnet.xml.VerbNetClassXml;
import io.github.clearwsd.verbnet.xml.VerbNetXmlFactory;
import java.util.Arrays;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * VerbNet reader unit tests.
 *
 * @author jgung
 */
public class VerbNetTest {

    private static VerbIndex verbNet;

    @BeforeClass
    public static void init() {
        verbNet = VerbNetXmlFactory.readVerbNet(VerbNetClassXml.class.getClassLoader()
            .getResourceAsStream("vn_test.xml"));
    }

    @Test
    public void loadVerbNet() {
        VerbIndex verbIndex = VerbNetXmlFactory.readVerbNet(VerbNetClassXml.class.getClassLoader()
            .getResourceAsStream("vn_3.3.xml"));
        assertEquals(327, verbIndex.roots().size());
    }

    @Test
    public void testMembers() {
        VerbNetClass verbNetClass = verbNet.roots().get(0);
        assertEquals(2, verbNetClass.members().size());
        VerbNetMember build = verbNetClass.members().get(0);
        VerbNetMember die = verbNetClass.members().get(1);
        assertTrue(build.features().contains("increase"));
        assertTrue(die.features().contains("decrease"));
        assertTrue(build.groupings().contains("build.02"));
        assertTrue(die.groupings().contains("die.02"));
        assertEquals("build", build.name());
        assertEquals("die", die.name());
        assertEquals("build#3", build.verbnetKey());
        assertEquals("die#2", die.verbnetKey());
        assertEquals(0, build.wn().size());
        assertEquals(0, die.wn().size());
    }

    @Test
    public void testThematicRoles() {
        VerbNetClass beginClass = verbNet.roots().get(1);
        assertEquals(3, beginClass.roles().size());
        ThematicRole agent = beginClass.roles().get(0);
        ThematicRole theme = beginClass.roles().get(1);
        ThematicRole instrument = beginClass.roles().get(2);
        assertEquals("Agent", agent.type());
        assertEquals("Theme", theme.type());
        assertEquals("Instrument", instrument.type());
        List<Restrictions<String>> selRels = agent.restrictions();
        assertTrue(selRels.get(0).include().containsAll(Arrays.asList("animate", "location")));
        assertTrue(selRels.get(1).include().containsAll(Arrays.asList("organization", "location")));
        assertTrue(selRels.get(0).exclude().contains("region"));
        assertTrue(selRels.get(1).exclude().contains("region"));
    }

    @Test
    public void testFrames() {
        VerbNetClass verbNetClass = verbNet.roots().get(0);
        assertEquals(3, verbNetClass.frames().size());

        VerbNetFrame frame = verbNetClass.frames().get(0);
        assertEquals("The price of oil soared.", frame.examples().get(0));
        assertEquals("2.13.5", frame.description().descriptionNumber());
        assertEquals("NP.attribute V", frame.description().primary());
        assertEquals("Intransitive; Attribute Subject", frame.description().secondary());
        assertEquals("", frame.description().xtag());

        assertEquals(4, frame.syntax().size());
        NounPhrase patient = (NounPhrase) frame.syntax().get(2);
        assertEquals(SyntaxType.NP, patient.type());
        assertEquals("Patient", patient.thematicRole());

        assertEquals(5, frame.predicates().size());
        SemanticPredicate semanticPredicate = frame.predicates().get(1);
        assertEquals("change_value", semanticPredicate.type());

        assertEquals(5, semanticPredicate.semanticArguments().size());
        SemanticArgument verbSpecific = semanticPredicate.semanticArguments().get(2);
        assertEquals("VerbSpecific", verbSpecific.type());
        assertEquals("V_Direction", verbSpecific.value());
    }

}
