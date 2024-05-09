package algorithm;

import exceptions.AlgorithmInterruptedException;
import exceptions.InvalidAlgorithmResultException;

public interface VertexDisjointPaths {

    /**
     * Searches for disjoint paths for each pair of start/end vertices.
     * @return true, if the algorithm finds disjoint paths.
     */
    boolean findDisjointWalks();

    /**
     * Verifies, that the result has unique vertices
     * and the edges within each path exist.
     */
    void verifyResult() throws InvalidAlgorithmResultException;


    void printDisjointWalks();
}
