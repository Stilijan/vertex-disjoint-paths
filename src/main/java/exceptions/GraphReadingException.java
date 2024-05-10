package exceptions;

/**
 * This exception gets thrown, if an error occurs while
 * loading the graph into an object.
 */
public class GraphReadingException extends Exception{

    public GraphReadingException(String msg) {
        super(msg);
    }
}
