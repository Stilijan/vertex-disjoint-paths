package walks;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;

public interface Walk<V> {



    /**
     * generates a list of vertices corresponding to a path.
     *
     * @return the walk with the generated path.
     */
    Walk<V> generate();

    /**
     * removes cycles from the path.
     *
     * @return the walk with the removed cycles from the path.
     */
    Walk<V> removeCycles();


    /**
     * a simple getter for the path property.
     *
     * @return the path of the walk.
     */
    List<V> getPath();


    /**
     * returns the last vertex of a walk.
     *
     * @return the endpoint vertex of a walk.
     */
    V getEndpoint();



}
