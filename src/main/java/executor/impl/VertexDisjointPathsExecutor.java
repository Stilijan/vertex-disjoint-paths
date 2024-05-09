package executor.impl;

import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import exceptions.AlgorithmInterruptedException;
import exceptions.InvalidAlgorithmResultException;
import executor.Executor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import util.VertexPairs;

public class VertexDisjointPathsExecutor implements Executor {

    private static final Logger LOGGER = LogManager.getLogger(VertexDisjointPathsExecutor.class);
    private int tries;
    private final Graph<Integer, DefaultWeightedEdge> graph;
    private final VertexPairs<Integer> pairs;

    public VertexDisjointPathsExecutor(Graph<Integer, DefaultWeightedEdge> graph, VertexPairs<Integer> pairs) {

        this.tries = 1;
        this.graph = graph;
        this.pairs = pairs;
    }


    @Override
    public void executeAlgorithm() {

        VertexDisjointPaths vertexDisjointPaths = null;
        boolean success = false;

        while (!success) {
            LOGGER.debug("Executing vertex-disjoint-paths - trial {}", tries++);

            vertexDisjointPaths = new VertexDisjointPathsImpl(graph, pairs.getStartVertices(), pairs.getEndVertices());
            success = vertexDisjointPaths.findDisjointWalks();
        }

        vertexDisjointPaths.printDisjointWalks();

        try {
            vertexDisjointPaths.verifyResult();
        } catch (InvalidAlgorithmResultException e) {
            LOGGER.error("Invalid result");
        }
    }
}
