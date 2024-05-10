package algorithm.impl;

import algorithm.VertexDisjointPaths;
import exceptions.AlgorithmInterruptedException;
import exceptions.InvalidAlgorithmResultException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.graph.*;
import util.VertexPairs;
import walks.*;

import java.util.*;
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
    public boolean findDisjointWalks()  {

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


        Graph<Integer, DefaultWeightedEdge> graphX = new AsSubgraph<>(mainGraph, xVertices);

        // convert K from set to list
        List<Integer> kVerticesList = new ArrayList<>(kVertices);

        assert kVerticesList.size() % 2 == 0;

        List<List<Walk<Integer>>> walks1And5;

        try {

            walks1And5 = extractWalks1AndWalks5(graphX, kVerticesList);

        } catch (AlgorithmInterruptedException e) {

            return false;
        }

        kVerticesList.clear();

        List<Walk<Integer>> walks1 = walks1And5.get(0);
        List<Walk<Integer>> walks5 = walks1And5.get(1);

        
        assert walks1.size() == numberPairs;
        assert walks5.size() == numberPairs;


        //================| STEP 4 |===================


        // partition Y in Z1 and Z2
        Set<Integer> yVertices = mainGraph.vertexSet().stream()
            .filter(v -> !xVertices.contains(v))
            .collect(Collectors.toSet());

        Map<Boolean, List<Integer>> zVerticesMap = yVertices.stream()
            .collect(Collectors.partitioningBy(v -> RANDOM.nextDouble() <= 1.0 / 2.0));

        Set<Integer> z1Vertices = new HashSet<>(zVerticesMap.get(true));
        Set<Integer> z2Vertices = new HashSet<>(zVerticesMap.get(false));


        yVertices.clear();


        // Create random walks
        List<List<Walk<Integer>>> walks2And4;

        try {
            walks2And4 = extractWalks2andWalks4(z1Vertices);
        } catch (AlgorithmInterruptedException e) {
            return false;
        }

        List<Walk<Integer>> walks2 = walks2And4.get(0);
        List<Walk<Integer>> walks4 = walks2And4.get(1);

        assert walks2.size() == numberPairs;
        assert walks4.size() == numberPairs;

        //================| STEP 5 |===================

        List<Walk<Integer>> walks3;

        try {
            walks3 = extractWalks3(z2Vertices);
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
                if (j > 0 && mainGraph.getEdge(walk.get(j - 1), currentVertex) == null &&
                    mainGraph.getEdge(currentVertex, walk.get(j - 1)) == null) {

                    String message =
                        "Edge %d - %d in walk %d doesn't exist".formatted(walk.get(j - 1), currentVertex, i + 1);

                    LOGGER.error(message);
                    throw new InvalidAlgorithmResultException(message);
                }

                // Check if all vertices are unique
                if (!disjointVerticesMap.containsKey(currentVertex)) {

                    disjointVerticesMap.put(currentVertex, i);
                } else {

                    String message =
                        "Vertex %d in walk %d is contained also in walk %d."
                            .formatted(walk.get(j), i + 1, disjointVerticesMap.get(currentVertex) + 1);

                    LOGGER.error(message);
                    throw new InvalidAlgorithmResultException(message);
                }
            }
        }

        LOGGER.info("Result verified!");
    }

    @Override
    public void printDisjointWalks() {

        LOGGER.trace("Printing result.");

        for (int i = 0; i < result.size(); i++) {

            LOGGER.info("Walk {}: {} ", (i + 1), result.get(i));
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
     * Returns W<sub>i</sub><sup>(3)</sup> by creating the shortest paths between each pair
     * (a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup>).
     * a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup> are random neighbours in the graph with {@code z2Vertices}
     * of a<sub>i</sub><sup>~</sup> and b<sub>i</sub><sup>~</sup>, respectively.
     *
     * @param z2Vertices the vertices in the graph, where the shortest path walk is generated.
     * @return a list of shortest path walks.
     */
    private List<Walk<Integer>> extractWalks3(Set<Integer> z2Vertices) throws AlgorithmInterruptedException{

        LOGGER.trace("Generating W(3)");

        List<Walk<Integer>> walks3 = new LinkedList<>();

        Graph<Integer, DefaultWeightedEdge> graphYiHat = new AsSubgraph<>(mainGraph, z2Vertices);

        for (int i = 0; i < numberPairs; i++) {

            int aiHat = aHatVertices.get(i);
            int biHat = bHatVertices.get(i);

            Walk<Integer> shortestPathWalk = generateShortestPathWalk(aiHat, biHat, graphYiHat);
            walks3.add(shortestPathWalk);

            shortestPathWalk.getPath().forEach(graphYiHat::removeVertex);
        }

        return walks3;
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
    private Walk<Integer> generateShortestPathWalk(int aiHat, int biHat, Graph<Integer, DefaultWeightedEdge> graphYiHat)
        throws AlgorithmInterruptedException{

        LOGGER.debug("Generating the shortest path from a neighbour of {} to a neighbour of {}", aiHat, biHat);


        List<Integer> neighboursOfAiHatInZ2 = getNeighboursOfVertexInGraph(aiHat, graphYiHat.vertexSet());
        int aiStar = neighboursOfAiHatInZ2.get(RANDOM.nextInt(neighboursOfAiHatInZ2.size()));


        List<Integer> neighboursOfBiHatInZ2 = getNeighboursOfVertexInGraph(biHat, graphYiHat.vertexSet());
        int biStar = neighboursOfBiHatInZ2.get(RANDOM.nextInt(neighboursOfBiHatInZ2.size()));

        neighboursOfAiHatInZ2.clear();
        neighboursOfBiHatInZ2.clear();


        Walk<Integer> walk = new ShortestPathWalk<>(graphYiHat, aiStar, biStar);
        walk.generateWalk();


        return walk;
    }


    /**
     * Generates a random walk in {@code graphJ}. The start vertex of this walk
     * is a random {@code Z1}-neighbour of {@code wj}, which is a vertex from {@code aTildeVertices} or {@code bTildeVertices}.
     * In some cases the generated walk must be reversed.
     *
     *
     * @param graphJ graph, where the random walk is generated.
     * @param wj vertex from {@code aTildeVertices} or {@code bTildeVertices}.
     * @param reverse true, if the generated walk must be reversed.
     * @return the generated walk.
     */
    private Walk<Integer> generateRandomWalk(
        Graph<Integer, DefaultWeightedEdge> graphJ,
        int wj,
        boolean reverse
    ) throws AlgorithmInterruptedException{

        LOGGER.debug("Generating a random walk with a neighbour of {} as a start vertex", wj);

        // get all neighbours of wj, which are in graphJ
        List<Integer> neighboursInZ1 = this.getNeighboursOfVertexInGraph(wj, graphJ.vertexSet());

        // get a random neighbour of wj as a start vertex
        int startVertex =
            neighboursInZ1.get(RANDOM.nextInt(neighboursInZ1.size()));

        neighboursInZ1.clear();

        // generate the walk
        Walk<Integer> randomWalk = new RandomWalk<>(graphJ, startVertex, lengthRandomWalk);
        randomWalk.generateWalk();

        if (reverse) {
            randomWalk = randomWalk.reversed();
        }


        return randomWalk;
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
                .distinct()
                .filter(filterVertices::contains)
                .toList();

        if (neighboursInFilterVertices.isEmpty()) {

            LOGGER.error("{} has no neighbours in Z1", v);
            throw new AlgorithmInterruptedException(v + " has no neighbours in Z1");
        }

        return new ArrayList<>(neighboursInFilterVertices);
    }


    /**
     * Returns disjoint walks W<sub>i</sub><sup>(2)</sup> and W<sub>i</sub><sup>(4)</sup>.
     * Random walks are generated between {@code aTildeVertices}/{@code bTildeVertices} and random endpoints
     * in the graph with {@code Z1} vertices.
     *
     * @param z1Vertices the vertices in the graph, where the random walks are generated.
     * @return list of 2 elements - W<sub>i</sub><sup>(2)</sup> and W<sub>i</sub><sup>(4)</sup>, which are lists with walks.
     */
    private List<List<Walk<Integer>>> extractWalks2andWalks4(Set<Integer> z1Vertices) throws AlgorithmInterruptedException{

        LOGGER.trace("Generating W(2) and W(4)");

        List<List<Walk<Integer>>> walks2And4 = new ArrayList<>(2);


        Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, z1Vertices);
        List<Walk<Integer>> walks2 = new LinkedList<>();

        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks1.get(i)
            int aiTilde = aTildeVertices.get(i);


            // generate a normal random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, aiTilde, false);

            aHatVertices.add(randomWalk.getEndVertex());
            walks2.add(randomWalk);

            // we remove the generated random walk from the graph,
            // in order to get disjoint paths
            randomWalk.getPath().forEach(graphJ::removeVertex);
        }

        walks2And4.add(walks2);


        List<Walk<Integer>> walks4 = new LinkedList<>();
        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks5.get(i)
            int biTilde = bTildeVertices.get(i);

            // generate a reversed random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, biTilde, true);

            bHatVertices.add(randomWalk.getStartVertex());
            walks4.add(randomWalk);

            randomWalk.getPath().forEach(graphJ::removeVertex);
        }

        walks2And4.add(walks4);

        return walks2And4;
    }


    /**
     * Returns the disjoint walks W<sub>i</sub><sup>(1)</sup> and W<sub>i</sub><sup>(5)</sup>
     * for {@code i} being from 1 to k. These are evaluated by reducing the problem to the
     * <a href="https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Edmonds-Karp Algorithm</a>
     * for finding the maximum flow in a network graph.
     *
     * @param graphX the graph with vertices from {@code X} vertex set.
     * @param kVertices the end vertices of the walks.
     * @return a list of two lists, each of them containing disjoint walks.
     */
    private List<List<Walk<Integer>>> extractWalks1AndWalks5(Graph<Integer, DefaultWeightedEdge> graphX,
                                                            List<Integer> kVertices) throws AlgorithmInterruptedException {

        LOGGER.trace("Generating W(1) and W(5)");

        List<List<Walk<Integer>>> walks1And5 = new ArrayList<>(2);


        // create a clone of X and split every vertex in vertex_in and vertex_out
        Graph<Integer, DefaultWeightedEdge> graphXClone = createCloneGraphAndSplitVertices(graphX);


        // create temporarily a source/sink vertex and
        // connect it with all start/end vertices, respectively
        addSinkAndSource(graphXClone, kVertices);


        // calculate the maximum flow of the graph with the
        // Edmonds-Karp Maximum Flow Algorithm
        EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> ek = new EdmondsKarpMFImpl<>(graphXClone);
        double maxFlow = ek.calculateMaximumFlow(NETWORK_FLOW_SOURCE, NETWORK_FLOW_SINK);

        if (maxFlow != 2 * numberPairs) {

            LOGGER.error("Maximum flow is insufficient.");
            throw new AlgorithmInterruptedException("");
        }

        // extract all vertex-disjoint paths
        Map<DefaultWeightedEdge, Double> flowMap = ek.getFlowMap();
        NetworkFlowWalk.setFlowMap(flowMap);


        List<Walk<Integer>> walks1 = new LinkedList<>();
        List<Walk<Integer>> walks5 = new LinkedList<>();

        for (int startVertex : this.pairs.getStartVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                graphXClone,
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

        for (int endVertex : this.pairs.getEndVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                graphXClone,
                endVertex,
                NETWORK_FLOW_SOURCE,
                NETWORK_FLOW_SINK
            );

            walk.generateWalk();
            walks5.add(walk.reversed());

            LOGGER.debug("Generated network flow walk between {} and {}",
                walk.getStartVertex(), walk.getEndVertex());

            bTildeVertices.add(walk.getStartVertex());
        }

        walks1And5.add(walks1);
        walks1And5.add(walks5);

        removeSinkAndSource(graphXClone);

        return walks1And5;
    }


    /**
     * Creates a new directed weighted graph clone with all vertices {@code V} from {@code graph} being split into
     * {@code V}<sub>in</sub> and {@code V}<sub>out</sub>. {@code V}<sub>in</sub> accepts all incoming edges of {@code V}
     * and {@code V}<sub>out</sub> accepts all outgoing edges of {@code V}.
     *
     * @param graph the graph to be cloned.
     * @return the graph clone with the split vertices.
     */
    private Graph<Integer, DefaultWeightedEdge> createCloneGraphAndSplitVertices(Graph<Integer, DefaultWeightedEdge> graph) {

        LOGGER.trace("Creating a clone of graph X1 and splitting all vertices");

        Graph<Integer, DefaultWeightedEdge> clonedGraph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

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
     * Removes the temporary source and sink vertices from the network.
     *
     * @param graph a network graph with existing source and sink vertices.
     */
    private void removeSinkAndSource(Graph<Integer, DefaultWeightedEdge> graph) {

        LOGGER.trace("Removing source and sink vertices from the flow network");

        graph.removeVertex(NETWORK_FLOW_SOURCE);
        graph.removeVertex(NETWORK_FLOW_SINK);
    }
}
