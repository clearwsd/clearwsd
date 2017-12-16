package edu.colorado.clear.wsd.feature.annotator;

import java.util.List;

import edu.colorado.clear.type.NlpInstance;
import edu.colorado.clear.wsd.feature.resource.FeatureResource;
import edu.colorado.clear.wsd.feature.resource.FeatureResourceManager;

/**
 * Base resource annotator.
 *
 * @author jamesgung
 */
public abstract class ResourceAnnotator<T, S extends NlpInstance> implements Annotator<S> {

    private static final long serialVersionUID = -4329097009920614601L;

    protected String resourceKey;
    protected FeatureResource<T, List<String>> resource;

    public ResourceAnnotator(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    @Override
    public boolean initialized() {
        return resource != null;
    }

    @Override
    public void initialize(FeatureResourceManager featureResourceManager) {
        this.resource = featureResourceManager.getResource(resourceKey);
    }

}
