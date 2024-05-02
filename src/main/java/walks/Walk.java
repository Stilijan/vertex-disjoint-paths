package walks;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public abstract class Walk<V> {

    protected List<V> path;
    protected V startVertex;
    protected Graph<V, DefaultWeightedEdge> graph;

    protected Walk(Graph<V, DefaultWeightedEdge> graph, V startVertex) {

        this.graph = graph;
        this.startVertex = startVertex;
        this.path = new ArrayList<>();
    }

    protected Walk() {

        this.path = new ArrayList<>();
    }


    /**
     * Removes possible cycles from the path.
     *
     * @return this walk with the removed cycles.
     */
    public Walk<V> withRemovedCycles() {

        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < path.size(); i++) {

            V currentVertex = path.get(i);

            if (!map.containsKey(currentVertex)) {

                map.put(currentVertex, i);
            } else {

                path.subList(path.indexOf(currentVertex), i).clear();
            }
        }

        return this;
    }

    /**
     * Reverses the path in a walk.
     *
     * @return this walk with the reversed path.
     */
    public Walk<V> reversed() {

        this.path = this.path.reversed();
        return this;
    }

    public List<V> getPath() {
        return path;
    }

    public void setPath(List<V> path) {
        this.path = path;
    }

    public V getEndVertex() {
        return path != null ? path.getLast() : null;
    }

    public V getStartVertex() {
        return path != null ? path.getFirst() : startVertex;
    }

    /**
     * An abstract method for generating the path of a walk.
     */
    public abstract void generateWalk();

    @Override
    public String toString() {
        return this.path.toString();
    }
}
