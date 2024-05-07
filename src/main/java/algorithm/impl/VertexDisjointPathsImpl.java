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



public class VertexDisjointPathsImpl implements VertexDisjointPaths {


    private static final Random RANDOM_GENERATOR = new Random();
    private static final Logger LOGGER = LogManager.getLogger(VertexDisjointPathsImpl.class);


    // ================== STEP 3 STATIC VARIABLES ==========================
    private static final List<Integer> aTildeVertices = new ArrayList<>();
    private static final List<Integer> bTildeVertices = new ArrayList<>();
    private static final int NETWORK_FLOW_SOURCE = Integer.MIN_VALUE;
    private static final int NETWORK_FLOW_SINK = Integer.MAX_VALUE;


    // ================== STEP 4 STATIC VARIABLES ==========================
    private static final List<Integer> aHatVertices = new ArrayList<>();
    private static final List<Integer> bHatVertices = new ArrayList<>();


    private final Graph<Integer, DefaultWeightedEdge> mainGraph;
    private final VertexPairs<Integer> pairs;
    private final int numberEdges;
    private final int numberVertices;
    private final int numberPairs;
    private final int lengthRandomWalk;

    private List<Walk<Integer>> result;





    public VertexDisjointPathsImpl(Graph<Integer, DefaultWeightedEdge> mainGraph,
                                   List<Integer> startPoints,
                                   List<Integer> endPoints) {
        
        this.mainGraph = mainGraph;
        this.pairs = new VertexPairs<>(startPoints, endPoints);

        this.numberEdges = mainGraph.edgeSet().size();
        this.numberVertices = mainGraph.vertexSet().size();
        this.numberPairs = pairs.getSize();


        double d = ((double) (2 * numberEdges)) / numberVertices;
        this.lengthRandomWalk =
            (int) Math.ceil(4.0 * Math.log(numberVertices) / Math.log(d));
    }


    @Override
    public void getDisjointWalks() throws AlgorithmInterruptedException {

        LOGGER.debug("Finding vertex disjoint paths in a graph with {} vertices and {} edges", mainGraph.vertexSet().size(), mainGraph.edgeSet().size());

        //================| STEP 1 |===================

        Set<Integer> allVertices = mainGraph.vertexSet();
        List<Integer> allStartEndVertices = pairs.getAllVertices();

        Set<Integer> x1Vertices = partitionVerticesIntoX1(allVertices);


        Set<Integer> xVertices = new HashSet<>();
        xVertices.addAll(x1Vertices);
        xVertices.addAll(allStartEndVertices);


        //================| STEP 2 |===================


        Set<Integer> kVertices = partitionVerticesIntoK(x1Vertices);


        //================| STEP 3 |===================


        Graph<Integer, DefaultWeightedEdge> graphX = new AsSubgraph<>(mainGraph, xVertices);

        // convert K from set to list
        List<Integer> kVerticesList = kVertices.stream().toList();

        assert kVerticesList.size() % 2 == 0;


        List<List<Walk<Integer>>> walks1And5 = extractWalks1AndWalks5(graphX, kVerticesList);
        List<Walk<Integer>> walks1 = walks1And5.get(0);
        List<Walk<Integer>> walks5 = walks1And5.get(1);

        
        assert walks1.size() == numberPairs;
        assert walks5.size() == numberPairs;


        //================| STEP 4 |===================


        // partition Y in Z1 and Z2
        Set<Integer> yVertices = new HashSet<>(mainGraph.vertexSet());
        yVertices.removeAll(xVertices);


        Set<Integer> z1Vertices = new HashSet<>();
        Set<Integer> z2Vertices = new HashSet<>();


        for (Integer vertex : yVertices) {

            if (RANDOM_GENERATOR.nextDouble() <= 1.0 / 2.0) {

                z1Vertices.add(vertex);
            } else {

                z2Vertices.add(vertex);
            }
        }


        // Create random walks
        List<List<Walk<Integer>>> walks2And4 = extractWalks2andWalks4(z1Vertices);

        List<Walk<Integer>> walks2 = walks2And4.get(0);
        List<Walk<Integer>> walks4 = walks2And4.get(1);

        assert walks2.size() == numberPairs;
        assert walks4.size() == numberPairs;

        //================| STEP 5 |===================

        List<Walk<Integer>> walks3 = extractWalks3(z2Vertices);

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
            connectedWalk.generateWalk();
            connectedWalks.add(connectedWalk);
        }

        this.result = connectedWalks;
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
                if (j > 0 && mainGraph.getEdge(walk.get(j - 1), currentVertex) == null && mainGraph.getEdge(currentVertex, walk.get(j - 1)) == null) {

                    LOGGER.error("Error: Edge {} - {} in walk {} doesn't exist.", walk.get(j - 1), currentVertex, i + 1);
                    throw new InvalidAlgorithmResultException("Edge %d - %d in walk %d doesn't exist".formatted(walk.get(j - 1), currentVertex, i + 1));
                }

