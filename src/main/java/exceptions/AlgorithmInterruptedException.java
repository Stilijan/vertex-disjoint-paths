package exceptions;

/**
 * This exception gets thrown, when the randomized algorithm gets into a
 * wrong state. After this, the algorithm must be re-executed.
 */
public class AlgorithmInterruptedException extends Exception{

    public AlgorithmInterruptedException(String msg) {
        super(msg);
    }
}
