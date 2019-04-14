/*
 * Copyright 2017 James Gung
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

package io.github.clearwsd.verbnet;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.SenseInventory;
import io.github.clearwsd.verbnet.xml.VerbNetXmlFactory;
import io.github.clearwsd.verbnet.xml.WordNetKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNetXml XML-based {@link SenseInventory} implementation.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetSenseInventory implements SenseInventory<VnClass>, Serializable {

    private static final long serialVersionUID = 410274561044821035L;

    public static final String DEFAULT_VERBNET = "vn_3.3.xml";

    @Getter
    private transient VerbIndex verbnet;

    private CountingSenseInventory countingSenseInventory = new CountingSenseInventory();
    private URL url;
    private byte[] data; // we persist this as a byte[] for model loading when the URL is no longer valid

    /**
     * Initialize sense inventory from directory.
     *
     * @param path VerbNetXml XML directory
     */
    public VerbNetSenseInventory(File path) {
        try {
            this.url = path.toURI().toURL();
            initialize();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error initializing VerbNetXml sense inventory: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize sense inventory from {@link URL} (possibly on classpath).
     *
     * @param url VerbNetXml XML URL
     */
    public VerbNetSenseInventory(URL url) {
        this.url = url;
        initialize();
    }

    /**
     * Initialize sense inventory with default VerbNetXml from classpath resources.
     */
    public VerbNetSenseInventory() {
        this(VerbNetSenseInventory.class.getClassLoader().getResource(DEFAULT_VERBNET));
    }

    @Override
    public Set<String> senses(String lemma) {
        return Sets.union(verbnet.getByLemma(lemma).stream()
            .map(cls -> cls.verbNetId().rootId())
            .collect(Collectors.toSet()), countingSenseInventory.senses(lemma));
    }

    @Override
    public String defaultSense(String lemma) {
        Optional<WordNetKey> wnKey = verbnet.getWordNetKeysByLemma(lemma).stream()
            .min(Comparator.comparingInt(WordNetKey::lexicalId));
        if (wnKey.isPresent()) {
            Optional<VerbNetId> member = verbnet.getMembersByWordNetKey(wnKey.get()).stream()
                .map(VnMember::verbClass)
                .map(VnClass::verbNetId)
                .min(VerbNetId::compareTo);
            if (member.isPresent()) {
                return member.get().rootId();
            }
        }
        return countingSenseInventory.defaultSense(lemma);
    }

    @Override
    public void addSense(String lemma, String sense) {
        countingSenseInventory.addSense(lemma, sense);
    }

    @Override
    public VnClass getSense(String id) {
        return verbnet.getById(id);
    }

    private void initialize() {
        if (this.data == null) {
            try (InputStream inputStream = url.openStream()) {
                this.data = ByteStreams.toByteArray(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Error reading VerbNetXml XML: " + e.getMessage(), e);
            }
            verbnet = VerbNetXmlFactory.readVerbNet(new ByteArrayInputStream(data));
        } else {
            verbnet = VerbNetXmlFactory.readVerbNet(new ByteArrayInputStream(data));
        }
    }

    private VnClass getRoot(VnClass cls) {
        while (cls.parentClass().isPresent()) {
            cls = cls.parentClass().get();
        }
        return cls;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        initialize();
    }

}
