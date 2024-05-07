package algorithm;

import exceptions.AlgorithmInterruptedException;
import exceptions.InvalidAlgorithmResultException;

public interface VertexDisjointPaths {

    /**
     * Returns the disjoint walks for each pair of start/end vertices.
     */
    void getDisjointWalks() throws AlgorithmInterruptedException;

    /**
     * Verifies, that the result has unique vertices
     * and the edges within each path exist.
     */
    void verifyResult() throws InvalidAlgorithmResultException;


    void printDisjointWalks();
}
