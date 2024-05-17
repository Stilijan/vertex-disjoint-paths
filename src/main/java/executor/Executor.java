package executor;

import exceptions.ExecutionInterruptedException;

public interface Executor {

    /**
     * Executes an algorithm.
     *
     * @throws ExecutionInterruptedException if the algorithm is blocked because of an error state.
     */
    void executeAlgorithm() throws ExecutionInterruptedException;
}
