import exceptions.GraphReadingException;
import executor.Executor;
import executor.impl.VertexDisjointPathsExecutor;
import graphloader.GraphLoader;
import graphloader.impl.SimpleUnirectedGraphLoader;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import pairloader.PairLoader;
import pairloader.impl.PairLoaderImpl;
import util.GraphFiles;

public class Main {

    public static void main(String[] args)  {


        GraphLoader<Integer, DefaultWeightedEdge> weightedGraphLoader =
            new SimpleUnirectedGraphLoader(GraphFiles.RAND_300000);

        Graph<Integer, DefaultWeightedEdge> graph;

        try {
            graph = weightedGraphLoader.loadGraph();
        } catch (GraphReadingException e) {

            throw new RuntimeException(e.getMessage());
        }

        double alpha = 1.0 / 2.0;
        double beta = 1.0 / 2.0;

        PairLoader<Integer> pairLoader = new PairLoaderImpl(graph, alpha, beta);
        pairLoader.generatePairs(10);
        pairLoader.printPairs();



        Executor vdpExecutor = new VertexDisjointPathsExecutor(graph, pairLoader.getPairs());
        vdpExecutor.executeAlgorithm();

    }
}