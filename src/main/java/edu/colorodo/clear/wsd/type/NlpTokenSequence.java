package edu.colorodo.clear.wsd.type;

import java.util.List;

/**
 * List of tokens, such as a sentence, dependency tree, or document.
 *
 * @author jamesgung
 */
public interface NlpTokenSequence<T extends NlpInstance> extends NlpInstance {

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
