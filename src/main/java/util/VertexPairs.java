package util;

import java.util.ArrayList;
import java.util.List;

public class VertexPairs<V>{

    private final List<V> startVertices;
    private final List<V> endVertices;

    public VertexPairs() {
        this.startVertices = new ArrayList<>();
        this.endVertices = new ArrayList<>();
    }


    public void addOneVertexPair(V startVertex, V endVertex) {

        this.startVertices.add(startVertex);
        this.endVertices.add(endVertex);
    }

    public List<V> getAllVertices() {

        List<V> union = new ArrayList<>();

        union.addAll(startVertices);
        union.addAll(endVertices);

        return union;
    }

    public List<V> getStartVertices() {
        return startVertices;
    }

    public List<V> getEndVertices() {
        return endVertices;
    }

    public int getSize() {

        return this.startVertices.size();
    }

    @Override
    public String toString() {

        StringBuilder res = new StringBuilder();

        for (int i = 0; i < startVertices.size(); i++) {

            res.append("Pair %s: (%s, %s)%n".formatted(i + 1, startVertices.get(i), endVertices.get(i)));
        }

        return res.toString();
    }
}
