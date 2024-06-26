package executor.impl;

import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import enums.ExecutionMode;
import exceptions.GraphReadingException;
import exceptions.InvalidAlgorithmResultException;
import exceptions.MaximumNumberOfPairsExceededException;
import exceptions.ExecutionInterruptedException;
import executor.Executor;
import graphloader.GraphLoader;
import graphloader.impl.SimpleUndirectedGraphLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import pairloader.PairLoader;
import pairloader.impl.PairLoaderImpl;

import java.util.Optional;

public class VertexDisjointPathsExecutor implements Executor {

    private static final Logger LOGGER = LogManager.getLogger(VertexDisjointPathsExecutor.class);
    private final String graphInputPath;
    private final int numberEndpointPairs;
    private final int iterations;

    private final ExecutionMode mode;


    /**
     * Initializes a vertex-disjoint-path executor by a given graph file
     * and k random endpoint vertices.
     *
     * @param graphInputPath path to the graph file
     * @param numberEndpointPairs number of random endpoints
     */
    public VertexDisjointPathsExecutor(String graphInputPath, int numberEndpointPairs, ExecutionMode mode, int iterations) {

        this.mode = mode;
        this.iterations = iterations;
        this.graphInputPath = graphInputPath;
        this.numberEndpointPairs = numberEndpointPairs;
    }


    @Override
    public void executeProcedure() throws ExecutionInterruptedException {

        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleUndirectedGraphLoader(graphInputPath);

        Graph<Integer, DefaultWeightedEdge> graph;

        try {
            graph = weightedGraphLoader.loadGraph();
        } catch (GraphReadingException e) {
            throw new ExecutionInterruptedException(e.getMessage());
        }

        double alpha = 1.0 / 10.0;

        PairLoader<Integer> pairLoader = new PairLoaderImpl(graph, alpha, numberEndpointPairs);

        try {
            pairLoader.generatePairs();
        } catch (MaximumNumberOfPairsExceededException e) {
            throw new ExecutionInterruptedException(e.getMessage());
        }

        pairLoader.printPairs();

        long avgDuration = 0;

        if (mode == ExecutionMode.BENCHMARK_MODE) {
            LOGGER.info("Executing 5 warmup iterations");
            warmup(graph, pairLoader);
        }

        for (int i = 0; i < iterations; i++) {

            if (mode == ExecutionMode.BENCHMARK_MODE)
                LOGGER.info("Benchmark iteration {}", i + 1);


            long startTime = System.currentTimeMillis();

            VertexDisjointPaths vdp = executeVDP(graph, pairLoader)
                    .orElseThrow();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;


            avgDuration += duration;

            if (mode == ExecutionMode.BENCHMARK_MODE)
                LOGGER.info("Algorithm duration: {} ms", duration);


            vdp.printDisjointPaths();

            try {
                vdp.verifyResult();
            } catch (InvalidAlgorithmResultException e) {

                LOGGER.error(e.getMessage());
                throw new ExecutionInterruptedException(e.getMessage());
            }
        }


        if (mode == ExecutionMode.BENCHMARK_MODE) {

            float executionDuration = (float) avgDuration / iterations;
            LOGGER.info("Algorithm executed {} time{}", iterations, (iterations > 1 ? "s" : ""));
            LOGGER.info("Average algorithm duration: {} ms", executionDuration);
        }

    }


    private void warmup(Graph<Integer, DefaultWeightedEdge> graph, PairLoader<Integer> pairLoader) {

        for (int i = 0; i < 5; i++) {
            executeVDP(graph, pairLoader);
        }
    }

    private Optional<VertexDisjointPaths> executeVDP(Graph<Integer, DefaultWeightedEdge> graph, PairLoader<Integer> pairLoader) {

        VertexDisjointPaths vertexDisjointPaths = null;
        boolean success = false;
        int attempts = 1;

        while (!success) {

            LOGGER.debug("Executing vertex-disjoint-paths - attempt {}", attempts++);
            vertexDisjointPaths = new VertexDisjointPathsImpl(graph, pairLoader.getPairs());
            success = vertexDisjointPaths.findDisjointPaths();

        }

        return Optional.of(vertexDisjointPaths);
    }
}
