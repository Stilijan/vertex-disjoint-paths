package pairloader;

import exceptions.MaximumNumberOfPairsExceededException;
import util.VertexPairs;


public interface PairLoader<V> {


    void generatePairs() throws MaximumNumberOfPairsExceededException;

    void printPairs();

    VertexPairs<V> getPairs();

}
