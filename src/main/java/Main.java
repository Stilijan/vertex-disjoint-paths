import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import exceptions.AlgorithmInterruptedException;
import exceptions.GraphReadingException;
import exceptions.InvalidAlgorithmResultException;
import graphloader.GraphLoader;
import graphloader.impl.SimpleUnirectedGraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import util.GraphFiles;

import java.util.List;

public class Main {

    public static void main(String[] args)  {


        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleUnirectedGraphLoader(GraphFiles.RAND_500);


        Graph<Integer, DefaultWeightedEdge> graph;
        try {
            graph = weightedGraphLoader.loadGraph();
        } catch (GraphReadingException e) {

            return;
        }


        List<Integer> startVertices = List.of(2, 5);
        List<Integer> endVertices = List.of(35, 3);


        VertexDisjointPaths disjointPaths = new VertexDisjointPathsImpl(graph, startVertices, endVertices);

        try {
            disjointPaths.getDisjointWalks();
        } catch (AlgorithmInterruptedException e) {
            System.out.println(e.getMessage());
            return;
        }

        disjointPaths.printDisjointWalks();

        try {
            disjointPaths.verifyResult();
        } catch (InvalidAlgorithmResultException e) {
            System.out.println(e.getMessage());
        }
    }
}