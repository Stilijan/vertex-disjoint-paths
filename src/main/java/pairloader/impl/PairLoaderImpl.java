package pairloader.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import pairloader.PairLoader;
import util.VertexPairs;

import java.util.*;

public class PairLoaderImpl implements PairLoader<Integer> {

    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger(PairLoaderImpl.class);

    private final Graph<Integer, DefaultWeightedEdge> graph;
    private final double maxNumberPairs;
    private final VertexPairs<Integer> vertexPairs;
    private boolean generated;
    private int numberPairs;

    public PairLoaderImpl(Graph<Integer, DefaultWeightedEdge> graph, double alpha, int numberPairs) {

        this.graph = graph;
        this.generated = false;

        this.numberPairs = numberPairs;

        int n = graph.vertexSet().size();
        int m = graph.edgeSet().size();
        double d = (double) (2 * m) / n;

        this.maxNumberPairs = alpha * n * Math.log(d) / Math.log(n);
        this.vertexPairs = new VertexPairs<>();
    }

    @Override
    public void generatePairs() {

        LOGGER.debug("Generating {} pairs of start and end vertices", numberPairs);

        if (generated) {

            LOGGER.error("Pairs already generated.");
            return;
        }

        if (numberPairs >= maxNumberPairs) {

            LOGGER.error("number of pairs must not exceed {}", maxNumberPairs);
            return;
        }

        int n = graph.vertexSet().size();
        int endpointsCapacity = 2 * numberPairs;

        Set<Integer> chosenEndpointsSet = HashSet.newHashSet(endpointsCapacity);

        while (vertexPairs.getSize() != numberPairs) {

            int randomStartVertex = RANDOM.nextInt(n) + 1;
            int randomEndVertex = RANDOM.nextInt(n) + 1;

            boolean disjointVertices = !(randomStartVertex == randomEndVertex ||
                chosenEndpointsSet.contains(randomStartVertex) ||
                chosenEndpointsSet.contains(randomEndVertex));

            if (!disjointVertices) {

                continue;
            }

            vertexPairs.addOneVertexPair(randomStartVertex, randomEndVertex);

            chosenEndpointsSet.add(randomStartVertex);
            chosenEndpointsSet.add(randomEndVertex);
        }


        LOGGER.info("Pairs generated");
        this.generated = true;
    }



    @Override
    public void printPairs() {

        LOGGER.trace("Printing pairs of start and end vertices");

        if (!generated) {

            LOGGER.error("Pairs not generated yet.");
            return;
        }


        for (int i = 0; i < vertexPairs.getSize(); i++) {

            LOGGER.info("Pair {}: ({}, {})", i + 1, vertexPairs.getStartVertices().get(i), vertexPairs.getEndVertices().get(i));
        }
    }

    @Override
    public VertexPairs<Integer> getPairs() {
        return vertexPairs;
    }

}
