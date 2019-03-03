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
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.data.IWordnetKey;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.SenseInventory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet XML-based {@link SenseInventory} implementation.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetSenseInventory implements SenseInventory<IVerbClass>, Serializable {

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
    private transient IVerbIndex verbnet;

    private transient Multimap<String, IVerbClass> lemmaVnMap;
    private transient Multimap<String, IWordnetKey> lemmaWnMap;
    private transient Map<String, IVerbClass> senseVnMap;
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
        this(VerbNetSenseInventory.class.getClassLoader().getResource("vn3.3.xml"));
    }

    @Override
    public Set<String> senses(String lemma) {
        return Sets.union(lemmaVnMap.get(lemma).stream()
                .map(cls -> getIdNumber(cls.getID()))
                .collect(Collectors.toSet()), countingSenseInventory.senses(lemma));
    }

    @Override
    public String defaultSense(String lemma) {
        // TODO: find principled option for selecting default sense
        Optional<IWordnetKey> wnKey = lemmaWnMap.get(lemma).stream()
                .min(Comparator.comparingInt(IWordnetKey::getLexicalID));
        if (wnKey.isPresent()) {
            Optional<IMember> member = verbnet.getMembers(wnKey.get()).stream()
                    .min((m1, m2) -> Comparator.<String>naturalOrder()
                    .compare(m1.getVerbClass().getID(), m2.getVerbClass().getID()));
            if (member.isPresent()) {
                return getIdNumber(getRoot(member.get().getVerbClass()).getID());
            }
        }
        return countingSenseInventory.defaultSense(lemma);
    }

    @Override
    public void addSense(String lemma, String sense) {
        countingSenseInventory.addSense(lemma, sense);
    }

    @Override
    public IVerbClass getSense(String id) {
        return senseVnMap.get(id);
    }

    private void initialize() {
        if (this.data == null) {
            try {
                this.data = ByteStreams.toByteArray(url.openStream());
            } catch (IOException e) {
                throw new RuntimeException("Error reading VerbNet XML: " + e.getMessage(), e);
            }
            verbnet = new VerbIndex(url);
        } else {
            try {
                Path tmp = Files.createTempFile(getClass().getSimpleName(), getClass().getSimpleName());
                Files.write(tmp, data);
                tmp.toFile().deleteOnExit();
                verbnet = new VerbIndex(tmp.toUri().toURL());
            } catch (IOException e) {
                throw new RuntimeException("Unable to read VerbNet: " + e.getMessage(), e);
            }
        }

        try {
            verbnet.open();
        } catch (IOException e) {
            throw new RuntimeException("Error loading VerbNet dictionary: " + e.getMessage(), e);
        }
        Iterator<IVerbClass> clsIterator = verbnet.iterator();
        lemmaVnMap = HashMultimap.create();
        lemmaWnMap = HashMultimap.create();
        senseVnMap = new HashMap<>();
        while (clsIterator.hasNext()) {
            IVerbClass cls = clsIterator.next();
            if (null != cls.getParent()) { // skip sub-classes, we'll handle them recursively
                continue;
            }
            senseVnMap.put(getIdNumber(cls.getID()), cls);
            for (IVerbClass subcls : getAllSubclasses(cls)) {
                for (IMember member : subcls.getMembers()) {
                    String name = getBaseForm(member.getName());
                    lemmaVnMap.put(name, cls);
                    lemmaWnMap.putAll(name, member.getWordnetTypes().keySet());
                }
            }
        }
    }

    private Set<IVerbClass> getAllSubclasses(IVerbClass cls) {
        Set<IVerbClass> subclasses = new HashSet<>();
        subclasses.add(cls);
        for (IVerbClass subclass : cls.getSubclasses()) {
            subclasses.addAll(getAllSubclasses(subclass));
        }
        return subclasses;
    }

    private IVerbClass getRoot(IVerbClass cls) {
        while (cls.getParent() != null) {
            cls = cls.getParent();
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
