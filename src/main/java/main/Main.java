package main;

import exceptions.GraphFileNotFoundException;
import exceptions.GraphReadingException;
import graphLoader.impl.SimpleDirectedGraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import util.State;
import util.TransitionMatrix;
import walks.impl.RandomWalk;

import java.util.List;
import java.util.Random;

public class Main {


    public static void main(String[] args) throws GraphFileNotFoundException, GraphReadingException {

//        Graph<Integer, DefaultEdge> graph =
//            new SimpleDirectedGraphLoader("inputs/Square-n.10.0.gr").loadGraph();
//
//
//        List<Integer> randomWalk =
//            new RandomWalk(10, 971, graph)
//                .generate()
//                .removeCycles()
//                .getPath();
//
//        System.out.println(randomWalk);

        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);

        graph.addVertex(1);
        graph.addVertex(5);
        graph.addVertex(3);
        graph.addVertex(2);

        graph.addEdge(1, 2);
        graph.addEdge(1, 3);
        graph.addEdge(1, 5);
        graph.addEdge(2, 5);
        graph.addEdge(3, 1);
        graph.addEdge(3, 5);
        graph.addEdge(5, 3);


        TransitionMatrix<Integer> matrix = new TransitionMatrix<>(graph);

        State<Integer> state = new State<>(graph, 3);

        State<Integer> state2 = matrix.calculateNextState(state);
        State<Integer> state3 = matrix.calculateNextState(state2);

        System.out.println(state);

        System.out.println(matrix);

        System.out.println(state2);
        System.out.println("State 3: " + state3);

        System.out.println(matrix.getTransitionProbability(2, 3));



    }
}