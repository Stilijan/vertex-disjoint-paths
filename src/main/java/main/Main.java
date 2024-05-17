package main;

import exceptions.ExecutionInterruptedException;
import executor.Executor;
import executor.impl.VertexDisjointPathsExecutor;
import util.GraphFiles;

public class Main {
    public static void main(String[] args)  {

        Executor vdpExecutor = new VertexDisjointPathsExecutor(GraphFiles.RAND_300000, 200);

        try {
            vdpExecutor.executeAlgorithm();
        } catch (ExecutionInterruptedException e) {
            System.exit(-1);
        }
    }
}