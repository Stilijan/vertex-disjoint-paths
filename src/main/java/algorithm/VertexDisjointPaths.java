package algorithm;

import exceptions.InvalidAlgorithmResultException;


public interface VertexDisjointPaths {

    /**
     * Searches for disjoint paths for each pair of start/end vertices.
     * @return true, if the algorithm finds disjoint paths.
     */
    boolean findDisjointPaths();

    /**
     * Verifies, that the result has unique vertices
     * and the edges within each path exist.
     * @throws InvalidAlgorithmResultException if the output of the algorithm is invalid.
     */
    void verifyResult() throws InvalidAlgorithmResultException;


    /**
     * Prints the disjoint paths.
     */
    void printDisjointPaths();


    long getAlgorithmDuration();
}