                // Check if all vertices are unique
                if (!disjointVerticesMap.containsKey(currentVertex)) {

                    disjointVerticesMap.put(currentVertex, i);
                } else {

                    LOGGER.error("Error: Vertex {} in walk {} is contained also in walk {}.", walk.get(j), i + 1, disjointVerticesMap.get(currentVertex) + 1);
                    throw new InvalidAlgorithmResultException("Vertex %d in walk %d is contained also in walk %d.".formatted(walk.get(j), i + 1, disjointVerticesMap.get(currentVertex) + 1));
                }
            }
        }

        LOGGER.info("Result verified!");
    }

    @Override
    public void printDisjointWalks() {

        LOGGER.trace("Printing result.");

        for (int i = 0; i < result.size(); i++) {

            System.out.println("Walk " + (i + 1) + ": " + result.get(i));
        }
    }

    /**
     * Chooses uniformly vertices from {@code vertexSet} and puts them in a {@code K} vertex set.
     *
     * @param vertexSet vertex set to be chosen from.
     * @return set of vertices, namely K.
     */
    private Set<Integer> partitionVerticesIntoK(Set<Integer> vertexSet) {

        LOGGER.trace("Partitioning X1 into K");

        Set<Integer> kVertices = HashSet.newHashSet(2 * numberPairs);

        // convert X1 set to X1 list
        List<Integer> tempX1 = new ArrayList<>(vertexSet);

        while (kVertices.size() != 2 * numberPairs) {

            // choose a random vertex from X1 and add it to K
            int randomIndex = RANDOM_GENERATOR.nextInt(tempX1.size());
            Integer randomVertex = tempX1.get(randomIndex);
            kVertices.add(randomVertex);
        }

        return kVertices;
    }

    /**
     * Chooses randomly vertices from {@code vertexSet} with the specified {@code probability}.
     *
     * @param vertexSet vertex set to be chosen from.
     * @return set of vertices, namely X1, which is approximately 1/{@code probability} of the size of {@code vertexSet}.
     */
    private Set<Integer> partitionVerticesIntoX1(Set<Integer> vertexSet) {

        LOGGER.trace("Partitioning the main vertex set into X1");

        Set<Integer> x1Vertices = new HashSet<>();

        vertexSet.forEach(v -> {
            if (RANDOM_GENERATOR.nextDouble() <= 1.0 / 3.0) {
                x1Vertices.add(v);
            }
        });

        return x1Vertices;
    }


    /**
     * Returns W<sub>i</sub><sup>(3)</sup> by creating the shortest paths between each pair (a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup>).
     * a<sub>i</sub><sup>*</sup>, b<sub>i</sub><sup>*</sup> are random neighbours in the graph with {@code z2Vertices}
     * of a<sub>i</sub><sup>~</sup> and b<sub>i</sub><sup>~</sup>, respectively.
     *
     * @param z2Vertices the vertices in the graph, where the shortest path walk is generated.
     * @return a list of shortest path walks.
     */
    private List<Walk<Integer>> extractWalks3(Set<Integer> z2Vertices) throws AlgorithmInterruptedException{

        LOGGER.trace("Generating W(3)");

        Set<Integer> yIVertices = new HashSet<>(z2Vertices);
        List<Walk<Integer>> walks3 = new ArrayList<>();

        for (int i = 0; i < numberPairs; i++) {

            Integer aiHat = aHatVertices.get(i);
            Integer biHat = bHatVertices.get(i);

            Walk<Integer> shortestPathWalk = generateShortestPathWalk(aiHat, biHat, yIVertices);
            walks3.add(shortestPathWalk);

            shortestPathWalk.getPath().forEach(yIVertices::remove);
        }

        return walks3;
    }


    /**
     * Generates the shortest path between a<sub>i</sub><sup>*</sup> and b<sub>i</sub><sup>*</sup>, which are
     * random neighbours of a<sub>i</sub><sup>^</sup> and b<sub>i</sub><sup>^</sup>, respectively.
     *
     * @param aiHat the end vertex of W<sub>i</sub><sup>(2)</sup>.
     * @param biHat the end vertex of W<sub>i</sub><sup>(4)</sup>.
     * @param yIVertices the vertices of the graph, where the shortest path is generated.
     * @return the shortest path walk.
     */
    private Walk<Integer> generateShortestPathWalk(Integer aiHat, Integer biHat, Set<Integer> yIVertices) throws AlgorithmInterruptedException{

        LOGGER.debug("Generating the shortest path from a neighbour of {} to a neighbour of {}", aiHat, biHat);

        Graph<Integer, DefaultWeightedEdge>  graphYiHat = new AsSubgraph<>(mainGraph, yIVertices);

        List<Integer> neighboursOfAiHatInZ2 = getNeighboursOfVertexInGraph(aiHat, yIVertices);
        Integer aiStar = neighboursOfAiHatInZ2.get(RANDOM_GENERATOR.nextInt(neighboursOfAiHatInZ2.size()));


        List<Integer> neighboursOfBiHatInZ2 = getNeighboursOfVertexInGraph(biHat, yIVertices);
        Integer biStar = neighboursOfBiHatInZ2.get(RANDOM_GENERATOR.nextInt(neighboursOfBiHatInZ2.size()));


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
     * @param yJVertices vertex set of {@code graphJ}, which gets reduced after each generated walk.
     * @param reverse true, if the generated walk must be reversed.
     * @return the generated walk.
     */
    private Walk<Integer> generateRandomWalk(
        Graph<Integer, DefaultWeightedEdge> graphJ,
        Integer wj,
        Set<Integer> yJVertices,
        boolean reverse
    ) throws AlgorithmInterruptedException{

        LOGGER.debug("Generating a random walk with a neighbour of {} as a start vertex", wj);

        // get all neighbours of wj, which are in graphJ
        List<Integer> neighboursInZ1 = this.getNeighboursOfVertexInGraph(wj, yJVertices);

        // get a random neighbour of wj as a start vertex
        Integer startVertex =
            neighboursInZ1.get(RANDOM_GENERATOR.nextInt(neighboursInZ1.size()));

        // generate the walk
        Walk<Integer> randomWalk = new RandomWalk<>(graphJ, startVertex, lengthRandomWalk);
        randomWalk.generateWalk();

        return reverse ? randomWalk.reversed() : randomWalk;
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

        return neighboursInFilterVertices;
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

        // Initially, the vertex set of the graph contains all vertices from Z1
        Set<Integer> yJVertices = new HashSet<>(z1Vertices);


        List<Walk<Integer>> walks2 = new ArrayList<>();
        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks1.get(i)
            Integer aiTilde = aTildeVertices.get(i);

            Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, yJVertices);

            // generate a normal random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, aiTilde, yJVertices, false);

            aHatVertices.add(randomWalk.getEndVertex());
            walks2.add(randomWalk);

            // we remove the generated random walk from the graph,
            // in order to get disjoint paths
            randomWalk.getPath().forEach(yJVertices::remove);
        }

        walks2And4.add(walks2);

        List<Walk<Integer>> walks4 = new ArrayList<>();
        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks5.get(i)
            Integer biTilde = bTildeVertices.get(i);

            Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, yJVertices);

            // generate a reversed random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, biTilde, yJVertices, true);

            bHatVertices.add(randomWalk.getStartVertex());
            walks4.add(randomWalk);

            randomWalk.getPath().forEach(yJVertices::remove);
        }

        walks2And4.add(walks4);

        return walks2And4;
    }


    /**
     * Returns the disjoint walks W<sub>i</sub><sup>(1)</sup> and W<sub>i</sub><sup>(5)</sup> for {@code i} being from 1 to k.
     * These are evaluated by reducing the problem to the <a href="https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Edmonds-Karp Algorithm</a>
     * for finding the maximum flow in a network graph.
     *
     * @param graphX the graph with vertices from {@code X} vertex set.
     * @param kVertices the end vertices of the walks.
     * @return a list of two lists, each of them containing disjoint walks.
     */
    public List<List<Walk<Integer>>> extractWalks1AndWalks5(Graph<Integer, DefaultWeightedEdge> graphX,
                                                            List<Integer> kVertices) {

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
        ek.calculateMaximumFlow(NETWORK_FLOW_SOURCE, NETWORK_FLOW_SINK);

        // extract all vertex-disjoint paths
        Map<DefaultWeightedEdge, Double> flowMap = ek.getFlowMap();
        NetworkFlowWalk.setFlowMap(flowMap);


        List<Walk<Integer>> walks1 = new LinkedList<>();
        List<Walk<Integer>> walks5 = new LinkedList<>();

        for (Integer startVertex : this.pairs.getStartVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                graphXClone,
                startVertex,
                NETWORK_FLOW_SOURCE,
                NETWORK_FLOW_SINK
            );

            walk.generateWalk();

            walks1.add(walk);

            aTildeVertices.add(walk.getEndVertex());
        }

        for (Integer endVertex : this.pairs.getEndVertices()) {

            NetworkFlowWalk walk = new NetworkFlowWalk(
                graphXClone,
                endVertex,
                NETWORK_FLOW_SOURCE,
                NETWORK_FLOW_SINK
            );

            walk.generateWalk();

            walks5.add(walk.reversed());

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

        for (Integer vertex : graph.vertexSet()) {

            // add vertex_in in the cloned graph
            clonedGraph.addVertex(vertex);

            // add vertex_out in the cloned graph
            clonedGraph.addVertex(-vertex);

            // connect vertex_in and vertex_out
            clonedGraph.addEdge(vertex, -vertex);
            clonedGraph.setEdgeWeight(vertex, -vertex, 1.0);
        }

        for (Integer vertex : graph.vertexSet()) {

            if (vertex < 0) {
                continue;
            }

            // get all incoming/outgoing edges of vertex
            Set<DefaultWeightedEdge> edgesOfVertex = graph.edgesOf(vertex);

            for (DefaultWeightedEdge edge : edgesOfVertex) {

                // get source and target of edge
                Integer edgeSource = graph.getEdgeSource(edge);
                Integer edgeTarget = graph.getEdgeTarget(edge);

                // get the neighbour of vertex
                Integer neighbourOfVertex = edgeSource.equals(vertex) ? edgeTarget : edgeSource;

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
