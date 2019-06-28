package io.github.clearwsd.verbnet;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import io.github.clearwsd.verbnet.restrictions.VnRestrictions;
import io.github.clearwsd.verbnet.semantics.VnPredicatePolarity;
import io.github.clearwsd.verbnet.semantics.VnSemanticArgument;
import io.github.clearwsd.verbnet.semantics.VnSemanticPredicate;
import io.github.clearwsd.verbnet.syntax.VnNounPhrase;
import io.github.clearwsd.verbnet.syntax.VnSyntaxType;
import io.github.clearwsd.verbnet.xml.VnClassXml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * VerbNet reader unit tests.
 *
 * @author jgung
 */
public class VnIndexTest {

    private static VnIndex verbNet;

    @BeforeClass
    public static void init() {
        verbNet = DefaultVnIndex.fromInputStream(VnClassXml.class.getClassLoader()
            .getResourceAsStream("vn_test.xml"));
    }

    @Test
    public void loadVerbNet() {
        VnIndex verbIndex = new DefaultVnIndex();
        assertEquals(328, verbIndex.roots().size());
    }

    @Test
    public void testMembers() {
        VnClass verbNetClass = verbNet.roots().get(0);
        assertEquals(2, verbNetClass.members().size());
        VnMember build = verbNetClass.members().get(0);
        VnMember die = verbNetClass.members().get(1);
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
        VnClass beginClass = verbNet.roots().get(1);
        assertEquals(3, beginClass.roles().size());
        VnThematicRole agent = beginClass.roles().get(0);
        VnThematicRole theme = beginClass.roles().get(1);
        VnThematicRole instrument = beginClass.roles().get(2);
        assertEquals("Agent", agent.type());
        assertEquals("Theme", theme.type());
        assertEquals("Instrument", instrument.type());
        List<VnRestrictions<String>> selRels = agent.restrictions();
        assertTrue(selRels.get(0).include().containsAll(Arrays.asList("animate", "location")));
        assertTrue(selRels.get(1).include().containsAll(Arrays.asList("organization", "location")));
        assertTrue(selRels.get(0).exclude().contains("region"));
        assertTrue(selRels.get(1).exclude().contains("region"));
    }

    @Test
    public void testFrames() {
        VnClass verbNetClass = verbNet.roots().get(0);
        assertEquals(3, verbNetClass.frames().size());

        VnFrame frame = verbNetClass.frames().get(0);
        assertEquals("The price of oil soared.", frame.examples().get(0));
        assertEquals("2.13.5", frame.description().descriptionNumber());
        assertEquals("NP.attribute V", frame.description().primary());
        assertEquals("Intransitive; Attribute Subject", frame.description().secondary());
        assertEquals("", frame.description().xtag());

        assertEquals(4, frame.syntax().size());
        VnNounPhrase patient = (VnNounPhrase) frame.syntax().get(2);
        assertEquals(VnSyntaxType.NP, patient.type());
        assertEquals(2, patient.index());
        assertEquals("Patient", patient.thematicRole());

        assertEquals(5, frame.predicates().size());
        VnSemanticPredicate semanticPredicate = frame.predicates().get(1);
        assertEquals(VnPredicatePolarity.TRUE, semanticPredicate.polarity());
        assertEquals("change_value", semanticPredicate.type());

        assertEquals(5, semanticPredicate.semanticArguments().size());
        VnSemanticArgument verbSpecific = semanticPredicate.semanticArguments().get(2);
        assertEquals("VerbSpecific", verbSpecific.type());
        assertEquals("V_Direction", verbSpecific.value());
    }

}
