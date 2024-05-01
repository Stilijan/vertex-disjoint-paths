package algorithm;

import walks.Walk;

import java.util.List;

public interface VertexDisjointPaths<V> {

    List<Walk<V>> getDisjointWalks();
}
