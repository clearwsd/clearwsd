package io.github.clearwsd.app;

import io.github.clearwsd.parser.NlpParser;
import io.github.clearwsd.parser.StanfordDependencyParser;

/**
 * Word sense classifier CLI with Stanford Parser.
 *
 * @author jamesgung
 */
public class StanfordWordSenseCLI extends WordSenseCLI {

    private StanfordWordSenseCLI(String[] args) {
        super(args);
    }

    @Override
    protected NlpParser parser() {
        return new StanfordDependencyParser();
    }

    public static void main(String[] args) {
        new StanfordWordSenseCLI(args).run();
    }

}
