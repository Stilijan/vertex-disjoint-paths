package executor;

import exceptions.ExecutionInterruptedException;

public interface Executor {

    /**
     * Executes an algorithm, as well as its preparation procedures.
     *
     * @throws ExecutionInterruptedException if the algorithm is blocked because of an error state.
     */
    void executeProcedure() throws ExecutionInterruptedException;
}
