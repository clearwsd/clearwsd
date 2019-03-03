package io.github.clearwsd.app;

import com.google.common.collect.Sets;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import io.github.clearwsd.parser.StanfordTokenizer;

/**
 * Utility for quickly generating annotations.
 *
 * @author jgung
 */
public class AnnotationUtil {

    public static void main(String[] args) throws Throwable {
        String lemma = "go";
        String outputPath = "data/verbnet/" + lemma + "-aug.txt";
        Scanner scanner = new Scanner(System.in);
        StanfordTokenizer tokenizer = new StanfordTokenizer();

        Set<String> words = Sets.newHashSet("go", "went", "goes", "going", "gone");
        String sense = "51.1-1";
        int sent = 0;
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
                    if (words.contains(tokens.get(i))) {
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
