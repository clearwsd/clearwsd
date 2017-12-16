package edu.colorado.clear.wsd.type;

/**
 * NLP focus/sequence pair, such as for classification of a word ({@link NlpInstance}) within a sentence ({@link NlpSequence} of
 * words).
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
