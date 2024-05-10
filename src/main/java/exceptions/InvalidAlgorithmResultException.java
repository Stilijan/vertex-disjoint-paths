package exceptions;

/**
 * This exception gets thrown, if the algorithm outputs a
 * wrong solution.
 */
public class InvalidAlgorithmResultException extends Exception{

    public InvalidAlgorithmResultException(String msg) {

        super(msg);
    }
}
