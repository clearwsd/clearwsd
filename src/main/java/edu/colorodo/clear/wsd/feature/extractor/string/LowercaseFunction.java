package edu.colorodo.clear.wsd.feature.extractor.string;

/**
 * Lowercase string function.
 *
 * @author jamesgung
 */
public class LowercaseFunction extends StringFunction {

    public static final String ID = "LL";

    public LowercaseFunction() {
        id = ID;
    }

    @Override
    public String apply(String input) {
        return input.toLowerCase();
    }

}
