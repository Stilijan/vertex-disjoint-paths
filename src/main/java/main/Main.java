package main;

import enums.ExecutionMode;
import exceptions.ExecutionInterruptedException;
import executor.Executor;
import executor.impl.VertexDisjointPathsExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        if (args.length < 3 || args.length > 4) {
           LOGGER.error("Algorithm interrupted, because the number of arguments is {}", args.length);
           System.exit(-1);
        }

        String inputPath = args[0];
        int k = Integer.parseInt(args[1]);
        ExecutionMode mode = ExecutionMode.valueOf(ExecutionMode.class, args[2].toUpperCase());
        int iterations = 1;

        if (args.length == 4) {
            iterations = Integer.parseInt(args[3]);
        }

        Executor vdpExecutor = new VertexDisjointPathsExecutor(
                inputPath,
                k,
                mode,
                iterations
        );

        try {
            vdpExecutor.executeProcedure();
        } catch (ExecutionInterruptedException e) {
            LOGGER.error("Algorithm interrupted");
            System.exit(-1);
        }
    }
}