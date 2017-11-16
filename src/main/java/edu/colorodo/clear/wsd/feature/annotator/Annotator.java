package edu.colorodo.clear.wsd.feature.annotator;

import edu.colorodo.clear.wsd.type.NlpInstance;

/**
 * NLP annotator, used to apply new features to a given input (typically as a pre-processing step).
 *
 * @author jamesgung
 */
public interface Annotator<T extends NlpInstance> {

    T annotate(T instance);

}
