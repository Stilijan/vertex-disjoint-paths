package walks;

import exceptions.AlgorithmInterruptedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BidirectionalDijkstraShortestPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class ShortestPathWalk<V> extends Walk<V> {

    private static final Logger LOGGER = LogManager.getLogger(ShortestPathWalk.class);
    private final V endVertex;

    public ShortestPathWalk(Graph<V, DefaultWeightedEdge> graph, V startVertex, V endVertex) {

        super(graph, startVertex);
        this.endVertex = endVertex;

    }


    @Override
    public void generateWalk() throws AlgorithmInterruptedException {

        LOGGER.debug("Generating the shortest path between {} and {}", startVertex, endVertex);


        BidirectionalDijkstraShortestPath<V, DefaultWeightedEdge> dijkstra =
            new BidirectionalDijkstraShortestPath<>(graph);

        try {
            this.path = dijkstra
                .getPath(startVertex, endVertex)
                .getVertexList();
        } catch (NullPointerException e) {

            LOGGER.error("There is no path between {} and {}", startVertex, endVertex);
            throw new AlgorithmInterruptedException("There is no path between %s and %s".formatted(startVertex, endVertex));
        }
    }
}
