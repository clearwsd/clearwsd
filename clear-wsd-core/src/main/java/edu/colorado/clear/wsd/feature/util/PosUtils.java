package edu.colorado.clear.wsd.feature.util;

/**
 * Penn Treebank part-of-speech (POS) tag utilities.
 *
 * @author jamesgung
 */
public class PosUtils {

    public static boolean isNoun(String pos) {
        return pos.startsWith("NN") || pos.equals("PRP") || pos.equals("WP");
    }

    public static boolean isVerb(String pos) {
        return pos.startsWith("VB");
    }

    public static boolean isAdjective(String pos) {
        return pos.startsWith("JJ");
    }

    public static boolean isAdverb(String pos) {
        return pos.startsWith("RB") || pos.equals("WRB");
    }

}
