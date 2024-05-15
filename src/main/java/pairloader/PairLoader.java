package pairloader;

import util.VertexPairs;


public interface PairLoader<V> {


    void generatePairs();

    void printPairs();

    VertexPairs<V> getPairs();

}
