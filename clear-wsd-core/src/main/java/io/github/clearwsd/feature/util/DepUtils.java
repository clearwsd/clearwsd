package io.github.clearwsd.feature.util;

/**
 * Universal dependency utility methods.
 *
 * @author jamesgung
 */
public class DepUtils {

    private DepUtils() {
    }

    public static boolean isAux(String rel) {
        return "aux".equalsIgnoreCase(rel);
    }

}
