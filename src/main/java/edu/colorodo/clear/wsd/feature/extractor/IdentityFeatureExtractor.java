package edu.colorodo.clear.wsd.feature.extractor;

/**
 * Identity string feature extractor.
 *
 * @author jamesgung
 */
public class IdentityFeatureExtractor<K> implements FeatureExtractor<K, String> {

    public static final String ID = "ID";

    private static final long serialVersionUID = 2852870228451742787L;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String extract(K instance) {
        return instance.toString();
    }

}
