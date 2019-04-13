package io.github.clearwsd.verbnet;

import java.util.List;

/**
 * Syntactic restrictions description.
 *
 * @author jamesgung
 */
public interface SyntResDescription {

    String logic();

    List<SyntRes> restrictions();
}
