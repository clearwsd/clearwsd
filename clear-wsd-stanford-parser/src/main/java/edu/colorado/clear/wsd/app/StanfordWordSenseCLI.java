package edu.colorado.clear.wsd.app;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.StanfordDependencyParser;

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
    protected DependencyParser parser() {
        return new StanfordDependencyParser();
    }

    public static void main(String[] args) {
        new StanfordWordSenseCLI(args).run();
    }

}
