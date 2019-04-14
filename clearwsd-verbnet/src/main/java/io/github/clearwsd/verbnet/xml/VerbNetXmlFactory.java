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

import io.github.clearwsd.verbnet.DefaultVerbIndex;
import io.github.clearwsd.verbnet.VerbIndex;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;

import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.AdjectiveXml;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.AdverbXml;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.LexXml;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.NounPhraseXml;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.PrepXml;
import static io.github.clearwsd.verbnet.xml.VerbNetFrameXml.VerbXml;

/**
 * VerbNetXml factory.
 *
 * @author jgung
 */
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
                AdjectiveXml.class, AdverbXml.class, NounPhraseXml.class, PrepXml.class, LexXml.class, VerbXml.class)
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
            for (VerbNetFrameXml frameXml: verbNetClass.frameElements()) {
                frameXml.verbClass(verbNetClass);
            }
            setPointers(verbNetClass);
        }
    }

}
