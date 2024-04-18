package graphLoader.impl;

import exceptions.GraphReadingException;
import graphLoader.GraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.io.*;

/**
 *  A component, which loads a simple directed graph (without self-loops and parallel arcs)
 */
public class SimpleDirectedGraphLoader implements GraphLoader<Integer, DefaultEdge> {

    private final String inputFilePath;


    public SimpleDirectedGraphLoader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    private static Graph<Integer, DefaultEdge> buildEmptySimpleDirectedGraph()
    {
        return GraphTypeBuilder
            .<Integer, DefaultEdge> directed()
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .edgeClass(DefaultEdge.class)
            .weighted(false)
            .buildGraph();
    }


    @Override
    public Graph<Integer, DefaultEdge> loadGraph() throws GraphReadingException {

        Graph<Integer, DefaultEdge> outputGraph = buildEmptySimpleDirectedGraph();

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

            }

            bufferedReader.close();

        } catch (IOException e) {

            throw new GraphReadingException(e.getMessage());
        }


        return outputGraph;
    }
}
