package walks;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ConnectedWalk<V> extends Walk<V>{

    private static final Logger LOGGER = LogManager.getLogger(ConnectedWalk.class);

    private final List<Walk<V>> walks;

    public ConnectedWalk(List<Walk<V>> walks) {
        super();
        this.walks = walks;
    }

    /**
     * Connects all 5 walks into a single walk.
     */
    @Override
    public void generateWalk() {

        LOGGER.trace("Connecting all parts of a walk");

        assert walks.size() == 5;

        for (Walk<V> walk : walks) {

            this.path.addAll(walk.getPath());
        }

    }
}
