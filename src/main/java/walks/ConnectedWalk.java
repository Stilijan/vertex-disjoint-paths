package walks;


import java.util.List;

public class ConnectedWalk<V> extends Walk<V>{

    List<Walk<V>> walks;

    public ConnectedWalk(List<Walk<V>> walks) {
        super();
        this.walks = walks;
    }

    @Override
    public void generate() {

        assert walks.size() == 5;

        for (Walk<V> walk : walks) {

            this.path.addAll(walk.getPath());
        }

    }
}
