package pairloader;

import exceptions.MaximumNumberOfPairsExceeded;
import util.VertexPairs;


public interface PairLoader<V> {


    void generatePairs() throws MaximumNumberOfPairsExceeded;

    void printPairs();

    VertexPairs<V> getPairs();

}
