package io.github.clearwsd.corpus.semeval;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Abstract SemEval element.
 *
 * @author jamesgung
 */
@NoArgsConstructor
public abstract class SemevalElement {

    @Getter
    @XmlValue
    protected String value;
    @Getter
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<>();

    SemevalElement(String value) {
        this.value = value;
    }

}
