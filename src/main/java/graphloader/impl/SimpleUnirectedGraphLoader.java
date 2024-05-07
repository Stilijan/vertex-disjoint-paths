package graphloader.impl;

import exceptions.GraphReadingException;
import graphloader.GraphLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.*;

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

        Graph<Integer, DefaultWeightedEdge> outputGraph = buildEmptySimpleWeightedUndirectedGraph();

        FileReader fileReader;
        BufferedReader bufferedReader;


        try {

            fileReader = new FileReader(inputFilePath);

            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (!line.startsWith("a")) {
                    continue;
                }

                String[] lineSplit = line.split(" ");
                int sourceVertex = Integer.parseInt(lineSplit[1]);
                int targetVertex = Integer.parseInt(lineSplit[2]);

                outputGraph.addVertex(sourceVertex);
                outputGraph.addVertex(targetVertex);

                outputGraph.addEdge(sourceVertex, targetVertex);
                outputGraph.setEdgeWeight(sourceVertex, targetVertex, 1.0);

            }

            bufferedReader.close();

        } catch (IOException e) {

            LOGGER.fatal("Failure while reading the graph.");
            throw new GraphReadingException(e.getMessage());
        }


        return outputGraph;
    }
}
