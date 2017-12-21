package io.github.clearwsd.app;

import com.google.common.collect.Sets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import io.github.clearwsd.corpus.semeval.ParsingSemevalReader;
import io.github.clearwsd.corpus.semeval.SemevalReader;
import io.github.clearwsd.parser.StanfordDependencyParser;
import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.NlpFocus;

/**
 * @author jamesgung
 */
public class Scratch {

    public static final boolean PARSE = false;

    public static void main(String[] args) throws FileNotFoundException {
        String path = "clear-wsd-core/data/datasets/eval/";
        String dataset = "ALL";
        String inputPath = path + String.format("%s/%s.data.xml", dataset, dataset);
        String keyPath = path + String.format("%s/%s.gold.key.txt", dataset, dataset);
        if (PARSE) {
            SemevalReader reader = new ParsingSemevalReader(keyPath, new StanfordDependencyParser());
            List<NlpFocus<DepNode, DepTree>> instances = reader.readInstances(new FileInputStream(inputPath));
            reader.writeInstances(instances, new FileOutputStream(inputPath + ".ud.xml"));
            System.out.println(instances.size());
        } else {
            SemevalReader reader = new SemevalReader(keyPath);
            reader.setIncludePos(Sets.newHashSet("VERB"));
            List<NlpFocus<DepNode, DepTree>> instances = reader.readInstances(
                    new FileInputStream(inputPath + ".ud.xml"));
            reader.writeInstances(instances, new FileOutputStream(inputPath + ".ud-v.xml"));
            System.out.println(instances.size());
        }
    }

}
