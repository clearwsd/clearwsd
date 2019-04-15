package io.github.clearwsd.corpus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.clearwsd.type.DepNode;
import io.github.clearwsd.type.DepTree;
import io.github.clearwsd.type.FeatureType;
import io.github.clearwsd.type.NlpFocus;
import lombok.AllArgsConstructor;

/**
 * Corpus reader that applies label mappings.
 *
 * @author jamesgung
 */
@AllArgsConstructor
public class LemmaMappingCorpusReader implements CorpusReader<NlpFocus<DepNode, DepTree>> {

    private CorpusReader<NlpFocus<DepNode, DepTree>> corpusReader;

    private Map<String, Map<String, String>> labelMappings;

    private List<NlpFocus<DepNode, DepTree>> applyMappings(List<NlpFocus<DepNode, DepTree>> instances) {
        for (NlpFocus<DepNode, DepTree> instance : instances) {
            String lemma = instance.focus().feature(FeatureType.Lemma).toString();
            Map<String, String> lemmaMappings = labelMappings.get(lemma);
            if (null != lemmaMappings) {
                String label = instance.feature(FeatureType.Gold);
                instance.addFeature(FeatureType.Gold, lemmaMappings.getOrDefault(label, label));
                instance.focus().addFeature(FeatureType.Gold, lemmaMappings.getOrDefault(label, label));
            }
        }
        return instances;
    }

    @Override
    public List<NlpFocus<DepNode, DepTree>> readInstances(InputStream inputStream) {
        return applyMappings(corpusReader.readInstances(inputStream));
    }

    @Override
    public List<NlpFocus<DepNode, DepTree>> readInstances(InputStream inputStream, Set<String> filter) {
        return applyMappings(corpusReader.readInstances(inputStream, filter));
    }

    public void writeInstances(List<NlpFocus<DepNode, DepTree>> instances, OutputStream outputStream) {
        corpusReader.writeInstances(instances, outputStream);
    }

    /**
     * Load mappings from a comma-separated file.
     */
    public static Map<String, Map<String, String>> loadMappings(Path mappingsPath) {
        try {
            Map<String, Map<String, String>> lemmaMappings = new HashMap<>();
            for (String line : Files.readAllLines(mappingsPath)) {
                String[] fields = line.split(",");
                if (fields.length < 3) {
                    continue;
                }
                Map<String, String> mappings = lemmaMappings.computeIfAbsent(fields[0], k -> new HashMap<>());
                mappings.put(fields[1], fields[2]);
            }
            return lemmaMappings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
