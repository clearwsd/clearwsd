package io.github.clearwsd.verbnet;

/**
 * VerbNet thematic role.
 *
 * @author jgung
 */
public interface ThematicRole {

    String type();

    SelResDescription restrictions();

}
