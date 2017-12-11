package edu.colorado.clear.wsd.app.experiment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.colorado.clear.wsd.corpus.VerbNetReader;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;

/**
 * @author jamesgung
 */
public class ArgumentAnalysis {

    public static void main(String... args) throws FileNotFoundException {
        List<FocusInstance<DepNode, DependencyTree>> instances
                = new VerbNetReader().readInstances(new FileInputStream(args[1]));

        List<String> children = new ArrayList<>();
        for (FocusInstance<DepNode, DependencyTree> instance : instances) {
            if (instance.focus().feature(FeatureType.Predicate).equals("fly"))
            instance.focus().children()
                    .stream()
//                    .filter(c -> c.dep().equalsIgnoreCase("nmod"))
//                    .map(DepNode::children)
//                    .flatMap(List::stream)
//                    .filter(c -> c.dep().equals("case"))
//                    .map(c -> c.head().feature(FeatureType.Lemma) + ":" + c.dep() + ":" + c.feature(FeatureType.Lemma) + ":" + c.feature(FeatureType.Pos)
                    .map(c -> c.dep() + ":" + c.feature(FeatureType.Lemma) + ":" + c.feature(FeatureType.Pos)
                            + "\t" + instance.sequence().feature(FeatureType.Text))
                    .forEach(children::add);
        }
        Collections.sort(children);
        for (String child : children) {
            System.out.println(child);
        }
    }

}
