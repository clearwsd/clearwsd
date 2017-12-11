package edu.colorado.clear.wsd.feature;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import static edu.colorado.clear.wsd.feature.util.FeatureUtils.FEATURE_ID_SEP;

/**
 * String feature, containing an ID and a value.
 *
 * @author jamesgung
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class StringFeature implements Serializable {

    private static final long serialVersionUID = -4634062576141187574L;

    private String id;
    private String value;

    @Override
    public String toString() {
        return id + FEATURE_ID_SEP + value;
    }
}
