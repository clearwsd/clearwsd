package edu.colorado.clear.wsd.corpus.semeval;

import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.SetMultimap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import edu.colorado.clear.wsd.corpus.CorpusReader;
import edu.colorado.clear.wsd.type.BaseDepNode;
import edu.colorado.clear.wsd.type.BaseDependencyTree;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Semeval XML reader.
 *
 * @author jamesgung
 */
@Slf4j
public class SemevalReader implements CorpusReader<FocusInstance<DepNode, DependencyTree>> {

    @Getter
    @AllArgsConstructor
    private static class InstanceParsePair<T> {
        private List<T> instances;
        private BaseDependencyTree tree;
    }

    private SetMultimap<String, String> keys;
    @Getter
    @Setter
    private Set<String> includePos = new HashSet<>();

    public SemevalReader(String keyPath) {
        keys = readKeys(keyPath);
    }

    @Override
    public List<FocusInstance<DepNode, DependencyTree>> readInstances(InputStream inputStream) {
        SemevalCorpus semevalCorpus;
        try {
            semevalCorpus = SemevalFactory.readCorpus(inputStream);
        } catch (JAXBException e) {
            throw new RuntimeException("Error reading Semeval XML", e);
        }
        List<FocusInstance<DepNode, DependencyTree>> instances = new ArrayList<>();
        int sentenceIndex = 0;
        for (SemevalSentence semevalSentence : semevalCorpus.getAllSentences()) {
            InstanceParsePair<DepNode> instancePair = addInstances(sentenceIndex, semevalSentence);
            DependencyTree tree = processSentence(instancePair.getTree());
            Iterator<DepNode> tokenIterator = instancePair.getTree().tokens().iterator();
            for (DepNode token : tree.tokens()) {
                DepNode original = tokenIterator.next();
                token.addFeature(FeatureType.GoldLemma, original.feature(FeatureType.GoldLemma));
                token.addFeature(FeatureType.GoldPos, original.feature(FeatureType.GoldPos));
            }
            for (DepNode token : instancePair.getInstances()) {
                //noinspection SuspiciousMethodCalls
                if (includePos.size() > 0 && !includePos.contains(token.feature(FeatureType.GoldPos))) {
                    continue;
                }
                DepNode treeToken = tree.get(token.index());
                FocusInstance<DepNode, DependencyTree> instance = new FocusInstance<>(instances.size(), treeToken, tree);
                instance.addFeature(FeatureType.Sense, token.feature(FeatureType.Sense));
                treeToken.addFeature(FeatureType.Id, token.feature(FeatureType.Id));
                treeToken.addFeature(FeatureType.Sense, token.feature(FeatureType.Sense));
                treeToken.addFeature(FeatureType.AllSenses, token.feature(FeatureType.AllSenses));
                instances.add(instance);
            }
            ++sentenceIndex;
            if (sentenceIndex % 100 == 0) {
                log.debug("Read {} sentences and {} instances.", sentenceIndex, instances.size());
            }
        }
        return instances;
    }

    protected DependencyTree processSentence(BaseDependencyTree dependencyTree) {
        return dependencyTree;
    }

    private InstanceParsePair<DepNode> addInstances(int sentenceIndex, SemevalSentence semevalSentence) {
        List<DepNode> tokens = new ArrayList<>();
        List<DepNode> newInstances = new ArrayList<>();
        BaseDependencyTree sentence = new BaseDependencyTree(sentenceIndex, tokens, null);
        BaseDepNode root = null;
        Map<Integer, Integer> headMap = new HashMap<>();
        int index = 0;
        for (SemevalWordForm word : semevalSentence) {
            BaseDepNode token = new BaseDepNode(index++);
            token.addFeature(FeatureType.Text, word.getValue());
            token.addFeature(FeatureType.GoldLemma, word.getLemma());
            token.addFeature(FeatureType.GoldPos, word.getPos());
            if (word.getDep() != null) {
                token.addFeature(FeatureType.Lemma, word.getPredictedLemma());
                token.addFeature(FeatureType.Pos, word.getPredictedPos());
                token.addFeature(FeatureType.Dep, word.getDep());
                headMap.put(token.index(), Integer.parseInt(word.getHead()));
            }
            tokens.add(token);
            if (word instanceof SemevalInstance) {
                SemevalInstance instance = (SemevalInstance) word;
                Collection<String> senses = keys.get(instance.getId());
                String sense = senses.iterator().next();
                token.addFeature(FeatureType.Sense, sense);
                token.addFeature(FeatureType.Id, instance.getId());
                token.addFeature(FeatureType.AllSenses, String.join(" ", senses));
                newInstances.add(token);
            }
        }
        for (Map.Entry<Integer, Integer> entry : headMap.entrySet()) {
            BaseDepNode depNode = (BaseDepNode) sentence.get(entry.getKey());
            Integer head = entry.getValue();
            if (head < 0) {
                root = depNode;
                continue;
            }
            depNode.head(sentence.get(head));
        }
        sentence.root(root);
        sentence.addFeature(FeatureType.Id, semevalSentence.getId());
        return new InstanceParsePair<>(newInstances, sentence);
    }

