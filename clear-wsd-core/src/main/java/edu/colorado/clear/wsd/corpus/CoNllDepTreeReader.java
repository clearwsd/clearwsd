package edu.colorado.clear.wsd.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.colorado.clear.type.DepNode;
import edu.colorado.clear.type.DepTree;
import edu.colorado.clear.type.FeatureType;
import edu.colorado.clear.wsd.type.DefaultDepNode;
import edu.colorado.clear.wsd.type.DefaultDepTree;

/**
 * CoNLL-U-style dependency tree corpus reader.
 *
 * @author jamesgung
 */
public class CoNllDepTreeReader implements CorpusReader<DepTree> {

    private static final String FIELD_DELIM = "\t";

    private Pattern headerPattern = Pattern.compile("^#.*$");

    @Override
    public List<DepTree> readInstances(InputStream inputStream) {
        List<DepTree> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<String> tree;
            while ((tree = readTree(reader)) != null) {
                results.add(readTree(results.size(), tree));
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading dependency trees.", e);
        }
        return results;
    }

    @Override
    public Iterator<DepTree> instanceIterator(InputStream inputStream) {
        return new TreeIterator(inputStream);
    }

    private List<String> readTree(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.isEmpty()) {
                if (lines.size() > 0) {
                    return lines;
                }
                continue;
            }
            lines.add(currentLine);
        }
        if (lines.size() > 0) {
            return lines;
        }
        return null;
    }

    private DepTree readTree(int id, List<String> tree) {
        List<DepNode> depNodes = new ArrayList<>();
        Map<DefaultDepNode, Integer> headMap = new HashMap<>();
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
        for (Map.Entry<DefaultDepNode, Integer> head : headMap.entrySet()) {
            if (head.getValue() < 0) {
                root = head.getKey();
                continue;
            }
            head.getKey().head(depMap.get(head.getValue()));
        }
        DepTree result = new DefaultDepTree(id, depNodes, root);
        processHeader(header, result);
        return result;
    }

    protected void processHeader(List<String> header, DepTree result) {
        // template method
    }

    private DefaultDepNode getDepNode(int index, String[] fields,
                                      Map<Integer, DepNode> tokenMap,
                                      Map<DefaultDepNode, Integer> tokenHeadMap) {
        try {
            DefaultDepNode depNode = new DefaultDepNode(index);
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
    public void writeInstances(List<DepTree> trees, OutputStream outputStream) {
        writeDependencyTrees(trees, outputStream);
    }

    private class TreeIterator implements Iterator<DepTree>, AutoCloseable {

        private List<DepTree> treeCache;
        private BufferedReader reader;
        private int cacheSize;

        private int index = 0;
        private int current = 0;
        private boolean closed = false;

        TreeIterator(InputStream inputStream, int cacheSize) {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            this.cacheSize = cacheSize;
            treeCache = new ArrayList<>(cacheSize);
            index = 0;
            current = 0;
        }

        TreeIterator(InputStream inputStream) {
            this(inputStream, 1000);
        }

        @Override
        public boolean hasNext() {
            if (current == treeCache.size()) {
                populateCache();
            }
            return current < treeCache.size();
        }

        @Override
        public DepTree next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return treeCache.get(current++);
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }

        private void populateCache() {
            current = 0;
            treeCache.clear();
            if (closed) {
                return;
            }
            try {
                List<String> tree;
                while ((tree = readTree(reader)) != null && treeCache.size() < cacheSize) {
                    treeCache.add(readTree(index++, tree));
                }
                if (tree == null) {
                    closed = true;
                    reader.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Write a list of {@link DepTree} to an {@link OutputStream} in a CoNLL-style format.
     *
     * @param trees        dependency trees
     * @param outputStream output stream
     */
    public static void writeDependencyTrees(List<DepTree> trees, OutputStream outputStream) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writeDependencyTrees(trees, writer);
        }
    }

    /**
     * Writes a list of {@link DepTree DepTrees} with a given {@link PrintWriter} in CoNLL-format without closing.
     *
     * @param trees  dependency trees
     * @param writer writer
     */
    public static void writeDependencyTrees(List<DepTree> trees, PrintWriter writer) {
        for (DepTree tree : trees) {
            writer.println(treeToString(tree));
            writer.println();
        }
        writer.flush();
    }

    /**
     * Convert an {@link DepTree} to a tab-separated CoNLL-style string (index word lemma pos dep head).
     *
     * @param tree  dependency tree
     * @param extra extra features to be serialized
     * @return CoNLL-style string
     */
    public static String treeToString(DepTree tree, String... extra) {
        List<String> lines = new ArrayList<>();
        for (DepNode depNode : tree) {
            List<String> fields = new ArrayList<>();
            fields.add(Integer.toString(depNode.index()));
            fields.add(depNode.feature(FeatureType.Text));
            fields.add(depNode.feature(FeatureType.Lemma));
            fields.add(depNode.feature(FeatureType.Pos));
            fields.add(depNode.feature(FeatureType.Dep));
            fields.add(Integer.toString(depNode.isRoot() ? -1 : depNode.head().index()));
            fields.add(Arrays.stream(extra).filter(s -> depNode.feature(s) != null)
                    .map(s -> s + "=" + depNode.feature(s))
                    .collect(Collectors.joining("|")));
            lines.add(String.join("\t", fields));
        }
        return String.join("\n", lines);
    }

}
