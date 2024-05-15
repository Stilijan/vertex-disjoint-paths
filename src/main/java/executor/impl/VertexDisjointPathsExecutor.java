package executor.impl;

import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import exceptions.GraphReadingException;
import exceptions.InvalidAlgorithmResultException;
import executor.Executor;
import graphloader.GraphLoader;
import graphloader.impl.SimpleUnirectedGraphLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import pairloader.PairLoader;
import pairloader.impl.PairLoaderImpl;

public class VertexDisjointPathsExecutor implements Executor {

    private static final Logger LOGGER = LogManager.getLogger(VertexDisjointPathsExecutor.class);
    private int tries;
    private final String graphInputPath;
    private final int numberEndpointPairs;


    /**
     * Initializes a vertex-disjoint-path executor by a given graph file
     * and k random endpoint vertices.
     *
     * @param graphInputPath path to the graph file
     * @param numberEndpointPairs number of random endpoints
     */
    public VertexDisjointPathsExecutor(String graphInputPath, int numberEndpointPairs) {

        this.tries = 1;
        this.graphInputPath = graphInputPath;
        this.numberEndpointPairs = numberEndpointPairs;
    }


    @Override
    public void executeAlgorithm() {

        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleUnirectedGraphLoader(graphInputPath);

        Graph<Integer, DefaultWeightedEdge> graph;

        try {
            graph = weightedGraphLoader.loadGraph();
        } catch (GraphReadingException e) {

            LOGGER.error("Error while loading the graph");
            return;
        }

        double alpha = 1.0 / 4.0;

        PairLoader<Integer> pairLoader = new PairLoaderImpl(graph, alpha, numberEndpointPairs);
        pairLoader.generatePairs();
        pairLoader.printPairs();


        VertexDisjointPaths vertexDisjointPaths = null;
        boolean success = false;

        while (!success) {
            LOGGER.debug("Executing vertex-disjoint-paths - trial {}", tries++);

            vertexDisjointPaths = new VertexDisjointPathsImpl(graph, pairLoader.getPairs());
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
