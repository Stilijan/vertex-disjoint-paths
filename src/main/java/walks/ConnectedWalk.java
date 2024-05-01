package walks;


import java.util.List;

public class ConnectedWalk<V> extends Walk<V>{

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

        assert walks.size() == 5;

        for (Walk<V> walk : walks) {

            this.path.addAll(walk.getPath());
        }

    }
}
