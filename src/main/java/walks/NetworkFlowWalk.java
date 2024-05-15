package walks;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;
import java.util.Set;

public class NetworkFlowWalk extends Walk<Integer> {

    private static Map<DefaultWeightedEdge, Double> flowMap = null;
    private static final Logger LOGGER = LogManager.getLogger(NetworkFlowWalk.class);


    public static void setFlowMap(Map<DefaultWeightedEdge, Double> flowMap) {

        NetworkFlowWalk.flowMap = flowMap;
    }

    private final Integer networkSource;
    private final Integer networkSink;


    public NetworkFlowWalk(Graph<Integer, DefaultWeightedEdge> graph,
                           Integer startVertex,
                           Integer networkSource,
                           Integer networkSink) {

        super(graph, startVertex);

        this.networkSource = networkSource;
        this.networkSink = networkSink;
    }



    @Override
    public void generateWalk() {

        LOGGER.debug("Generating a vertex disjoint path from {} to a random vertex in K", startVertex);

        Integer currentVertex = startVertex;

        while (!flowMap.containsKey(graph.getEdge(currentVertex, networkSink))) {

            // if current vertex = vertex_in, add to path and just go directly to vertex_out
            if (currentVertex >= 0) {
                path.add(currentVertex);

                assert (flowMap.get(graph.getEdge(currentVertex, -currentVertex)).equals(1.0));

                currentVertex = -currentVertex;
                continue;
            }


            Set<DefaultWeightedEdge> outgoingEdgesOfCurrVertex = graph.outgoingEdgesOf(currentVertex);

            for (DefaultWeightedEdge edge : outgoingEdgesOfCurrVertex) {

                double flow = flowMap.get(edge);

                boolean isNotSourceOrSink = !(graph.getEdgeTarget(edge).equals(networkSource) ||
                    graph.getEdgeTarget(edge).equals(networkSink));
                boolean isExistentFlow = flow > 0.0;

                if (isNotSourceOrSink && isExistentFlow) {

                    currentVertex =
                        graph.getEdgeSource(edge).equals(currentVertex) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);

                    flowMap.put(edge, 0.0);
                    break;
                }

            }
        }
    }
}
