package walks;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class ShortestPathWalk<V> extends Walk<V> {

    private V endVertex;

    public ShortestPathWalk(Graph<V, DefaultWeightedEdge> graph, V startVertex, V endVertex) {

        super(graph, startVertex);
        this.endVertex = endVertex;

    }


    @Override
    public void generate() {

        DijkstraShortestPath<V, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(graph);

        this.path = dijkstra
            .getPath(startVertex, endVertex)
            .getVertexList();
    }
}
