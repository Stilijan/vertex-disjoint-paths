package algorithm.impl;

import algorithm.VertexDisjointPaths;
import exceptions.AlgorithmInterruptedException;
import exceptions.InvalidAlgorithmResultException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import util.VertexPairs;

import walks.Walk;
import walks.ConnectedWalk;
import walks.NetworkFlowWalk;
import walks.RandomWalk;
import walks.ShortestPathWalk;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.stream.Collectors;


public class VertexDisjointPathsImpl implements VertexDisjointPaths {


    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getLogger(VertexDisjointPathsImpl.class);


    // ================== STEP 3 VARIABLES ==========================
    private final List<Integer> aTildeVertices = new ArrayList<>();
    private final List<Integer> bTildeVertices = new ArrayList<>();
    private static final int NETWORK_FLOW_SOURCE = Integer.MIN_VALUE;
    private static final int NETWORK_FLOW_SINK = Integer.MAX_VALUE;


    // ================== STEP 4 VARIABLES ==========================
    private final List<Integer> aHatVertices = new ArrayList<>();
    private final List<Integer> bHatVertices = new ArrayList<>();


    private final Graph<Integer, DefaultWeightedEdge> mainGraph;
    private final VertexPairs<Integer> pairs;
    private final int numberPairs;
    private final int lengthRandomWalk;
    private List<Walk<Integer>> result;



    public VertexDisjointPathsImpl(Graph<Integer, DefaultWeightedEdge> mainGraph,
                                   VertexPairs<Integer> vertexPairs) {
        
        this.mainGraph = mainGraph;
        this.pairs = vertexPairs;

        int numberEdges = mainGraph.edgeSet().size();
        int numberVertices = mainGraph.vertexSet().size();
        this.numberPairs = pairs.getSize();


        double d = ((double) (2 * numberEdges)) / numberVertices;
        this.lengthRandomWalk =
            (int) Math.ceil(4.0 * Math.log(numberVertices) / Math.log(d));
    }


    @Override
    public boolean findDisjointWalks() {

        LOGGER.debug("Finding vertex disjoint paths in a graph with {} vertices and {} edges",
            mainGraph.vertexSet().size(),
            mainGraph.edgeSet().size());

        //================| STEP 1 |===================

        Set<Integer> x1Vertices = partitionVerticesIntoX1(mainGraph.vertexSet());

        Set<Integer> xVertices = new HashSet<>();
        xVertices.addAll(x1Vertices);
        xVertices.addAll(pairs.getAllVertices());

        //================| STEP 2 |===================

        Set<Integer> kVertices;
        try {
            kVertices = putRandomVerticesInK(x1Vertices);
        } catch (AlgorithmInterruptedException e) {
            return false;
        }

        //================| STEP 3 |===================

        Graph<Integer, DefaultWeightedEdge> splitGraphX =
            createCloneGraphAndSplitVertices(new AsSubgraph<>(mainGraph, xVertices));

        List<Integer> kVerticesList = new ArrayList<>(kVertices);

        assert kVerticesList.size() % 2 == 0;

        List<Walk<Integer>> walks1;
        List<Walk<Integer>> walks5;

        try {
            NetworkFlowWalk.setFlowMap(createFlowMap(splitGraphX, kVerticesList));
        } catch (AlgorithmInterruptedException e) {
            return false;
        }

        walks1 = extractWalks1(splitGraphX);
        walks5 = extractWalks5(splitGraphX);

        assert walks1.size() == numberPairs;
        assert walks5.size() == numberPairs;


        //================| STEP 4 |===================

        // Put all vertices, which are not in X, in Y
        Set<Integer> yVertices = mainGraph.vertexSet().stream()
            .filter(v -> !xVertices.contains(v))
            .collect(Collectors.toSet());

        // Partition randomly Y in Z1 and Z2
        Map<Boolean, List<Integer>> zVerticesMap = yVertices.stream()
            .collect(Collectors.partitioningBy(v -> RANDOM.nextDouble() <= 1.0 / 2.0));

        Set<Integer> z1Vertices = new HashSet<>(zVerticesMap.get(true));
        Set<Integer> z2Vertices = new HashSet<>(zVerticesMap.get(false));


        // Generate random walks
        List<Walk<Integer>> walks2;
        List<Walk<Integer>> walks4;
        Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, z1Vertices);

        try {
            walks2 = extractWalks2(graphJ);
            walks4 = extractWalks4(graphJ);
        } catch (AlgorithmInterruptedException e) {
            return false;
        }


