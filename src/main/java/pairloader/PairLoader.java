package pairloader;

import util.VertexPairs;

import java.util.List;

public interface PairLoader<V> {


    void generatePairs(int numberOfPairs);
    List<V> getStartVertices();
    List<V> getEndVertices();

    void printPairs();

    VertexPairs<V> getPairs();
}
