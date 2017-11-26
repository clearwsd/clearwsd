package edu.colorado.clear.wsd.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.type.DependencyTree;
import edu.colorado.clear.wsd.type.FeatureType;
import lombok.AllArgsConstructor;

/**
 * Interactive test loop for evaluating/quick testing of parsers.
 *
 * @author jamesgung
 */
public class InteractiveTestLoop {

    /**
     * Test a dependency parser--interactively input utterances, and print the resulting parse tree.
     *
     * @param dependencyParser dependency parser
     */
    public static void test(DependencyParser dependencyParser, Function<DependencyTree, String> formatter) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter test input (\"EXIT\" to quit).");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if ("EXIT".equals(line)) {
                break;
            }
            System.out.println();
            for (String sentence : dependencyParser.segment(line)) {
                DependencyTree tree = dependencyParser.parse(dependencyParser.tokenize(sentence));
                System.out.println(formatter.apply(tree));
                System.out.println();
            }
        }
    }

    public static void test(DependencyParser dependencyParser, String key) {
        test(dependencyParser, new InlineFormatter(key));
    }

    public static void test(DependencyParser dependencyParser, List<String> keys) {
        test(dependencyParser, new InlineFormatter(keys));
    }

    @AllArgsConstructor
    public static class InlineFormatter implements Function<DependencyTree, String> {

        private List<String> keys;

        public InlineFormatter(String key) {
            this.keys = Collections.singletonList(key);
        }

        @Override
        public String apply(DependencyTree dependencyTree) {
            return dependencyTree.tokens().stream().map(t -> {
                String text = t.feature(FeatureType.Text);
                List<String> results = keys.stream()
                        .map(t::feature)
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
                if (results.size() > 0) {
                    return String.format("%s%s", text, results);
                } else {
                    return text;
                }
            }).collect(Collectors.joining("\n"));
        }
    }

}
