package io.github.clearwsd.verbnet.xml;

import org.xml.sax.InputSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import static io.github.clearwsd.verbnet.xml.VerbNetFrame.Adjective;
import static io.github.clearwsd.verbnet.xml.VerbNetFrame.Adverb;
import static io.github.clearwsd.verbnet.xml.VerbNetFrame.Lexical;
import static io.github.clearwsd.verbnet.xml.VerbNetFrame.NounPhrase;
import static io.github.clearwsd.verbnet.xml.VerbNetFrame.Preposition;
import static io.github.clearwsd.verbnet.xml.VerbNetFrame.Verb;

/**
 * VerbNet factory.
 *
 * @author jgung
 */
@XmlRegistry
public class VerbNetFactory {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Reads a single VerbNet XML file, a collection of VerbNet classes.
     *
     * @param inputStream VerbNet XML file input stream
     * @return VerbNet classes
     */
    public static VerbNet readVerbNet(InputStream inputStream) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parserFactory.setFeature(LOAD_EXTERNAL_DTD, false);
            SAXSource source = new SAXSource(parserFactory.newSAXParser().getXMLReader(), new InputSource(inputStream));
            return (VerbNet) JAXBContext.newInstance(VerbNet.class,
                    Adjective.class, Adverb.class, NounPhrase.class, Preposition.class, Lexical.class, Verb.class)
                    .createUnmarshaller().unmarshal(source);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading VerbNet XML files", e);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        VerbNet verbNet = readVerbNet(new FileInputStream("src/main/resources/vn3.3.1.xml"));
        for (VerbNetClass cls : verbNet.classes()) {
            System.out.println(cls.id());
            for (VerbNetClass vncls : cls.subclasses()) {
                System.out.println(vncls.id());
                vncls.members().forEach(System.out::println);
            }
        }
    }

}
