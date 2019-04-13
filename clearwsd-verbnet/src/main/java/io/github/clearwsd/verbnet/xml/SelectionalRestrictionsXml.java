package io.github.clearwsd.verbnet.xml;

import io.github.clearwsd.verbnet.SelRes;
import io.github.clearwsd.verbnet.SelResDescription;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * XML binding implementation of {@link SelResDescription}.
 *
 * @author jgung
 */
@Data
@Accessors(fluent = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = SelectionalRestrictionsXml.ROOT_NAME)
public class SelectionalRestrictionsXml implements SelResDescription {

    static final String ROOT_NAME = "SELRESTRS";

    @XmlAttribute(name = "logic")
    private String logic = "";

    @XmlElement(name = SelectionalRestrictionXml.ROOT_NAME)
    private List<SelectionalRestrictionXml> selectionalRestriction = new ArrayList<>();

    @XmlElement(name = ROOT_NAME)
    private List<SelectionalRestrictionsXml> selectionalRestrictions = new ArrayList<>();

    @Override
    public List<SelRes> restrictions() {
        return selectionalRestriction.stream().map(res -> (SelRes) res).collect(Collectors.toList());
    }

    @Override
    public List<SelResDescription> descriptions() {
        return selectionalRestrictions.stream().map(res -> (SelResDescription) res).collect(Collectors.toList());
    }
}
