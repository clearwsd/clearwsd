package edu.colorodo.clear.wsd.feature.extractor.string;

/**
 * Identity string function.
 *
 * @author jamesgung
 */
public class IdentityStringFunction extends StringFunction {

    public static final String ID = "I";

    public IdentityStringFunction() {
        id = ID;
    }

    @Override
    public String apply(String input) {
        return input;
    }

}
