package io.github.clearwsd.verbnet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.clearwsd.utils.CountingSenseInventory;
import io.github.clearwsd.utils.SenseInventory;
import edu.mit.jverbnet.data.IMember;
import edu.mit.jverbnet.data.IVerbClass;
import edu.mit.jverbnet.data.IWordnetKey;
import edu.mit.jverbnet.index.IVerbIndex;
import edu.mit.jverbnet.index.VerbIndex;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * VerbNet XML-based sense inventory.
 *
 * @author jamesgung
 */
@Slf4j
public class VerbNetSenseInventory implements SenseInventory, Serializable {

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

    public VerbNetSenseInventory(URL url) {
        this.url = url;
        initialize();
    }

    public VerbNetSenseInventory() {
        this(VerbNetSenseInventory.class.getClassLoader().getResource("vn32.xml"));
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
                    .sorted((m1, m2) -> Comparator.<String>naturalOrder()
                            .compare(m1.getVerbClass().getID(), m2.getVerbClass().getID()))
                    .findFirst();
            if (member.isPresent()) {
                return getIdNumber(getRoot(member.get().getVerbClass()).getID());
            }
        }
        return countingSenseInventory.defaultSense(lemma);
    }

    @Override
    public void addSense(String lemma, String sense) {
        if (!senseVnMap.containsKey(sense) && !countingSenseInventory.senses(lemma).contains(sense)) {
            log.warn("Unrecognized sense: {}", sense);
        }
        countingSenseInventory.addSense(lemma, sense);
    }

    private void initialize() {
        verbnet = new VerbIndex(url);
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
