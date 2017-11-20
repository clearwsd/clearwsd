package edu.colorado.clear.wsd.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.colorado.clear.wsd.type.BaseDepNode;
import edu.colorado.clear.wsd.type.BaseDependencyTree;
import edu.colorado.clear.wsd.type.DepNode;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;

/**
 * CoNLL-U-style dependency tree corpus reader.
 *
 * @author jamesgung
 */
public class CoNllDepTreeReader implements CorpusReader<DependencyTree> {

    private static final String FIELD_DELIM = "\t";

    private Pattern headerPattern = Pattern.compile("^#.*$");

    @Override
    public List<DependencyTree> readInstances(InputStream inputStream) {
        List<DependencyTree> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> lines = new ArrayList<>();
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.isEmpty()) {
                    if (lines.size() > 0) {
                        results.add(readTree(results.size(), lines));
                        lines = new ArrayList<>();
                    }
                    continue;
                }
                lines.add(currentLine);
            }
            if (lines.size() > 0) {
                results.add(readTree(results.size(), lines));
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading dependency trees.", e);
        }
        return results;
    }


    private DependencyTree readTree(int id, List<String> tree) {
        List<DepNode> depNodes = new ArrayList<>();
        Map<BaseDepNode, Integer> headMap = new HashMap<>();
        Map<Integer, DepNode> depMap = new HashMap<>();
        int index = 0;


        List<String> header = new ArrayList<>();
        for (String line : tree) {
            if (headerPattern.matcher(line).matches()) {
                header.add(line);
            } else {
                break;
            }
        }

        tree = tree.subList(header.size(), tree.size());
        for (String line : tree) {
            depNodes.add(getDepNode(index++, line.split(FIELD_DELIM), depMap, headMap));
        }
        DepNode root = null;
        for (Map.Entry<BaseDepNode, Integer> head : headMap.entrySet()) {
            if (head.getValue() < 0) {
                root = head.getKey();
                continue;
            }
            head.getKey().head(depMap.get(head.getValue()));
        }
        DependencyTree result = new BaseDependencyTree(id, depNodes, root);
        processHeader(header, result);
        return result;
    }

    protected void processHeader(List<String> header, DependencyTree result) {
        // template method
    }

    private BaseDepNode getDepNode(int index, String[] fields,
                                   Map<Integer, DepNode> tokenMap,
                                   Map<BaseDepNode, Integer> tokenHeadMap) {
        try {
            BaseDepNode depNode = new BaseDepNode(index);
            tokenMap.put(Integer.parseInt(fields[0]), depNode);
            depNode.addFeature(FeatureType.Text, fields[1]);
            depNode.addFeature(FeatureType.Lemma, fields[2]);
            depNode.addFeature(FeatureType.Pos, fields[3]);
            depNode.addFeature(FeatureType.Dep, fields[4]);
            tokenHeadMap.put(depNode, Integer.parseInt(fields[5]));
            return depNode;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error parsing line: " + String.join(" ", fields), e);
        }
    }


    @Override
    public void writeInstances(List<DependencyTree> trees, OutputStream outputStream) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            for (DependencyTree tree : trees) {
                writer.println(treeToString(tree));
                writer.println();
                writer.flush();
            }
        }
    }

    protected String treeToString(DependencyTree tree) {
        List<String> lines = new ArrayList<>();
        int index = 0;
        for (DepNode depNode : tree.tokens()) {
            List<String> fields = new ArrayList<>();
            fields.add(Integer.toString(index));
            fields.add(depNode.feature(FeatureType.Text));
            fields.add(depNode.feature(FeatureType.Lemma));
            fields.add(depNode.feature(FeatureType.Pos));
            fields.add(Integer.toString(depNode.isRoot() ? -1 : depNode.head().index()));
            fields.add(depNode.feature(FeatureType.Dep));
            lines.add(String.join("\t", fields));
        }
        return String.join("\n", lines);
    }

}
