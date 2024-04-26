import algorithm.VertexDisjointPaths;
import algorithm.impl.VertexDisjointPathsImpl;
import exceptions.GraphReadingException;
import graphLoader.GraphLoader;
import graphLoader.impl.SimpleDirectedGraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import walks.Walk;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Main {

    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws GraphReadingException {

        String path = "./inputs/rands/rand_50.gr";

        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleDirectedGraphLoader(path);

        Graph<Integer, DefaultWeightedEdge> graph = weightedGraphLoader.loadGraph();

        int countPaths = 3;

        List<Integer> startVertices = List.of(2, 5);
        List<Integer> endVertices = List.of(22, 25);


        VertexDisjointPaths<Integer> disjointPaths = new VertexDisjointPathsImpl(graph, startVertices, endVertices);

        List<Walk<Integer>> res = disjointPaths.executeAlgorithm();

        for (int i = 0; i < res.size(); i++) {

            System.out.println("Walk " + i + ": " + res.get(i));
        }


    }
}