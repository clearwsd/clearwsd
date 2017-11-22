package edu.colorado.clear.wsd.feature.resource;

/**
 * Feature resource manager.
 *
 * @author jamesgung
 */
public interface FeatureResourceManager {

    <K, T> FeatureResource<K, T> getResource(String key);

}