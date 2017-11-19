package edu.colorodo.clear.wsd.feature.extractor.string;

/**
 * Identity string function.
 *
 * @author jamesgung
 */
public class IdentityStringFunction extends StringFunction {

    public static final String ID = "I";

    private static final long serialVersionUID = 2135977924104760415L;

    public IdentityStringFunction() {
        id = ID;
    }

    @Override
    public String apply(String input) {
        return input;
    }

}
