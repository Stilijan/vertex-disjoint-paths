package algorithm.impl;

import algorithm.VertexDisjointPaths;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import walks.Walk;

import java.util.List;

public class VertexDisjointPathsImpl implements VertexDisjointPaths<Integer> {

    
    private final Graph<Integer, DefaultEdge> graph;
    private final List<Integer> startpoints;
    private final List<Integer> endpoints;

    public VertexDisjointPathsImpl(Graph<Integer, DefaultEdge> graph, List<Integer> startpoints, List<Integer> endpoints) {
        
        this.graph = graph;
        this.startpoints = startpoints;
        this.endpoints = endpoints;
    }


    @Override
    public List<Walk<Integer>> executeAlgorithm() {
        return null;
    }
}
