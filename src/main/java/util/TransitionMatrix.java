package util;

import org.jgrapht.Graph;

import java.util.Arrays;
import java.util.HashMap;

public class TransitionMatrix<V> {

    private HashMap<V, Integer> vertexIndexMap;

    private Double[][] matrix;

    private final Graph<V, ?> graph;


    public TransitionMatrix(Graph<V, ?> graph) {

        this.graph = graph;

        this.vertexIndexMap = new HashMap<>();
        this.matrix = new Double[graph.vertexSet().size()][graph.vertexSet().size()];


        loadVertexIndexMap();
        loadMatrix();

    }

    private void loadMatrix() {

        for (V targetVertex : graph.vertexSet()) {

            int i = vertexIndexMap.get(targetVertex);

            for (V sourceVertex : graph.vertexSet()) {

                int j = vertexIndexMap.get(sourceVertex);
                matrix[i][j] =
                    graph.containsEdge(sourceVertex, targetVertex) ? 1.0 / graph.degreeOf(sourceVertex) : 0.0;
            }
        }
    }


    private void loadVertexIndexMap() {

        int i = 0;
        for (V vertex : this.graph.vertexSet()) {

            vertexIndexMap.put(vertex, i++);
        }
    }

    public double getTransitionProbability(V from, V to) {

        int i = vertexIndexMap.get(to);
        int j = vertexIndexMap.get(from);

        return this.matrix[i][j];
    }

    public State<V> calculateNextState(State<V> prevState) {

        double[] nextStateProbabilities =
            new double[graph.vertexSet().size()];


        for (int i = 0; i < nextStateProbabilities.length; i++) {

            for (int j = 0; j < matrix[0].length; j++) {

                nextStateProbabilities[i] += matrix[i][j] * prevState.getProbabilities()[j];
            }
        }

        return new State<>(graph, nextStateProbabilities);
    }


    public State<V> calculateNextStateAfterNTransitions(State<V> initState, int n) {

        State<V> currState = initState;

        for (int i = 0; i < n; i++) {

            currState = calculateNextState(currState);
        }

        return currState;
    }

    @Override
    public String toString() {

        StringBuilder res = new StringBuilder();

        for (Double[] row : matrix) {

            res.append(Arrays.deepToString(row)).append("\n");
        }

        return res.toString();
    }
}
