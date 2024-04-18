package graphLoader;

import exceptions.GraphFileNotFoundException;
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
     * A method, which creates the graph instance by a given .gr file.
     *
     * @return the created graph instance
     * @throws GraphReadingException if there is an error while reading the input file
     */
    Graph<V, E> loadGraph() throws  GraphReadingException;
}
