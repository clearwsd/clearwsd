package edu.colorado.clear.wsd.type;

import java.util.List;

/**
 * List of tokens ({@link NlpInstance NlpInstances}), such as a sentence, dependency tree, or document.
 *
 * @author jamesgung
 */
public interface NlpSequence<T extends NlpInstance> extends NlpInstance, Iterable<T> {

    /**
     * List of tokens in this NLP sequence.
     */
    List<T> tokens();

    /**
     * Get the token at a specified index.
     *
     * @param index token index
     * @return token at index
     */
    T get(int index);

    /**
     * Number of tokens in this instance.
     */
    int size();

}
