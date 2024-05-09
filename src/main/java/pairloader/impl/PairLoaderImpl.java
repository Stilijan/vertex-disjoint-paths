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

    private List<Integer> startVertices;
    private List<Integer> endVertices;
    private boolean generated;
    private final double beta;

    public PairLoaderImpl(Graph<Integer, DefaultWeightedEdge> graph, double alpha, double beta) {

        this.graph = graph;
        this.generated = false;
        this.beta = beta;

        int n = graph.vertexSet().size();
        int m = graph.edgeSet().size();
        double d = (double) (2 * m) / n;

        this.maxNumberPairs = alpha * n * Math.log(d) / Math.log(n);
    }

    @Override
    public void generatePairs(int numberOfPairs) {

        LOGGER.debug("Generating {} pairs of start and end vertices", numberOfPairs);

        if (generated) {

            LOGGER.error("Pairs already generated.");
            return;
        }

        if (numberOfPairs >= maxNumberPairs) {

            LOGGER.error("number of pairs must not exceed {}", maxNumberPairs);
            return;
        }

        int n = graph.vertexSet().size();

        Set<Integer> startVertexSet = new LinkedHashSet<>();
        Set<Integer> endVertexSet = new LinkedHashSet<>();

        while (startVertexSet.size() != numberOfPairs) {

            int randomStartVertex = RANDOM.nextInt(n) + 1;
            int randomEndVertex = RANDOM.nextInt(n) + 1;

            boolean disjointVertices = !(randomStartVertex == randomEndVertex ||
                endVertexSet.contains(randomStartVertex) ||
                startVertexSet.contains(randomEndVertex) ||
                startVertexSet.contains(randomStartVertex) ||
                endVertexSet.contains(randomEndVertex));

            if (!disjointVertices) {

                continue;
            }

            startVertexSet.add(randomStartVertex);
            endVertexSet.add(randomEndVertex);
        }

        this.startVertices = new ArrayList<>(startVertexSet);
        this.endVertices = new ArrayList<>(endVertexSet);

        this.generated = true;
        LOGGER.info("Pairs generated");
    }

    @Override
    public List<Integer> getStartVertices() {
        return generated ? startVertices : null;
    }

    @Override
    public List<Integer> getEndVertices() {
        return generated ? endVertices : null;
    }

    @Override
    public void printPairs() {

        LOGGER.trace("Printing pairs of start and end vertices");

        if (!generated) {

            LOGGER.error("Pairs already generated.");
            return;
        }

        for (int i = 0; i < startVertices.size(); i++) {

            LOGGER.info("Pair {}: ({}, {})", i + 1, startVertices.get(i), endVertices.get(i));
        }
    }

    @Override
    public VertexPairs<Integer> getPairs() {
        return new VertexPairs<>(startVertices, endVertices);
    }
}
