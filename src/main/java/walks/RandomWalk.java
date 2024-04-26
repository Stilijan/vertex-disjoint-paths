package walks;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;


/**
 * The random walk algorithm (see S4 from the algorithm) is basically a Markov chain.
 * There is a transition matrix, which contains the probabilities of a transition between two nodes
 * in a given graph.
 */
public class RandomWalk<V> extends Walk<V> {

    private int length;


    public RandomWalk(Graph<V, DefaultWeightedEdge> graph, V startVertex, int length) {

        super(graph, startVertex);
        this.length = length;
    }



    @Override
    public void generate() {


        V currentVertex = startVertex;
        path.add(currentVertex);

        double transitionProbability;

        for (int i = 0; i < length - 1; i++) {

            transitionProbability = 1.0f / graph.degreeOf(currentVertex);
            List<V> neighbours = Graphs.neighborListOf(graph, currentVertex);

            for (int j = 0; j < neighbours.size(); j++) {

                if (Math.random() <= transitionProbability || j == neighbours.size() - 1) {

                    currentVertex = neighbours.get(j);
                    break;
                }
            }

            path.add(currentVertex);
        }

    }


}
