package io.github.clearwsd.app;

import com.google.common.collect.Sets;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import io.github.clearwsd.parser.StanfordTokenizer;
import io.github.clearwsd.verbnet.VerbNetSenseInventory;
import lombok.Getter;

/**
 * Utility for quickly generating annotations.
 *
 * @author jgung
 */
public class AnnotationUtil {

    private static Map<String, Set<String>> LEMMA_MAPPINGS = new HashMap<String, Set<String>>() {
        {
            put("make", Sets.newHashSet("make", "makes", "made", "making"));
            put("take", Sets.newHashSet("take", "takes", "take", "took", "taking", "taken"));
            put("run", Sets.newHashSet("run", "ran", "running"));
            put("come", Sets.newHashSet("come", "coming", "came"));
        }
    };

    @Getter
    @Parameter(names = {"-verb", "-v"}, description = "Target lemma, e.g. 'take'", required = true)
    private String lemma;
    @Getter
    @Parameter(names = {"-output", "-o"}, description = "Output path, e.g. 'take-annotations.txt'", required = true)
    private String outputPath;

    private AnnotationUtil(String... args) {
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

    public static void main(String[] args) throws Throwable {
        AnnotationUtil util = new AnnotationUtil(args);
        String lemma = util.getLemma();
        String outputPath = util.getOutputPath();

        Scanner scanner = new Scanner(System.in);
        StanfordTokenizer tokenizer = new StanfordTokenizer();

        Set<String> words = LEMMA_MAPPINGS.get(lemma);
        VerbNetSenseInventory vn = new VerbNetSenseInventory();
        Set<String> senses = vn.senses(lemma);
        String sense = vn.defaultSense(lemma);

        System.out.println(lemma);
        System.out.println(String.join(", ", senses));
        System.out.println("Using sense: " + sense);

        int sent = 0;
        System.out.println("Writing to " + outputPath);
        try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, true)))) {
            do {
                System.out.print(">> ");
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    break;
                }
                if (line.startsWith(">")) {
                    sense = line.replaceAll(">+\\s*", "");
                    System.out.println("Changing sense to " + sense);
                    continue;
                }
                List<String> tokens = tokenizer.tokenize(line);
                int index = -1;
                for (int i = 0; i < tokens.size(); ++i) {
                    if (words.contains(tokens.get(i).toLowerCase())) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    printWriter.println(String.format("%s %d %d %s %s\t%s", outputPath, sent, index, lemma, sense,
                            String.join(" ", tokens)));
                    printWriter.flush();
                    ++sent;
                }
            } while (true);
        }

    }

}
