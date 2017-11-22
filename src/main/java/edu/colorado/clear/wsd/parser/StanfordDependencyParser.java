package edu.colorado.clear.wsd.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.colorado.clear.wsd.type.BaseDepNode;
import edu.colorado.clear.wsd.type.BaseDependencyTree;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.TypedDependency;

import static edu.stanford.nlp.parser.nndep.DependencyParser.loadFromModelFile;

/**
 * Dependency parser implementation wrapping the Stanford parser.
 *
 * @author jamesgung
 */
public class StanfordDependencyParser implements DependencyParser {

    private static final String NO_ESCAPING = "ptb3Escaping=false";

    private TokenizerFactory tokenizer;
    private MaxentTagger posTagger;
    private Morphology lemmatizer;
    private edu.stanford.nlp.parser.nndep.DependencyParser depParser;

    public StanfordDependencyParser() {
        tokenizer = PTBTokenizer.coreLabelFactory();
        posTagger = new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH);
        lemmatizer = new Morphology();
        depParser = loadFromModelFile(edu.stanford.nlp.parser.nndep.DependencyParser.DEFAULT_MODEL);
    }

    @Override
    public List<String> segment(String string) {
        DocumentPreprocessor preprocessor = new DocumentPreprocessor(new StringReader(string));
        List<String> results = new ArrayList<>();
        for (List<HasWord> sentence : preprocessor) {
            results.add(SentenceUtils.listToOriginalTextString(sentence));
        }
        return results;
    }

    @Override
    public List<String> tokenize(String text) {
        Tokenizer tokenizer = this.tokenizer.getTokenizer(new StringReader(text), NO_ESCAPING);
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasNext()) {
            tokens.add(((HasWord) tokenizer.next()).word());
        }
        return tokens;
    }

    @Override
    public DependencyTree parse(List<String> sentence) {
        return parseTokens(tag(sentence));
    }

    private List<CoreLabel> getStanfordTokens(List<String> tokens) {
        List<CoreLabel> cls = new ArrayList<>();
        for (String token : tokens) {
            CoreLabel cl = new CoreLabel();
            cl.setOriginalText(token);
            cl.setWord(token);
            cl.setValue(token);
            cls.add(cl);
        }
        return cls;
    }

    private List<CoreLabel> tag(List<String> tokens) {
        List<CoreLabel> cls = getStanfordTokens(tokens);
        posTagger.tagCoreLabels(cls);
        return lemmatize(cls);
    }

    private List<CoreLabel> lemmatize(List<CoreLabel> words) {
        for (CoreLabel word : words) {
            word.setLemma(lemmatizer.lemma(word.word(), word.tag()));
        }
        return words;
    }

    private List<DepNode> toTokens(List<CoreLabel> cls) {
        int index = 0;
        List<DepNode> tokens = new ArrayList<>();
        for (Iterator<CoreLabel> iterator = cls.iterator(); iterator.hasNext(); ++index) {
            CoreLabel coreLabel = iterator.next();
            BaseDepNode token = new BaseDepNode(index);
            token.addFeature(FeatureType.Text, coreLabel.originalText());
            token.addFeature(FeatureType.Lemma, coreLabel.lemma());
            token.addFeature(FeatureType.Pos, coreLabel.tag());
            tokens.add(token);
        }
        return tokens;
    }

    private DependencyTree parseTokens(List<CoreLabel> cls) {
        List<DepNode> tokens = toTokens(cls);
        Collection<TypedDependency> dependencies = depParser.predict(cls).typedDependencies();
        Map<Integer, TypedDependency> dependencyMap = new HashMap<>();
        for (TypedDependency dep : dependencies) {
            dependencyMap.put(dep.dep().index(), dep);
        }
        DepNode root = null;
        for (DepNode token : tokens) {
            int head = dependencyMap.get(token.index() + 1).gov().index() - 1;
            if (head >= 0) {
                ((BaseDepNode) token).head(tokens.get(head));
                tokens.get(head).children().add(token);
            } else {
                root = token;
            }
            TypedDependency parent = dependencyMap.get(token.index() + 1);
            token.addFeature(FeatureType.Dep, parent.reln().toString());
        }

        return new BaseDependencyTree(0, tokens, root);
    }

}
