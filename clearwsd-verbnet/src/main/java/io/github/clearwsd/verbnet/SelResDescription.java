package io.github.clearwsd.verbnet;

import java.util.List;

/**
 * VerbNet selectional restrictions description.
 *
 * @author jgung
 */
public interface SelResDescription {

    String logic();

    List<SelRes> restrictions();

    List<SelResDescription> descriptions();

}
