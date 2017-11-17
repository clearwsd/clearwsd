package edu.colorodo.clear.wsd.feature.annotator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.colorodo.clear.wsd.type.NlpInstance;

/**
 * NLP annotator, used to apply new features to a given input (typically as a pre-processing step).
 *
 * @author jamesgung
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, include=JsonTypeInfo.As.PROPERTY, property="@c")
public interface Annotator<T extends NlpInstance> {

    T annotate(T instance);

}
