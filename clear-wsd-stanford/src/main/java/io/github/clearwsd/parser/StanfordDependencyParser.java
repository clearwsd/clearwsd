/*
 * Copyright (C) 2017  James Gung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.clearwsd.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.DefaultDepNode;
import io.github.clearwsd.type.DefaultDepTree;
import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import lombok.Getter;

import static edu.stanford.nlp.parser.nndep.DependencyParser.DEFAULT_MODEL;
import static edu.stanford.nlp.parser.nndep.DependencyParser.loadFromModelFile;

/**
 * Dependency parser implementation wrapping the Stanford parser. Applies part-of-speech annotation and performs lemmatization
 * during parsing, adding {@link FeatureType#Pos} and {@link FeatureType#Lemma} features. Components are threadsafe.
 *
 * @author jamesgung
 */
public class StanfordDependencyParser implements NlpParser {

    public enum StanfordParserModel {
        UD(DEFAULT_MODEL, Language.UniversalEnglish.name()),
        SD("edu/stanford/nlp/models/parser/nndep/english_SD.gz", Language.English.name());
        @Getter
        private String path;
        @Getter
        private Properties language;

        StanfordParserModel(String path, String language) {
            this.path = path;
            Properties properties = new Properties();
            properties.setProperty("language", language);
            this.language = properties;
        }
    }

    private NlpTokenizer nlpTokenizer;
    private MaxentTagger posTagger;
    private DependencyParser depParser;

    /**
     * Constructor with a specific StanfordCoreNLP part-of-speech tagger and dependency parser.
     *
     * @param nlpTokenizer tokenizer
     * @param posTagger    part-of-speech tagger
     * @param depParser    dependency parser
     */
    public StanfordDependencyParser(NlpTokenizer nlpTokenizer, MaxentTagger posTagger, DependencyParser depParser) {
        this.nlpTokenizer = nlpTokenizer;
        this.posTagger = posTagger;
        this.depParser = depParser;
    }

    /**
     * Constructor taking a model type from {@link StanfordParserModel}.
     *
     * @param model dependency parser model type
     */
    public StanfordDependencyParser(StanfordParserModel model) {
        this(new StanfordTokenizer(), new MaxentTagger(MaxentTagger.DEFAULT_JAR_PATH),
                loadFromModelFile(model.path, model.language));
        nlpTokenizer = new StanfordTokenizer();
    }

    /**
     * Constructor that automatically initializes the {@link StanfordParserModel#UD} model.
     */
    public StanfordDependencyParser() {
        this(StanfordParserModel.UD);
    }

    @Override
    public List<String> segment(String input) {
        return nlpTokenizer.segment(input);
    }

    @Override
    public List<String> tokenize(String sentence) {
        return nlpTokenizer.tokenize(sentence);
    }

    @Override
    public DepTree parse(List<String> sentence) {
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
        Morphology lemmatizer = new Morphology();
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
            DefaultDepNode token = new DefaultDepNode(index);
            token.addFeature(FeatureType.Text, coreLabel.originalText());
            token.addFeature(FeatureType.Lemma, coreLabel.lemma());
            token.addFeature(FeatureType.Pos, coreLabel.tag());
            tokens.add(token);
        }
        return tokens;
    }

    private DepTree parseTokens(List<CoreLabel> cls) {
        List<DepNode> tokens = toTokens(cls);
        GrammaticalStructure structure = depParser.predict(cls);
        Map<Integer, TypedDependency> dependencyMap = structure.typedDependencies()
                .stream().collect(Collectors.toMap(dep -> dep.dep().index(), dep -> dep));

        Map<Integer, TypedDependency> collapsedDeps = new HashMap<>();
        for (TypedDependency dependency : structure.typedDependenciesCollapsed()) {
            collapsedDeps.put(dependency.dep().index(), dependency);
        }

        DepNode root = null;
        for (DepNode token : tokens) {
            int index = token.index() + 1;
            TypedDependency rel = collapsedDeps.get(index);
            TypedDependency collapsedRel = dependencyMap.get(index);
            boolean collapsed = rel == null;
            if (collapsed || (rel.dep().index() == rel.gov().index()) || collapsedRel.gov().index() <= 0) {
                rel = collapsedRel;
            }
            token.addFeature(FeatureType.Dep, rel.reln().toString());
            int head = rel.gov().index() - 1;
            if (head >= 0) {
                ((DefaultDepNode) token).head(tokens.get(head));
                if (!collapsed) {
                    tokens.get(head).children().add(token);
                }
            } else {
                root = token;
            }
        }

        return new DefaultDepTree(0, tokens, root);
    }

}
