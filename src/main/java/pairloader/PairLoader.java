package pairloader;

import util.VertexPairs;

import java.util.List;

public interface PairLoader<V> {


    void generatePairs(int numberPairs);

    void printPairs();

    VertexPairs<V> getPairs();

}
