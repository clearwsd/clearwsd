package io.github.clearwsd.corpus;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import io.github.clearwsd.type.NlpInstance;

/**
 * Corpus reader/writer.
 *
 * @param <T> instance type
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

    /**
     * Create an instance iterator, useful when the input corpus is too large to fit into memory.
     *
     * @param inputStream corpus input stream
     * @return iterator over instances
     */
    default Iterator<T> instanceIterator(InputStream inputStream) {
        List<T> instances = readInstances(inputStream);
        return instances.iterator();
    }

}
