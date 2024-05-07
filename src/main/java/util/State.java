package util;

import org.jgrapht.Graph;

import java.util.*;

public class State<V> {


    private final double[] probabilities;

    private Map<V, Integer> vertexIndexMap;

    private final Graph<V, ?> graph;

    /**
     * is used for setting a next state of a walk
     *
     * @param graph the graph.
     * @param probabilities a list of the probabilities of all vertices in the graph.
     */
    public State(Graph<V, ?> graph, double[] probabilities) {

        this.graph = graph;
        this.probabilities = probabilities;
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    /**
     * creates an initial state of a walk
     *
     * @param graph the graph, which the startVertex is located in.
     * @param startVertex the starting point of a walk
     */
    public State(Graph<V, ?> graph, V startVertex) {
        this.graph = graph;
        this.probabilities = new double[graph.vertexSet().size()];
        this.vertexIndexMap = new HashMap<>();

        loadVertexIndexMap();

        int index = vertexIndexMap.get(startVertex);

        this.probabilities[index] = 1.0;
    }

    public double getProbabilityToReachVertex(V vertex) {

        int index = this.vertexIndexMap.get(vertex);

        return probabilities[index];
    }

    private void loadVertexIndexMap() {

        int i = 0;
        for (V vertex : this.graph.vertexSet()) {

            vertexIndexMap.put(vertex, i++);
        }
    }

    @Override
    public String toString() {

        StringBuilder res = new StringBuilder("[");

        for (int i = 0; i < probabilities.length; i++) {

            res.append(probabilities[i]);
            res.append(((i == probabilities.length - 1) ? " " : ", "));

        }

        return res + "]";
    }
}
