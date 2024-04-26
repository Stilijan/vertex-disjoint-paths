package walks;


import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Map;
import java.util.Set;

public class NetworkFlowWalk<V> extends Walk<V> {

    private static Map<DefaultWeightedEdge, Double> flowMap = null;
    private final V endVertex;

    private final V networkSource;

    private final V networkSink;
    public NetworkFlowWalk(Graph<V, DefaultWeightedEdge> graph,
                           V startVertex,
                           V endVertex,
                           V networkSource,
                           V networkSink) {

        super(graph, startVertex);
        this.endVertex = endVertex;

        this.networkSource = networkSource;
        this.networkSink = networkSink;
    }


    public static void initFlowMap(Map<DefaultWeightedEdge, Double> flowMap) {

        if (NetworkFlowWalk.flowMap == null) {
            NetworkFlowWalk.flowMap = flowMap;
        }
    }


    @Override
    public void generate() {

        V currentVertex = startVertex;

        while (currentVertex != endVertex) {

            path.add(currentVertex);
            Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);

            for (DefaultWeightedEdge edge : outgoingEdges) {

                if ((graph.getEdgeTarget(edge).equals(networkSource) ||
                    graph.getEdgeTarget(edge).equals(networkSink))) {

                    continue;
                }

                Double flow = flowMap.get(edge);

                if (flow > 0.0) {

                    currentVertex =
                        graph.getEdgeSource(edge) == currentVertex ? graph.getEdgeTarget(edge) : graph.getEdgeSource(edge);
                    flowMap.put(edge, 0.0);
                    break;
                }

            }
        }

        path.add(endVertex);
    }
}
