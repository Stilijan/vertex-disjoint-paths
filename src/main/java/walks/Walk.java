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

    protected Walk() {}


    public Walk<V> removeCycles() {

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

    public List<V> getPath() {
        return path;
    }

    public void setPath(List<V> path) {
        this.path = path;
    }

    public V getEndpoint() {

        return path.getLast();
    }

    public V getStartVertex() {
        return startVertex;
    }

    public abstract void generate();

    @Override
    public String toString() {
        return this.path.toString();
    }
}
