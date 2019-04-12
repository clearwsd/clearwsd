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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.SenseInventory;
import io.github.clearwsd.verbnet.xml.VerbNet;
import io.github.clearwsd.verbnet.xml.VerbNetClass;
import io.github.clearwsd.verbnet.xml.VerbNetFactory;
import io.github.clearwsd.verbnet.xml.VerbNetMember;
import io.github.clearwsd.verbnet.xml.WordNetKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet XML-based {@link SenseInventory} implementation.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetSenseInventory implements SenseInventory<VerbNetClass>, Serializable {

    private static final long serialVersionUID = 410274561044821035L;

    /**
     * Return the base lemma of a multi-word expression (e.g. "go_ballistic" 0-&gt; "go").
     *
     * @param mwe multi word expression
     * @return base lemma
     */
    public static String getBaseForm(String mwe) {
        String[] fields = mwe.split("_");
        return fields[0].toLowerCase();
    }

    /**
     * Get the numbered portion of a VerbNet class string, (e.g. "confront-98" --&gt; "98").
     *
     * @param id VerbNet class id
     * @return VerbNet class number
     */
    public static String getIdNumber(String id) {
        int hyphenIndex = id.indexOf("-");
        return id.substring(hyphenIndex + 1);
    }

    @Getter
    private transient VerbNet verbnet;

    private transient Multimap<String, VerbNetClass> lemmaVnMap;
    private transient Multimap<String, WordNetKey> lemmaWnMap;
    private transient Multimap<WordNetKey, VerbNetMember> wordNetMemberMap;
    private transient Map<String, VerbNetClass> senseVnMap;
    private CountingSenseInventory countingSenseInventory = new CountingSenseInventory();
    private URL url;
    private byte[] data; // we persist this as a byte[] for model loading when the URL is no longer valid

    /**
     * Initialize sense inventory from directory.
     *
     * @param path VerbNet XML directory
     */
    public VerbNetSenseInventory(File path) {
        try {
            this.url = path.toURI().toURL();
            initialize();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error initializing VerbNet sense inventory: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize sense inventory from {@link URL} (possibly on classpath).
     *
     * @param url VerbNet XML URL
     */
    public VerbNetSenseInventory(URL url) {
        this.url = url;
        initialize();
    }

    /**
     * Initialize sense inventory with default VerbNet from classpath resources.
     */
    public VerbNetSenseInventory() {
        this(VerbNetSenseInventory.class.getClassLoader().getResource("vn3.3.1.xml"));
    }

    @Override
    public Set<String> senses(String lemma) {
        return Sets.union(lemmaVnMap.get(lemma).stream()
                .map(cls -> getIdNumber(cls.id()))
                .collect(Collectors.toSet()), countingSenseInventory.senses(lemma));
    }

    @Override
    public String defaultSense(String lemma) {
        // TODO: find principled option for selecting default sense
        Optional<WordNetKey> wnKey = lemmaWnMap.get(lemma).stream()
                .min(Comparator.comparingInt(WordNetKey::lexicalId));
        if (wnKey.isPresent()) {
            Optional<VerbNetMember> member = wordNetMemberMap.get(wnKey.get()).stream().findFirst();
            if (member.isPresent()) {
                return getIdNumber(getRoot(member.get().verbClass()).id());
            }
        }
        return countingSenseInventory.defaultSense(lemma);
    }

    @Override
    public void addSense(String lemma, String sense) {
        countingSenseInventory.addSense(lemma, sense);
    }

    @Override
    public VerbNetClass getSense(String id) {
        return senseVnMap.get(id);
    }

    private void initialize() {
        if (this.data == null) {
            try (InputStream inputStream = url.openStream()) {
                this.data = ByteStreams.toByteArray(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Error reading VerbNet XML: " + e.getMessage(), e);
            }
            verbnet = VerbNetFactory.readVerbNet(new ByteArrayInputStream(data));
        } else {
            verbnet = VerbNetFactory.readVerbNet(new ByteArrayInputStream(data));
        }

        lemmaVnMap = HashMultimap.create();
        lemmaWnMap = HashMultimap.create();
        senseVnMap = new HashMap<>();
        wordNetMemberMap = LinkedHashMultimap.create();

        for (VerbNetClass cls : verbnet.classes()) {
            senseVnMap.put(getIdNumber(cls.id()), cls);
            for (VerbNetClass subcls : getAllSubclasses(cls)) {
                for (VerbNetMember member : subcls.members()) {
                    String name = getBaseForm(member.name());
                    lemmaVnMap.put(name, cls);
                    lemmaWnMap.putAll(name, member.wn());
                    for (WordNetKey key : member.wn()) {
                        wordNetMemberMap.put(key, member);
                    }
                }
            }
        }
    }

    private Set<VerbNetClass> getAllSubclasses(VerbNetClass cls) {
        Set<VerbNetClass> subclasses = new HashSet<>();
        subclasses.add(cls);
        for (VerbNetClass subclass : cls.subclasses()) {
            subclasses.addAll(getAllSubclasses(subclass));
        }
        return subclasses;
    }

    private VerbNetClass getRoot(VerbNetClass cls) {
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