        assert walks2.size() == numberPairs;
        assert walks4.size() == numberPairs;

        //================| STEP 5 |===================

        Graph<Integer, DefaultWeightedEdge> graphHat = new AsSubgraph<>(mainGraph, z2Vertices);
        List<Walk<Integer>> walks3;

        try {
            walks3 = extractWalks3(graphHat);
        } catch (AlgorithmInterruptedException e) {
            return false;
        }

        assert walks3.size() == numberPairs;
        
        //================| STEP 6 |===================

        List<Walk<Integer>> connectedWalks = new ArrayList<>(numberPairs);


        for (int i = 0; i < numberPairs; i++) {

            List<Walk<Integer>> walkParts = new LinkedList<>();

            walkParts.add(walks1.get(i).withRemovedCycles());
            walkParts.add(walks2.get(i).withRemovedCycles());
            walkParts.add(walks3.get(i).withRemovedCycles());
            walkParts.add(walks4.get(i).withRemovedCycles());
            walkParts.add(walks5.get(i).withRemovedCycles());

            Walk<Integer> connectedWalk = new ConnectedWalk<>(walkParts);

            try {
                connectedWalk.generateWalk();
            } catch (AlgorithmInterruptedException e) {
                return false;
            }

            connectedWalks.add(connectedWalk);
        }

