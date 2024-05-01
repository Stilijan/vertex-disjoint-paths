package walks;


import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;
import java.util.Set;

public class NetworkFlowWalk extends Walk<Integer> {

    private static Map<DefaultWeightedEdge, Double> flowMap = null;

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

                if ((graph.getEdgeTarget(edge).equals(networkSource) ||
                    graph.getEdgeTarget(edge).equals(networkSink))) {

                    continue;
                }

                Double flow = flowMap.get(edge);

                if (flow > 0.0) {

                    currentVertex =
                        graph.getEdgeSource(edge).equals(currentVertex) ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);

                    flowMap.put(edge, 0.0);
                    break;
                }

            }
        }
    }
}
