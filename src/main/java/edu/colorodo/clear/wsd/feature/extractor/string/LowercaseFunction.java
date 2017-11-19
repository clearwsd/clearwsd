package edu.colorodo.clear.wsd.feature.extractor.string;

/**
 * Lowercase string function.
 *
 * @author jamesgung
 */
public class LowercaseFunction extends StringFunction {

    public static final String ID = "LL";

    private static final long serialVersionUID = 4625756530549395857L;

    public LowercaseFunction() {
        id = ID;
    }

    @Override
    public String apply(String input) {
        return input.toLowerCase();
    }

}
