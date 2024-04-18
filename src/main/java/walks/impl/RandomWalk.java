package walks.impl;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import walks.Walk;

import java.util.*;


/**
 * The random walk algorithm (see S4 from the algorithm) is basically a Markov chain.
 * There is a transition matrix, which contains the probabilities of a transition between two nodes
 * in a given graph.
 *
 */
public class RandomWalk implements Walk<Integer> {


    private final int length;

    private final Integer startVertex;

    private final Graph<Integer, DefaultEdge> graph;

    private List<Integer> path;

    public RandomWalk(int length, Integer startVertex, Graph<Integer, DefaultEdge> graph) {
        this.length = length;
        this.startVertex = startVertex;
        this.graph = graph;

        this.path = new ArrayList<>();
    }



    @Override
    public Walk<Integer> generate() {


        Integer currentVertex = startVertex;
        path.add(currentVertex);

        double transitionProbability;

        for (int i = 0; i < length - 1; i++) {

            transitionProbability = 1.0f / graph.degreeOf(currentVertex);
            List<Integer> neighbours = Graphs.neighborListOf(graph, currentVertex);

            for (int j = 0; j < neighbours.size(); j++) {

                if (Math.random() <= transitionProbability || j == neighbours.size() - 1) {

                    currentVertex = neighbours.get(j);
                    break;
                }
            }

            path.add(currentVertex);
        }

        return this;
    }

    @Override
    public Walk<Integer> removeCycles() {

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < path.size(); i++) {

            Integer currentVertex = path.get(i);

            if (!map.containsKey(currentVertex)) {

                map.put(currentVertex, i);
            } else {

                path.subList(path.indexOf(currentVertex), i).clear();
            }
        }

        return this;
    }

    @Override
    public List<Integer> getPath() {
        return path;
    }

    @Override
    public Integer getEndpoint() {
        return this.path.getLast();
    }

}
