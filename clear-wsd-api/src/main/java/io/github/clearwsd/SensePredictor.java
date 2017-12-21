package io.github.clearwsd;

import java.util.List;

/**
 * Minimal prediction interface for word senses. For most NLP applications, tokenization will already be present. Providing a
 * pre-tokenized sentence ensures the consistency of tokenization with the internal processing performed by the WSD system
 * and should avoid headaches involved in "realigning" the predicted senses with the original input text.
 *
 * @param <S> sense type
 * @author jamesgung
 */
@FunctionalInterface
public interface SensePredictor<S> {

    /**
     * Given a tokenized sentence, return a list of senses (one per input token).
     *
     * @param sentence tokenized sentence
     * @return list of senses (one per input token)
     */
    List<S> predict(List<String> sentence);

}
