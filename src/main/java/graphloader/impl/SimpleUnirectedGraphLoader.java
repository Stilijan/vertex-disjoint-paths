package graphloader.impl;

import exceptions.GraphReadingException;
import graphloader.GraphLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.*;
import java.util.stream.Stream;

/**
 *  A component, which loads a simple directed graph (without self-loops and parallel arcs)
 *  The edges of the loaded graph have weight.
 */
public class SimpleUnirectedGraphLoader implements GraphLoader<Integer, DefaultWeightedEdge> {

    private static final Logger LOGGER = LogManager.getLogger(SimpleUnirectedGraphLoader.class);
    private final String inputFilePath;


    public SimpleUnirectedGraphLoader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    private static Graph<Integer, DefaultWeightedEdge> buildEmptySimpleWeightedUndirectedGraph()
    {
        return GraphTypeBuilder
            .<Integer, DefaultWeightedEdge> undirected()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .edgeClass(DefaultWeightedEdge.class)
            .weighted(true)
            .buildGraph();
    }


    @Override
    public Graph<Integer, DefaultWeightedEdge> loadGraph() throws GraphReadingException {

        LOGGER.debug("Generating graph from {}", inputFilePath);

        Graph<Integer, DefaultWeightedEdge> outputGraph = buildEmptySimpleWeightedUndirectedGraph();


        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFilePath))) {

            bufferedReader
                .lines()
                .filter(line -> line.startsWith("a"))
                .map(line -> line.split("\\s"))
                .forEach(lineSplit -> {

                    int sourceVertex = Integer.parseInt(lineSplit[1]);
                    int targetVertex = Integer.parseInt(lineSplit[2]);

                    outputGraph.addVertex(sourceVertex);
                    outputGraph.addVertex(targetVertex);

                    outputGraph.addEdge(sourceVertex, targetVertex);
//                    outputGraph.setEdgeWeight(sourceVertex, targetVertex, 1.0);
                });

        } catch (IOException e) {

            LOGGER.fatal("Failure while reading the graph.");
            throw new GraphReadingException(e.getMessage());
        }


        LOGGER.info("Graph with {} vertices and {} edges generated.", outputGraph.vertexSet().size(), outputGraph.edgeSet().size());
        return outputGraph;
    }
}
