package edu.colorado.clear.type;

/**
 * NLP focus/sequence pair, such as for classification of a word ({@link NlpInstance}) within a sentence ({@link NlpSequence} of
 * words). Used mostly to organize relevant information for feature extraction where overall features of the container sequence
 * are important, in addition to information about the position of the target within the sequence. Especially useful for
 * token-level classification, such as in sense disambiguation.
 *
 * @param <T> focus type
 * @param <S> sequence type
 * @author jamesgung
 */
public interface NlpFocus<T extends NlpInstance, S extends NlpSequence<T>> extends NlpSequence<T> {

    /**
     * Return the focus within the sequence.
     */
    T focus();

    /**
     * Return the overall sequence containing the focus.
     */
    S sequence();

}
