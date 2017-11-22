package edu.colorado.clear.wsd.corpus;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import edu.colorado.clear.wsd.type.NlpInstance;

/**
 * Corpus reader/writer.
 *
 * @author jamesgung
 */
public interface CorpusReader<T extends NlpInstance> {

    /**
     * Read all instances from a given {@link InputStream}.
     *
     * @param inputStream instance input stream
     * @return list of instances
     */
    List<T> readInstances(InputStream inputStream);

    /**
     * Write a list of instances to a given output stream.
     *
     * @param instances    list of instances
     * @param outputStream target output
     */
    void writeInstances(List<T> instances, OutputStream outputStream);

}
