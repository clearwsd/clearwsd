package edu.colorado.clear.wsd.feature.context;

import java.util.Collections;
import java.util.List;

import edu.colorado.clear.type.NlpInstance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Context over tokens used in feature extraction.
 *
 * @author jamesgung
 */
@Data
@Accessors(fluent = true)
@AllArgsConstructor
public class NlpContext<T extends NlpInstance> {

    private String identifier;
    private List<T> tokens;

    public NlpContext(String identifier, T token) {
        this.identifier = identifier;
        this.tokens = Collections.singletonList(token);
    }

}
