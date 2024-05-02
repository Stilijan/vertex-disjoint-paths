import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import exceptions.GraphReadingException;
import graphLoader.GraphLoader;
import graphLoader.impl.SimpleDirectedGraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.flow.PushRelabelMFImpl;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import walks.Walk;

import java.util.*;

public class Main {


    public static void main(String[] args) throws GraphReadingException {


        String path = "./inputs/rands/rand_50_750.gr";

        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleDirectedGraphLoader(path);

        Graph<Integer, DefaultWeightedEdge> graph = weightedGraphLoader.loadGraph();


        List<Integer> startVertices = List.of(2, 5);
        List<Integer> endVertices = List.of(22, 25);


        VertexDisjointPaths<Integer> disjointPaths = new VertexDisjointPathsImpl(graph, startVertices, endVertices);

        List<Walk<Integer>> res = disjointPaths.getDisjointWalks();

        for (int i = 0; i < res.size(); i++) {

            System.out.println("Walk " + (i + 1) + ": " + res.get(i));
        }
    }
}