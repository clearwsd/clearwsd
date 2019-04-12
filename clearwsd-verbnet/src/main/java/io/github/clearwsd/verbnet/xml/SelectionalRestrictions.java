package io.github.clearwsd.verbnet.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * VerbNet selectional restrictions.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SelectionalRestrictions.ROOT_NAME)
public class SelectionalRestrictions {

    static final String ROOT_NAME = "SELRESTRS";

    @XmlAttribute(name = "logic")
    private String logic = "";

    @XmlElement(name = SelectionalRestriction.ROOT_NAME)
    private List<SelectionalRestriction> selectionalRestriction = new ArrayList<>();

    @XmlElement(name = ROOT_NAME)
    private List<SelectionalRestrictions> selectionalRestrictions = new ArrayList<>();

}
