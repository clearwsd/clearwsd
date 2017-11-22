package edu.colorado.clear.wsd.corpus;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import edu.colorado.clear.wsd.type.FocusInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * VerbNet (parsed) corpus reader.
 *
 * @author jamesgung
 */
public class VerbNetReader implements CorpusReader<FocusInstance<DepNode, DependencyTree>> {

    private VerbNetCoNllDepReader depReader = new VerbNetCoNllDepReader();

    @Override
    public List<FocusInstance<DepNode, DependencyTree>> readInstances(InputStream inputStream) {
        List<FocusInstance<DepNode, DependencyTree>> results = new ArrayList<>();
        int index = 0;
        for (DependencyTree tree : depReader.readInstances(inputStream)) {
            for (DepNode focus : tree.tokens().stream()
                    .filter(t -> t.feature(FeatureType.Gold) != null)
                    .collect(Collectors.toList())) {
                FocusInstance<DepNode, DependencyTree> instance = new FocusInstance<>(index++, focus, tree);
                instance.addFeature(FeatureType.Gold, focus.feature(FeatureType.Gold));
                results.add(instance);
            }
        }
        return results;
    }

    @Override
    public void writeInstances(List<FocusInstance<DepNode, DependencyTree>> instances, OutputStream outputStream) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            for (FocusInstance<DepNode, DependencyTree> instance : instances) {
                String metadata = instance.feature(FeatureType.Metadata);
                if (metadata == null) {
                    writer.println("# " + new VerbNetInstanceParser().toString(new VerbNetInstance()
                            .path(Integer.toString(instance.index()))
                            .label(instance.feature(FeatureType.Gold))
                            .sentence(instance.sequence().index())
                            .token(instance.focus().index())
                            .lemma(instance.feature(FeatureType.Predicate))
                            .originalText(instance.sequence().feature(FeatureType.Text))));
                } else {
                    writer.println("# " + metadata);
                }
                writer.println(depReader.treeToString(instance.sequence()));
                writer.println();
                writer.flush();
            }
        }
    }

    public static class VerbNetCoNllDepReader extends CoNllDepTreeReader {
        @Override
        protected void processHeader(List<String> header, DependencyTree result) {
            for (String headerLine : header) {
                headerLine = headerLine.replaceAll("^#\\s*", "");
                VerbNetInstance instance = new VerbNetInstanceParser().parse(headerLine);
                DepNode focus = result.get(instance.token);
                focus.addFeature(FeatureType.Gold, instance.label);
                focus.addFeature(FeatureType.Predicate, instance.lemma);
                result.addFeature(FeatureType.Text, instance.originalText);
            }
        }
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static class VerbNetInstance {
        private String path;
        private int sentence;
        private int token;
        private String lemma;
        private String label;
        private String originalText;
    }

    public static class VerbNetInstanceParser {

        public VerbNetInstance parse(String input) {
            String[] fields = input.split("\t");
            String[] subFields = fields[0].split(" ");
            return new VerbNetInstance()
                    .path(subFields[0])
                    .sentence(Integer.parseInt(subFields[1]))
                    .token(Integer.parseInt(subFields[2]))
                    .lemma(subFields[3])
                    .label(subFields[4])
                    .originalText(fields[1]);
        }

        public String toString(VerbNetInstance instance) {
            return String.format("%s %d %d %s %s\t%s", instance.path, instance.sentence, instance.token,
                    instance.lemma, instance.label, instance.originalText);
        }
    }

}