        this.result = connectedWalks;
        return true;
    }



    @Override
    public void verifyResult() throws InvalidAlgorithmResultException {

        LOGGER.trace("Verifying result.");

        Map<Integer, Integer> disjointVerticesMap = new HashMap<>();


        for (int i = 0; i < result.size(); i++) {

            List<Integer> walk = result.get(i).getPath();

            for (int j = 0; j < walk.size(); j++) {

                int currentVertex = walk.get(j);

                // Check if the current and the previous vertex are neighbours
                if (j > 0 && mainGraph.getEdge(walk.get(j - 1), currentVertex) == null) {

                    int prevVertex = walk.get(j - 1);
                    String message =
                        "Edge %d - %d in walk %d doesn't exist".formatted(prevVertex, currentVertex, i + 1);

                    throw new InvalidAlgorithmResultException(message);
                }

                // Check if all vertices are unique
                if (disjointVerticesMap.containsKey(currentVertex)) {

                    String message =
                        "Vertex %d in walk %d is contained also in walk %d."
                            .formatted(currentVertex, i + 1, disjointVerticesMap.get(currentVertex) + 1);

                    throw new InvalidAlgorithmResultException(message);
                }

                disjointVerticesMap.put(currentVertex, i);
            }
        }

        LOGGER.info("Result verified!");
    }

    @Override
    public void printDisjointPaths() {

        LOGGER.trace("Printing paths.");

        for (int i = 0; i < result.size(); i++) {

            LOGGER.info("Path {}: {} ", (i + 1), result.get(i));
        }
    }

    /**
     * Chooses uniformly vertices from {@code vertexSet} and puts them in a {@code K} vertex set.
     *
     * @param x1VertexSet vertex set to be chosen from.
     * @return a set of vertices, namely K.
     * @throws AlgorithmInterruptedException if the size of K is  bigger than X1.
     */
    private Set<Integer> putRandomVerticesInK(Set<Integer> x1VertexSet) throws AlgorithmInterruptedException {

        LOGGER.trace("Partitioning X1 into K");

        final int kVertexSetCapacity = 2 * numberPairs;

        if (x1VertexSet.size() < kVertexSetCapacity) {

            String message = "K is bigger than X1";
            LOGGER.error(message);
            throw new AlgorithmInterruptedException(message);
        }

        Set<Integer> kVertexSet = HashSet.newHashSet(kVertexSetCapacity);

        while (kVertexSet.size() != kVertexSetCapacity) {

            // choose a random vertex from X1 and add it to K
            int randomIndex = RANDOM.nextInt(x1VertexSet.size());
            int randomVertex = new ArrayList<>(x1VertexSet).get(randomIndex);

            x1VertexSet.remove(randomVertex);
            kVertexSet.add(randomVertex);
        }

        return kVertexSet;
    }

    /**
     * Chooses randomly vertices from {@code vertexSet} with the specified {@code probability}.
     *
     * @param vertexSet vertex set to be chosen from.
     * @return set of vertices, namely X1, which is approximately 1/{@code probability} of the size of {@code vertexSet}.
     */
    private Set<Integer> partitionVerticesIntoX1(Set<Integer> vertexSet) {

        LOGGER.trace("Partitioning the main vertex set into X1");

        return vertexSet.stream()
            .filter(v -> RANDOM.nextDouble() <= 1.0 / 3.0)
            .collect(Collectors.toSet());
    }


    /**
     * Generates the shortest path between a<sub>i</sub><sup>*</sup> and b<sub>i</sub><sup>*</sup>, which are
     * random neighbours of a<sub>i</sub><sup>^</sup> and b<sub>i</sub><sup>^</sup>, respectively.
     *
     * @param aiHat the end vertex of W<sub>i</sub><sup>(2)</sup>.
     * @param biHat the end vertex of W<sub>i</sub><sup>(4)</sup>.
     * @param graphYiHat the graph, where the shortest path is generated.
     * @return the shortest path walk.
     */
    private Walk<Integer> generateShortestPathWalk(int aiHat,
                                                   int biHat,
                                                   Graph<Integer, DefaultWeightedEdge> graphYiHat)
        throws AlgorithmInterruptedException{

        LOGGER.debug("Generating the shortest path from a neighbour of {} to a neighbour of {}", aiHat, biHat);

        List<Integer> neighboursOfAiHatInZ2 = getNeighboursOfVertexInGraph(aiHat, graphYiHat.vertexSet());
        int aiStar = neighboursOfAiHatInZ2.get(RANDOM.nextInt(neighboursOfAiHatInZ2.size()));

        List<Integer> neighboursOfBiHatInZ2 = getNeighboursOfVertexInGraph(biHat, graphYiHat.vertexSet());
        int biStar = neighboursOfBiHatInZ2.get(RANDOM.nextInt(neighboursOfBiHatInZ2.size()));

        Walk<Integer> walk = new ShortestPathWalk<>(graphYiHat, aiStar, biStar);
        walk.generateWalk();

        return walk;
    }


    /**
     * Generates a random walk in {@code graphJ}. The start vertex of this walk
     * is a random {@code Z1}-neighbour of {@code wj}, which is a vertex
     * from {@code aTildeVertices} or {@code bTildeVertices}.
     *
     * @param graphJ graph, where the random walk is generated.
     * @param wj vertex from {@code aTildeVertices} or {@code bTildeVertices}.
     * @return the generated walk.
     */
    private Walk<Integer> generateRandomWalk(
        Graph<Integer, DefaultWeightedEdge> graphJ,
        int wj
    ) throws AlgorithmInterruptedException{

        LOGGER.debug("Generating a random walk with a neighbour of {} as a start vertex", wj);

        // get all neighbours of wj, which are in graphJ
        List<Integer> neighboursInZ1 = this.getNeighboursOfVertexInGraph(wj, graphJ.vertexSet());

        // get a random neighbour of wj as a start vertex
        int startVertex =
            neighboursInZ1.get(RANDOM.nextInt(neighboursInZ1.size()));

        // generate the walk
        Walk<Integer> randomWalk = new RandomWalk<>(graphJ, startVertex, lengthRandomWalk);
        randomWalk.generateWalk();



        return randomWalk;
    }


    /**
     * Extracts network flow walks from the split graph X.
     *
     * @param splitGraphX the graph, where the walks are generated.
     * @return a list of walks
     */
    private List<Walk<Integer>> extractWalks1(Graph<Integer, DefaultWeightedEdge> splitGraphX) {

        LOGGER.trace("Generating W(1)");

        List<Walk<Integer>> walks1 = new LinkedList<>();

        for (int startVertex : this.pairs.getStartVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                splitGraphX,
                startVertex,
                NETWORK_FLOW_SOURCE,
                NETWORK_FLOW_SINK
            );

            walk.generateWalk();

            LOGGER.debug("Generated network flow walk between {} and {}",
                walk.getStartVertex(), walk.getEndVertex());

            walks1.add(walk);

            aTildeVertices.add(walk.getEndVertex());
        }

        return walks1;
    }


    /**
     * Extracts reversed random walks from graph J.
     *
     * @param graphJ the graph, where the walks are generated.
     * @return a list of walks.
     * @throws AlgorithmInterruptedException if a random walk cannot achieve the necessary length.
     */
    private List<Walk<Integer>> extractWalks2(Graph<Integer, DefaultWeightedEdge> graphJ)
        throws AlgorithmInterruptedException {

        LOGGER.trace("Generating W(2)");
        List<Walk<Integer>> walks2 = new LinkedList<>();

        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks1.get(i)
            int aiTilde = aTildeVertices.get(i);


            // generate a normal random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, aiTilde);

            walks2.add(randomWalk);
            aHatVertices.add(randomWalk.getEndVertex());

            // we remove the generated random walk from the graph,
            // in order to get disjoint paths
            randomWalk.getPath().forEach(graphJ::removeVertex);
        }

        return walks2;
    }


    /**
     * Extracts W(3) by creating the shortest paths between each pair
     * (a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup>).
     * a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup> are random neighbours in the graph with {@code z2Vertices}
     * of a<sub>i</sub><sup>~</sup> and b<sub>i</sub><sup>~</sup>, respectively.
     *
     * @param graphHat the graph, where the shortest path is generated.
     * @return a list of shortest path walks.
     */
    private List<Walk<Integer>> extractWalks3(Graph<Integer, DefaultWeightedEdge> graphHat) throws AlgorithmInterruptedException{

        LOGGER.trace("Generating W(3)");

        List<Walk<Integer>> walks3 = new LinkedList<>();

        for (int i = 0; i < numberPairs; i++) {

            int aiHat = aHatVertices.get(i);
            int biHat = bHatVertices.get(i);

            Walk<Integer> shortestPathWalk = generateShortestPathWalk(aiHat, biHat, graphHat);
            walks3.add(shortestPathWalk);

            shortestPathWalk.getPath().forEach(graphHat::removeVertex);
        }

        return walks3;
    }


    /**
     * Extracts reversed random walks from graph J.
     *
     * @param graphJ the graph, where the walks are generated.
     * @return a list of walks.
     * @throws AlgorithmInterruptedException if a random walk cannot achieve the necessary length.
     */
    private List<Walk<Integer>> extractWalks4(Graph<Integer, DefaultWeightedEdge> graphJ)
            throws AlgorithmInterruptedException {
        LOGGER.trace("Generating W(4)");

        List<Walk<Integer>> walks4 = new LinkedList<>();

        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks1.get(i)
            int biTilde = bTildeVertices.get(i);

            // generate a normal random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, biTilde);

            walks4.add(randomWalk.reversed());
            bHatVertices.add(randomWalk.getStartVertex());

            // we remove the generated random walk from the graph,
            // in order to get disjoint paths
            randomWalk.getPath().forEach(graphJ::removeVertex);
        }

        return walks4;
    }

    /**
     * Extracts reversed network flow walks from the split graph X.
     *
     * @param splitGraphX the graph, where the walks are generated.
     * @return a list of walks
     */
    private List<Walk<Integer>> extractWalks5(Graph<Integer, DefaultWeightedEdge> splitGraphX) {

        LOGGER.trace("Generating W(5)");

        List<Walk<Integer>> walks5 = new LinkedList<>();

        for (int endVertex : this.pairs.getEndVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                splitGraphX,
                endVertex,
                NETWORK_FLOW_SOURCE,
                NETWORK_FLOW_SINK
            );

            walk.generateWalk();
            walks5.add(walk.reversed());

            bTildeVertices.add(walk.getStartVertex());
        }

        return walks5;
    }


    /**
     * Performs the Edmonds-Karp Maximum Flow problem searching for
     * vertex-disjoint paths between a source and a sink.
     *
     * @param splitGraphX a graph with split vertices, where the network flow algorithm is performed.
     * @param kVertices the end vertices of the paths
     * @return a flow map of edge - double pairs, denoting the flow value of each edge in the graph.
     * @throws AlgorithmInterruptedException if the maximum flow doesn't equal 2 * k.
     */
    private Map<DefaultWeightedEdge, Double> createFlowMap(Graph<Integer, DefaultWeightedEdge> splitGraphX,
                                                           List<Integer> kVertices)
            throws AlgorithmInterruptedException {

        LOGGER.trace("Creating a flow map");

        // create temporarily a source/sink vertex and
        // connect it with all start/end vertices, respectively
        addSinkAndSource(splitGraphX, kVertices);

        // calculate the maximum flow of the graph with the
        // Edmonds-Karp Maximum Flow Algorithm
        EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> ek = new EdmondsKarpMFImpl<>(splitGraphX);

        LOGGER.trace("Performing Edmonds-Karp algorithm");
        double maxFlow = ek.calculateMaximumFlow(NETWORK_FLOW_SOURCE, NETWORK_FLOW_SINK);

        if (maxFlow != 2 * numberPairs) {

            String message = "Maximum flow is not %d.".formatted(2 * numberPairs);
            LOGGER.error(message);
            throw new AlgorithmInterruptedException(message);
        }

        return ek.getFlowMap();
    }

    /**
     * Creates a new directed weighted graph clone with all vertices {@code V} from {@code graph} being split into
     * {@code V}<sub>in</sub> and {@code V}<sub>out</sub>. {@code V}<sub>in</sub> accepts all incoming edges of {@code V}
     * and {@code V}<sub>out</sub> accepts all outgoing edges of {@code V}.
     *
     * @param graph the graph to be cloned.
     * @return the graph clone with the split vertices.
     */
    private Graph<Integer, DefaultWeightedEdge> createCloneGraphAndSplitVertices(Graph<Integer,
            DefaultWeightedEdge> graph) {

        LOGGER.trace("Creating a clone of graph X1 and splitting all vertices");

        Graph<Integer, DefaultWeightedEdge> clonedGraph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

        for (int vertex : graph.vertexSet()) {

            // add vertex_in in the cloned graph
            clonedGraph.addVertex(vertex);

            // add vertex_out in the cloned graph
            clonedGraph.addVertex(-vertex);

            // connect vertex_in and vertex_out
            clonedGraph.addEdge(vertex, -vertex);
            clonedGraph.setEdgeWeight(vertex, -vertex, 1.0);
        }

        for (int vertex : graph.vertexSet()) {

            if (vertex < 0) {
                continue;
            }

            // get all incoming/outgoing edges of vertex
            Set<DefaultWeightedEdge> edgesOfVertex = graph.edgesOf(vertex);

            for (DefaultWeightedEdge edge : edgesOfVertex) {

                // get source and target of edge
                int edgeSource = graph.getEdgeSource(edge);
                int edgeTarget = graph.getEdgeTarget(edge);

                // get the neighbour of vertex
                int neighbourOfVertex = edgeSource == vertex ? edgeTarget : edgeSource;

                // add incoming edge to vertex_in
                clonedGraph.addEdge(-neighbourOfVertex, vertex);
                clonedGraph.setEdgeWeight(-neighbourOfVertex, vertex, 1.0);

                // add outgoing edge from vertex_out
                clonedGraph.addEdge(-vertex, neighbourOfVertex);
                clonedGraph.setEdgeWeight(-vertex, neighbourOfVertex, 1.0);

            }
        }

        return clonedGraph;
    }


    /**
     * Adds network flow source and sink vertices to a graph.
     * The source vertex is connected with all {@code V}<sub>in</sub> of the start and end vertices.
     * The sink vertex is connected with all {@code V}<sub>out</sub> of the k vertices.
     *
     * @param graph a graph, which the source and sink addition is applied on.
     * @param kVertices the vertices, which have to be connected with the sink.
     */
    private void addSinkAndSource(Graph<Integer, DefaultWeightedEdge> graph, List<Integer> kVertices) {

        LOGGER.trace("Adding source and sink vertices into the flow network");

        graph.addVertex(NETWORK_FLOW_SOURCE);
        graph.addVertex(NETWORK_FLOW_SINK);

        for (int i = 0; i < numberPairs; i++) {

            graph.addEdge(NETWORK_FLOW_SOURCE, pairs.getStartVertices().get(i));
            graph.addEdge(-(kVertices.get(i)), NETWORK_FLOW_SINK);
        }

        for (int i = 0; i < numberPairs; i++) {

            graph.addEdge(NETWORK_FLOW_SOURCE, pairs.getEndVertices().get(i));
            graph.addEdge(-(kVertices.get(numberPairs + i)), NETWORK_FLOW_SINK);
        }

    }


    /**
     * Returns all neighbours of vertex {@code v}, which are also in the {@code filterVertices}
     * vertex set.
     *
     * @param v a vertex
     * @param filterVertices vertex set, used for filtering out the neighbours of {@code v}
     * @return a list of vertices, which are neighbours of {@code v} and are in {@code filterVertices}
     * @throws AlgorithmInterruptedException if {@code v} has no neighbours, that are also in {@code filterVertices}
     */
    private List<Integer> getNeighboursOfVertexInGraph(Integer v, Set<Integer> filterVertices) throws AlgorithmInterruptedException {

        List<Integer> allNeighbours = Graphs.neighborListOf(mainGraph, v);
        List<Integer> neighboursInFilterVertices =
            allNeighbours
                .stream()
                .filter(filterVertices::contains)
                .toList();

        if (neighboursInFilterVertices.isEmpty()) {

            LOGGER.error("{} has no neighbours in Z1", v);
            throw new AlgorithmInterruptedException(v + " has no neighbours in Z1");
        }

        return new ArrayList<>(neighboursInFilterVertices);
    }
}
