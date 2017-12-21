package io.github.clearwsd.feature.extractor;

/**
 * Identity string feature extractor.
 *
 * @author jamesgung
 */
public class IdentityFeatureExtractor<K> implements StringExtractor<K> {

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
