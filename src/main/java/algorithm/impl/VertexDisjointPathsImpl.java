package algorithm.impl;

import algorithm.VertexDisjointPaths;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.graph.*;
import util.VertexPairs;
import walks.*;

import java.util.*;

public class VertexDisjointPathsImpl implements VertexDisjointPaths<Integer> {


    private static final Random RANDOM_GENERATOR = new Random();

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
    public List<Walk<Integer>> getDisjointWalks() {


        //================| STEP 1 |===================

        Set<Integer> allVertices = mainGraph.vertexSet();
        List<Integer> allStartEndVertices = pairs.getAllVertices();

        Set<Integer> x1Vertices = partitionVerticesIntoX1(allVertices, 1.0 / 3.0);


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

        List<Walk<Integer>> walks3 = createShortestPaths(z2Vertices, walks2, walks4);

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


        return connectedWalks;
    }

    /**
     * Chooses uniformly vertices from {@code vertexSet} and puts them in a {@code K} vertex set.
     *
     * @param vertexSet vertex set to be chosen from.
     * @return set of vertices, namely K.
     */
    private Set<Integer> partitionVerticesIntoK(Set<Integer> vertexSet) {

        Set<Integer> kVertices = HashSet.newHashSet(2 * numberPairs);

        // convert X1 set to X1 list
        List<Integer> tempX1 = new ArrayList<>(vertexSet);

        for (int i = 0; i < 2 * numberPairs; i++) {
            // Choose a random element from X1 that is not already in K
            int randomIndex;
            Integer randomVertex;
            do {
                randomIndex = RANDOM_GENERATOR.nextInt(tempX1.size());
                randomVertex = tempX1.get(randomIndex);
            } while (kVertices.contains(randomVertex));

            kVertices.add(randomVertex);
        }

        return kVertices;
    }

    /**
     * Chooses randomly vertices from {@code vertexSet} with the specified {@code probability}.
     *
     * @param vertexSet vertex set to be chosen from.
     * @param probability probability of choosing a vertex.
     * @return set of vertices, namely X1, which is approximately 1/{@code probability} of the size of {@code vertexSet}.
     */
    private Set<Integer> partitionVerticesIntoX1(Set<Integer> vertexSet, double probability) {

        Set<Integer> x1Vertices = new HashSet<>();

        vertexSet.forEach(v -> {
            if (RANDOM_GENERATOR.nextDouble() <= probability) {
                x1Vertices.add(v);
            }
        });

        return x1Vertices;
    }


    private List<Walk<Integer>> createShortestPaths(Set<Integer> z2Vertices,
                                                    List<Walk<Integer>> walks2,
                                                    List<Walk<Integer>> walks4) {


        List<Walk<Integer>> walks3 = new ArrayList<>();


        Set<Integer> tempYiVertices = z2Vertices;

        for (int i = 0; i < numberPairs; i++) {


            Graph<Integer, DefaultWeightedEdge>  graphYiHat = new AsSubgraph<>(mainGraph, tempYiVertices);

            Integer aiHat = walks2.get(i).getEndVertex();

            List<Integer> neighboursOfAiStar = Graphs.neighborListOf(mainGraph, aiHat);
            List<Integer> neighboursOfAiStarInZ2 = neighboursOfAiStar
                .stream()
                .distinct()
                .filter(z2Vertices::contains)
                .toList();

            Integer randomAiHat = neighboursOfAiStarInZ2.get(RANDOM_GENERATOR.nextInt(neighboursOfAiStarInZ2.size()));

            Integer biHat = walks4.get(i).getStartVertex();

            List<Integer> neighboursOfBiStar = Graphs.neighborListOf(mainGraph, biHat);
            List<Integer> neighboursOfBiStarInZ2 = neighboursOfBiStar
                .stream()
                .distinct()
                .filter(z2Vertices::contains)
                .toList();

            Integer randomBiHat = neighboursOfBiStarInZ2.get(RANDOM_GENERATOR.nextInt(neighboursOfAiStarInZ2.size()));

            Walk<Integer> walk = new ShortestPathWalk<>(graphYiHat, randomAiHat, randomBiHat);
            walk.generateWalk();

            walks3.add(walk);
            walk.getPath().forEach(tempYiVertices::remove);
        }

        return walks3;
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
    ) {

        // get all neighbours of wj, which are in graphJ
        List<Integer> allNeighbours = Graphs.neighborListOf(mainGraph, wj);
        List<Integer> neighboursInZ1 =
            allNeighbours
                .stream()
                .distinct()
                .filter(yJVertices::contains)
                .toList();

        // get a random neighbour of wj as a start vertex
        Integer startVertex =
            neighboursInZ1.get(RANDOM_GENERATOR.nextInt(neighboursInZ1.size()));

        // generate the walk
        Walk<Integer> randomWalk = new RandomWalk<>(graphJ, startVertex, lengthRandomWalk);
        randomWalk.generateWalk();

        return reverse ? randomWalk.reversed() : randomWalk;
    }


    /**
     * Returns disjoint walks W<sub>i</sub><sup>(2)</sup> and W<sub>i</sub><sup>(4)</sup>.
     * Random walks are generated between {@code aTildeVertices}/{@code bTildeVertices} and random endpoints
     * in the graph with {@code Z1} vertices.
     *
     * @param z1Vertices the vertices in the graph, where the random walks are generated.
     * @return list of 2 elements - W<sub>i</sub><sup>(2)</sup> and W<sub>i</sub><sup>(4)</sup>, which are lists with walks.
     */
    private List<List<Walk<Integer>>> extractWalks2andWalks4(Set<Integer> z1Vertices) {

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

        walks2And4.add(0, walks2);

        List<Walk<Integer>> walks4 = new ArrayList<>();
        for (int i = 0; i < numberPairs; i++) {

            // get the endpoint of walks5.get(i)
            Integer biTilde = bTildeVertices.get(i);

            Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, yJVertices);

            // generate a reversed random walk
            Walk<Integer> randomWalk = generateRandomWalk(graphJ, biTilde, yJVertices, true);

            bHatVertices.add(randomWalk.getEndVertex());
            walks4.add(randomWalk);

            randomWalk.getPath().forEach(yJVertices::remove);
        }

        walks2And4.add(1, walks4);

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

        graph.removeVertex(NETWORK_FLOW_SOURCE);
        graph.removeVertex(NETWORK_FLOW_SINK);
    }
}