    @Override
    public void writeInstances(List<FocusInstance<DepNode, DependencyTree>> instances,
                               OutputStream outputStream) {
        SemevalCorpus corpus = new SemevalCorpus();
        PeekingIterator<FocusInstance<DepNode, DependencyTree>> iterator = Iterators.peekingIterator(instances.iterator());
        InstanceParsePair<FocusInstance<DepNode, DependencyTree>> current;
        while ((current = nextTree(iterator)) != null) {
            SemevalSentence semevalSentence = new SemevalSentence();
            BaseDependencyTree tree = current.getTree();
            semevalSentence.setId(tree.feature(FeatureType.Id));
            List<FocusInstance<DepNode, DependencyTree>> tokens = current.getInstances();
            Map<Integer, SemevalInstance> instanceMap = tokens.stream().collect(
                    Collectors.toMap(focusInstance -> focusInstance.focus().index(),
                            focusInstance -> {
                                DepNode focusNode = focusInstance.focus();
                                SemevalInstance instance = new SemevalInstance(focusNode.feature(FeatureType.Text));
                                instance.setId(focusNode.feature(FeatureType.Id));
                                return instance;
                            }));
            for (DepNode depNode : tree.tokens()) {
                SemevalWordForm instance = instanceMap.get(depNode.index());
                if (instance == null) {
                    instance = new SemevalWordForm(depNode.feature(FeatureType.Text));
                }
                instance.setLemma(depNode.feature(FeatureType.GoldLemma));
                instance.setPos(depNode.feature(FeatureType.GoldPos));
                String dep = depNode.feature(FeatureType.Dep);
                if (dep != null) {
                    instance.setPredictedLemma(depNode.feature(FeatureType.Lemma));
                    instance.setPredictedPos(depNode.feature(FeatureType.Pos));
                    instance.setDep(dep);
                    instance.setHead(depNode.isRoot() ? "-1" : Integer.toString(depNode.head().index()));
                }
                semevalSentence.getElements().add(instance);
            }
            corpus.getElements().add(semevalSentence);
            if (corpus.getSentences().size() % 1000 == 0) {
                log.debug("Wrote {} sentences", corpus.getSentences().size());
            }
        }
        try {
            SemevalFactory.writeCorpus(corpus, outputStream);
            log.debug("Wrote {} sentences and {} instances.", corpus.getSentences().size(), instances.size());
        } catch (JAXBException e) {
            throw new RuntimeException("Error writing Semeval corpus XML", e);
        }
    }

    private InstanceParsePair<FocusInstance<DepNode, DependencyTree>> nextTree(PeekingIterator<FocusInstance<DepNode,
            DependencyTree>> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        List<FocusInstance<DepNode, DependencyTree>> instances = new ArrayList<>();
        FocusInstance<DepNode, DependencyTree> current = iterator.next();
        DependencyTree tree = current.sequence();
        instances.add(current);
        while (iterator.hasNext()) {
            current = iterator.peek();
            if (tree.index() == current.sequence().index()) {
                instances.add(iterator.next());
                continue;
            }
            break;
        }
        return new InstanceParsePair<>(instances, (BaseDependencyTree) tree);
    }

    /**
     * Read Semeval keys file.
     *
     * @param path path to keys file
     * @return map from sense IDs onto senses
     */
    private static SetMultimap<String, String> readKeys(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            SetMultimap<String, String> keys = LinkedHashMultimap.create();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] fields = line.split(" ");
                for (int i = 1; i < fields.length; ++i) {
                    keys.put(fields[0], fields[i]);
                }
            }
            return keys;
        } catch (IOException e) {
            throw new RuntimeException("Error reading sense keys file", e);
        }
    }

}
