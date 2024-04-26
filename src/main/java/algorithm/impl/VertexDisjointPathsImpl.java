package algorithm.impl;

import algorithm.VertexDisjointPaths;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import util.VertexPairs;
import walks.*;

import java.util.*;

public class VertexDisjointPathsImpl implements VertexDisjointPaths<Integer> {

    private static final Random RANDOM_GENERATOR = new Random();



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
        double d = ((double) (2 * numberEdges)) / numberVertices;
        this.lengthRandomWalk =
            (int) Math.ceil(4.0 * Math.log(numberVertices) / Math.log(d));
        this.numberPairs = pairs.getSize();
    }


    @Override
    public List<Walk<Integer>> executeAlgorithm() {


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

        int mid = kVerticesList.size() / 2;

        List<Integer> aTildeVertices = kVerticesList.subList(0, mid);
        List<Integer> bTildeVertices = kVerticesList.subList(mid, kVerticesList.size());


        List<Integer> startVertices = pairs.getStartVertices();
        List<Walk<Integer>> walks1 =
            getVertexDisjointPathsUsingNetworkFlowAlgorithm(graphX, startVertices, aTildeVertices);


        List<Integer> endVertices = pairs.getEndVertices();
        List<Walk<Integer>> walks5 =
            getVertexDisjointPathsUsingNetworkFlowAlgorithm(graphX, bTildeVertices, endVertices);

        assert walks1.size() == numberPairs;
        assert walks5.size() == numberPairs;


        //================| STEP 4 |===================


        // partition Y in Z1 and Z2
        Set<Integer> yVertices = mainGraph.vertexSet();
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
        List<Walk<Integer>> walks2 = createRandomWalks(false, z1Vertices, walks1, kVerticesList);
        List<Walk<Integer>> walks4 = createRandomWalks(true, z1Vertices, walks5, kVerticesList);

        assert walks2.size() == numberPairs;
        assert walks4.size() == numberPairs;

        //================| STEP 5 |===================

        List<Walk<Integer>> walks3 = createShortestPaths(z2Vertices, walks2,walks4);

        assert walks3.size() == numberPairs;
        
        //================| STEP 6 |===================


        List<Walk<Integer>> connectedWalks = new ArrayList<>(numberPairs);


        for (int i = 0; i < numberPairs; i++) {

            List<Walk<Integer>> walkParts = new LinkedList<>();

            walkParts.add(walks1.get(i).removeCycles());
            walkParts.add(walks2.get(i).removeCycles());
            walkParts.add(walks3.get(i).removeCycles());
            walkParts.add(walks4.get(i).removeCycles());
            walkParts.add(walks5.get(i).removeCycles());

            Walk<Integer> connectedWalk = new ConnectedWalk<>(walkParts);
            connectedWalk.generate();
            connectedWalks.add(connectedWalk);
        }


        return connectedWalks;
    }

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

            Integer aiHat = walks2.get(i).getEndpoint();

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
            walk.generate();

            walks3.add(walk);
            walk.getPath().forEach(tempYiVertices::remove);
        }

        return walks3;
    }



    private List<Walk<Integer>> createRandomWalks(boolean isReversed,
                                                  Set<Integer> yJVertices,
                                                  List<Walk<Integer>> prevWalks,
                                                  List<Integer> kVerticesList) {

        Set<Integer> tempYJVertices = yJVertices;
        Set<Integer> z1Vertices = yJVertices;

        List<Walk<Integer>> walks = new ArrayList<>();

        for (int i = 0; i < numberPairs; i++) {

            // get a random start vertex from walks1
            Graph<Integer, DefaultWeightedEdge> graphJ = new AsSubgraph<>(mainGraph, tempYJVertices);

            Walk<Integer> currWalk = prevWalks.get(i);
            Integer lastVertexOfCurrWalk = currWalk.getEndpoint();

            List<Integer> allNeighbours = Graphs.neighborListOf(mainGraph, lastVertexOfCurrWalk);
            List<Integer> neighboursInZ1 = allNeighbours.stream().distinct().filter(z1Vertices::contains).toList();

            Integer startVertex = neighboursInZ1.get(RANDOM_GENERATOR.nextInt(neighboursInZ1.size()));


            // create a random walk
            Walk<Integer> randomWalk = new RandomWalk<>(graphJ, startVertex, lengthRandomWalk);
            randomWalk.generate();

            List<Integer> path;

            // reverse for walks4
            if (isReversed) {

                path = randomWalk.getPath().reversed();
                path.add(kVerticesList.get(i));
            } else {

                path = randomWalk.getPath();
                path.add(0, kVerticesList.get(i));
            }

            randomWalk.setPath(path);


            walks.add(randomWalk);
            randomWalk.getPath().forEach(tempYJVertices::remove);

        }

        return walks;

    }




    private List<Walk<Integer>> getVertexDisjointPathsUsingNetworkFlowAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph,
        List<Integer> starts,
        List<Integer> ends
    ) {

        // create temporarily a source/sink vertex and
        // connect it with all start/end vertices, respectively
        Integer networkFlowSource = -1;
        Integer networkFlowSink = -2;

        mainGraph.addVertex(networkFlowSink);
        mainGraph.addVertex(networkFlowSource);

        graph.addVertex(networkFlowSource);
        graph.addVertex(networkFlowSink);

        for (int i = 0; i < starts.size(); i++) {

            mainGraph.addEdge(networkFlowSource, starts.get(i));
            mainGraph.addEdge(ends.get(i), networkFlowSink);

            graph.addEdge(networkFlowSource, starts.get(i));
            graph.addEdge(ends.get(i), networkFlowSink);
        }


        // calculate the maximum flow of the graph with the
        // Edmonds-Karp Maximum Flow Algorithm
        EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> ek = new EdmondsKarpMFImpl<>(graph);
        ek.calculateMaximumFlow(networkFlowSource, networkFlowSink);

        // extract all vertex-disjoint paths
        Map<DefaultWeightedEdge, Double> flowMap = ek.getFlowMap();
        NetworkFlowWalk.initFlowMap(flowMap);

        List<Walk<Integer>> disjointWalks = new LinkedList<>();
        for (int i = 0; i < starts.size(); i++) {

            NetworkFlowWalk<Integer> walk = new NetworkFlowWalk<>(
                graph,
                starts.get(i),
                ends.get(i),
                networkFlowSource,
                networkFlowSink
            );

            walk.generate();

            disjointWalks.add(walk);
        }

        // remove source and sink
        mainGraph.removeVertex(networkFlowSource);
        mainGraph.removeVertex(networkFlowSink);

        graph.removeVertex(networkFlowSource);
        graph.removeVertex(networkFlowSink);

        return disjointWalks;
    }
}
