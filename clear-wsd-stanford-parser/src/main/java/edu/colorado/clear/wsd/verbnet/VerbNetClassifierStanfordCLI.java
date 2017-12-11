package edu.colorado.clear.wsd.verbnet;

import edu.colorado.clear.wsd.parser.DependencyParser;
import edu.colorado.clear.wsd.parser.StanfordDependencyParser;
import lombok.NoArgsConstructor;

/**
 * VerbNet classifier CLI with Stanford Parser.
 *
 * @author jamesgung
 */
@NoArgsConstructor
public class VerbNetClassifierStanfordCLI extends VerbNetClassifierCLI {

    private VerbNetClassifierStanfordCLI(String[] args) {
        super(args);
    }

    @Override
    protected DependencyParser parser() {
        return new StanfordDependencyParser();
    }

    public static void main(String[] args) {
        new VerbNetClassifierStanfordCLI(args).run();
    }

}
