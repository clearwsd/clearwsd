package io.github.clearwsd.corpus.semeval;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import io.github.clearwsd.corpus.CorpusReader;
import io.github.clearwsd.type.DefaultDepNode;
import io.github.clearwsd.type.DefaultDepTree;
import io.github.clearwsd.type.DefaultNlpFocus;
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
public class SemevalReader implements CorpusReader<NlpFocus<DepNode, DepTree>> {

    @Getter
    @AllArgsConstructor
    private static class InstanceParsePair<T> {
        private List<T> instances;
        private DefaultDepTree tree;
    }

    private SetMultimap<String, String> keys;
    @Getter
    @Setter
    private Set<String> includePos = new HashSet<>();

    public SemevalReader(String keyPath) {
        keys = readKeys(keyPath);
    }

    @Override
    public List<NlpFocus<DepNode, DepTree>> readInstances(InputStream inputStream) {
        SemevalCorpus semevalCorpus;
        try {
            semevalCorpus = SemevalFactory.readCorpus(inputStream);
        } catch (JAXBException e) {
            throw new RuntimeException("Error reading Semeval XML", e);
        }
        List<NlpFocus<DepNode, DepTree>> instances = new ArrayList<>();
        int sentenceIndex = 0;
        for (SemevalSentence semevalSentence : semevalCorpus.getAllSentences()) {
            InstanceParsePair<DepNode> instancePair = addInstances(sentenceIndex, semevalSentence);
            DepTree tree = processSentence(instancePair.getTree());
            Iterator<DepNode> tokenIterator = instancePair.getTree().tokens().iterator();
            for (DepNode token : tree) {
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
                NlpFocus<DepNode, DepTree> instance = new DefaultNlpFocus<>(instances.size(), treeToken, tree);
                instance.addFeature(FeatureType.Gold, token.feature(FeatureType.Gold));
                instance.addFeature(FeatureType.AllSenses, token.feature(FeatureType.AllSenses));
                treeToken.addFeature(FeatureType.Id, token.feature(FeatureType.Id));
                treeToken.addFeature(FeatureType.Sense, token.feature(FeatureType.Sense));
                instances.add(instance);
            }
            ++sentenceIndex;
            if (sentenceIndex % 100 == 0) {
                log.debug("Read {} sentences and {} instances.", sentenceIndex, instances.size());
            }
        }
        return instances;
    }

    protected DepTree processSentence(DefaultDepTree dependencyTree) {
        return dependencyTree;
    }

    private InstanceParsePair<DepNode> addInstances(int sentenceIndex, SemevalSentence semevalSentence) {
        List<DepNode> tokens = new ArrayList<>();
        List<DepNode> newInstances = new ArrayList<>();
        DefaultDepTree sentence = new DefaultDepTree(sentenceIndex, tokens, null);
        DefaultDepNode root = null;
        Map<Integer, Integer> headMap = new HashMap<>();
        int index = 0;
        for (SemevalWordForm word : semevalSentence) {
            DefaultDepNode token = new DefaultDepNode(index++);
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
                Set<String> senses = keys.get(instance.getId());
                String sense = senses.iterator().next();
                token.addFeature(FeatureType.Gold, sense);
                token.addFeature(FeatureType.AllSenses, senses);
                token.addFeature(FeatureType.Id, instance.getId());
                token.addFeature(FeatureType.Sense, sense);
                token.addFeature(FeatureType.Predicate, instance.getLemma());
                newInstances.add(token);
            }
        }
        for (Map.Entry<Integer, Integer> entry : headMap.entrySet()) {
            DefaultDepNode depNode = (DefaultDepNode) sentence.get(entry.getKey());
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
    public void writeInstances(List<NlpFocus<DepNode, DepTree>> instances,
                               OutputStream outputStream) {
        SemevalCorpus corpus = new SemevalCorpus();
        PeekingIterator<NlpFocus<DepNode, DepTree>> iterator = Iterators.peekingIterator(instances.iterator());
        InstanceParsePair<NlpFocus<DepNode, DepTree>> current;
        while ((current = nextTree(iterator)) != null) {
            SemevalSentence semevalSentence = new SemevalSentence();
            DefaultDepTree tree = current.getTree();
            semevalSentence.setId(tree.feature(FeatureType.Id));
            List<NlpFocus<DepNode, DepTree>> tokens = current.getInstances();
            Map<Integer, SemevalInstance> instanceMap = tokens.stream().collect(
                    Collectors.toMap(focusInstance -> focusInstance.focus().index(),
                            focusInstance -> {
                                DepNode focusNode = focusInstance.focus();
                                SemevalInstance instance = new SemevalInstance(focusNode.feature(FeatureType.Text));
                                instance.setId(focusNode.feature(FeatureType.Id));
                                return instance;
                            }));
            for (DepNode depNode : tree) {
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

    private InstanceParsePair<NlpFocus<DepNode, DepTree>> nextTree(PeekingIterator<NlpFocus<DepNode,
            DepTree>> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        List<NlpFocus<DepNode, DepTree>> instances = new ArrayList<>();
        NlpFocus<DepNode, DepTree> current = iterator.next();
        DepTree tree = current.sequence();
        instances.add(current);
        while (iterator.hasNext()) {
            current = iterator.peek();
            if (tree.index() == current.sequence().index()) {
                instances.add(iterator.next());
                continue;
            }
            break;
        }
        return new InstanceParsePair<>(instances, (DefaultDepTree) tree);
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
