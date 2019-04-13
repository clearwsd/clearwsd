package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.DefaultVerbIndex;
import io.github.clearwsd.verbnet.VerbIndex;
import io.github.clearwsd.verbnet.VerbNetClass;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;

import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.Adjective;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.Adverb;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.Lexical;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.NounPhrase;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.Preposition;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.Verb;

/**
 * VerbNetXml factory.
 *
 * @author jgung
 */
@XmlRegistry
public class VerbNetXmlFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Reads a single VerbNetXml XML file, a collection of VerbNetXml classes.
     *
     * @param inputStream VerbNetXml XML file input stream
     * @return VerbNetXml classes
     */
    public static VerbIndex readVerbNet(InputStream inputStream) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            SAXSource source = new SAXSource(parserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
            VerbNetXml verbNet = (VerbNetXml) JAXBContext.newInstance(VerbNetXml.class,
                Adjective.class, Adverb.class, NounPhrase.class, Preposition.class, Lexical.class, Verb.class)
                .createUnmarshaller().unmarshal(source);
            verbNet.classes().forEach(VerbNetXmlFactory::setPointers);
            return new DefaultVerbIndex(verbNet.verbClasses());
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading VerbNetXml XML files", e);
        }
    }

    private static void setPointers(VerbNetClassXml parent) {
        for (VerbNetMemberXml member : parent.memberElements()) {
            member.verbClass(parent);
        }
        for (VerbNetClassXml verbNetClass : parent.children()) {
            verbNetClass.parentClass(parent);
            setPointers(verbNetClass);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        VerbIndex verbNet = readVerbNet(new FileInputStream("src/main/resources/vn3.3.1.xml"));
        for (VerbNetClass cls : verbNet.roots()) {
            System.out.println(cls.verbNetId());
            for (VerbNetClass vncls : cls.subclasses()) {
                System.out.println(vncls.verbNetId());
                vncls.members().forEach(System.out::println);
            }
        }
    }

}
