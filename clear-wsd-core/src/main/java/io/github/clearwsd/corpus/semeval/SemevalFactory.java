package io.github.clearwsd.corpus.semeval;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRegistry;


@XmlRegistry
public class SemevalFactory {

    /**
     * Create an instance of {@link SemevalSentence }
     */
    public SemevalSentence createSentence() {
        return new SemevalSentence();
    }

    /**
     * Create an instance of {@link SemevalWordForm }
     */
    public SemevalWordForm createWf() {
        return new SemevalWordForm();
    }

    /**
     * Create an instance of {@link SemevalInstance }
     */
    public SemevalInstance createInstance() {
        return new SemevalInstance();
    }

    /**
     * Create an instance of {@link SemevalCorpus }
     */
    public SemevalCorpus createCorpus() {
        return new SemevalCorpus();
    }

    /**
     * Create an instance of {@link SemevalText }
     */
    public SemevalText createText() {
        return new SemevalText();
    }

    public static SemevalCorpus readCorpus(InputStream inputStream) throws JAXBException {
        JAXBContext jaxb = JAXBContext.newInstance(SemevalCorpus.class);
        return (SemevalCorpus) jaxb.createUnmarshaller().unmarshal(inputStream);
    }

    public static void writeCorpus(SemevalCorpus corpus, OutputStream outputStream) throws JAXBException {
        JAXBContext jaxb = JAXBContext.newInstance(SemevalCorpus.class);
        Marshaller marshaller = jaxb.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(corpus, outputStream);
    }

}
