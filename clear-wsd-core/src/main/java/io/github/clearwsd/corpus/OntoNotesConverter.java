package io.github.clearwsd.corpus;

import com.google.common.collect.Lists;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.clearwsd.corpus.semlink.VerbNetReader;
import io.github.clearwsd.corpus.semlink.VerbNetReader.VerbNetInstance;
import io.github.clearwsd.parser.NlpTokenizer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Converter to convert OntoNotes data to an inline format for WSD.
 *
 * @author jamesgung
 */
@Setter
@NoArgsConstructor
@Accessors(fluent = true)
public class OntoNotesConverter {

    @Parameter(names = {"-input", "-i"}, description = "Input path", required = true)
    private String inputPath;
    @Parameter(names = {"-output", "-o"}, description = "Output path", required = true)
    private String outputPath;
    @Parameter(names = "-parseExt", description = "Parse file extension")
    private String parseExt = ".parse";
    @Parameter(names = "-senseExt", description = "Sense file extension")
    private String senseExt = ".sense";
    @Parameter(names = "-lemmaFilter", description = "Regex filter for specific lemma types")
    private String lemmafilter = ".*-v";

    private OntoNotesConverter(String... args) {
        JCommander cmd = new JCommander(this);
        cmd.setProgramName(this.getClass().getSimpleName());
        try {
            cmd.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }
    }

    private void run() {
        File outputFile = new File(outputPath).getAbsoluteFile();
        if (!outputFile.getParentFile().exists()) {
            if (!outputFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Unable to write to directory at " + outputFile.getParent());
            }
        }
        try (PrintWriter writer = new PrintWriter(outputPath)) {
            for (VerbNetInstance instance : new OntoNotesConverter().getInstances(Paths.get(inputPath))) {
                writer.println(VerbNetReader.VerbNetInstanceParser.toString(instance));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to write to output path at " + outputPath + ": " + e.getMessage(), e);
        }
    }

    private List<Path> getParseFiles(Path directory) {
        try {
            return Files.find(directory, Integer.MAX_VALUE, (path, atts) -> path.toString().endsWith(parseExt))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a list of {@link VerbNetInstance instances} from OntoNotes files in a given {@link Path}.
     *
     * @param directory OntoNotes path
     * @return list of sense instances
     */
    public List<VerbNetInstance> getInstances(Path directory) {
        Predicate<String> filter = Pattern.compile(lemmafilter).asPredicate();
        List<VerbNetInstance> instances = new ArrayList<>();
        try {
            for (Path path : getParseFiles(directory)) {
                Path sensePath = Paths.get(path.toString().replaceAll(parseExt + "$", senseExt));
                if (Files.exists(sensePath)) {
                    String trees = new String(Files.readAllBytes(path));
                    Map<Integer, String> sentenceMap = new HashMap<>();
                    for (TreebankTreeNode treeNode : parse(trees)) {
                        sentenceMap.put(sentenceMap.size(), treeNode.toString());
                    }
                    String senses = new String(Files.readAllBytes(sensePath));
                    for (String senseLine : senses.split("[\\r\\n]+")) {
                        String[] fields = senseLine.split(" ");
                        String lemma = fields[3];
                        if (!filter.test(lemma)) {
                            continue;
                        }
                        VerbNetInstance instance = new VerbNetInstance()
                                .path(fields[0])
                                .sentence(Integer.parseInt(fields[1]))
                                .token(Integer.parseInt(fields[2]))
                                .lemma(lemma)
                                .label(fields.length == 6 ? fields[5] : fields[4]);
                        instance.originalText(sentenceMap.getOrDefault(instance.sentence(), ""));
                        instances.add(instance);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading instances: " + e.getMessage(), e);
        }
        return instances;
    }

    static List<TreebankTreeNode> parse(String sentence) {
        NlpTokenizer tokenizer = new TreebankTokenizer();
        return tokenizer.segment(sentence).stream().map(tree -> {
            Stack<TreebankTreeNode> stack = new Stack<>();
            int index = 0;
            TreebankTreeNode current = new TreebankTreeNode(index++).label(TreebankTreeNode.ROOT);
            ParserState state = ParserState.OUTSIDE;
            for (String token : tokenizer.tokenize(tree)) {
                switch (state) {
                    case OUTSIDE:
                        switch (token) {
                            case "(":
                                stack.push(current);
                                current = current.addChild(new TreebankTreeNode(index++));
                                state = ParserState.NEW;
                                break;
                            case ")":
                                current = stack.pop();
                                break;
                        }
                        break;
                    case NEW:
                        switch (token) {
                            case "(":
                            case ")":
                                throw new IllegalStateException("Unexpected node boundary, expecting a tag, but got " + token);
                            default:
                                current.label(token);
                                state = ParserState.INSIDE;
                        }
                        break;
                    case INSIDE:
                        switch (token) {
                            case "(":
                                stack.push(current);
                                current = current.addChild(new TreebankTreeNode(index++));
                                state = ParserState.NEW;
                                break;
                            case ")":
                                current = stack.pop();
                                break;
                            default:
                                current.value(token);
                        }
                }
            }
            return current;
        }).collect(Collectors.toList());
    }

    private enum ParserState {
        OUTSIDE,
        NEW,
        INSIDE
    }

    static class TreebankTokenizer implements NlpTokenizer {

        private Pattern splitter = Pattern.compile("(?<=[)(])|(?=[)(])|\\s");

        @Override
        public List<String> segment(String input) {
            return Arrays.asList(input.split("\\n\\n+"));
        }

        @Override
        public List<String> tokenize(String sentence) {
            return Arrays.stream(splitter.split(sentence))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    static class TreebankTreeNode {

        public static final String ROOT = "ROOT";

        private int id;
        private String label;
        private String value;
        private List<TreebankTreeNode> children = new ArrayList<>();

        TreebankTreeNode(int id) {
            this.id = id;
        }

        /**
         * Return true if this node has no children.
         */
        public boolean isLeaf() {
            return children().size() == 0;
        }

        /**
         * Returns true if this node has no textual realization (is an empty node).
         */
        public boolean isNull() {
            return label.equals("-NONE-");
        }

        /**
         * Return all child nodes for the current node (none, if this is a leaf node).
         */
        public List<TreebankTreeNode> allChildren() {
            Set<TreebankTreeNode> results = new HashSet<>();
            Stack<TreebankTreeNode> stack = new Stack<>();
            stack.push(this);
            while (!stack.isEmpty()) {
                TreebankTreeNode current = stack.pop();
                if (!results.contains(current)) {
                    results.add(current);
                    if (current.children.size() == 0) {
                        results.add(current);
                    }
                    stack.addAll(Lists.reverse(current.children));
                }
            }
            return results.stream()
                    .sorted(Comparator.comparingInt(t -> t.id))
                    .collect(Collectors.toList());
        }

        private TreebankTreeNode addChild(TreebankTreeNode child) {
            children.add(child);
            return child;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            TreebankTreeNode that = (TreebankTreeNode) other;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return allChildren().stream()
                    .filter(TreebankTreeNode::isLeaf)
                    .filter(t -> !t.isNull())
                    .map(TreebankTreeNode::value)
                    .collect(Collectors.joining(" "));
        }
    }

    public static void main(String[] args) {
        new OntoNotesConverter(args).run();
    }

}
