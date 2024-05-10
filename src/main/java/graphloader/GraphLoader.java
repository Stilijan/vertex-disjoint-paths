package graphloader;

import exceptions.GraphReadingException;
import org.jgrapht.Graph;

/**
 * A component, which creates a JGraphT graph instance by a given input file.
 *
 * @param <V> the type of vertices to be loaded.
 * @param <E> the type of edges to be loaded.
 */
public interface GraphLoader<V,E> {

    /**
     * Returns a newly created graph instance.
     *
     * @return the created graph instance
     * @throws GraphReadingException if there is an error while reading the input file
     */
    Graph<V, E> loadGraph() throws GraphReadingException;
}
