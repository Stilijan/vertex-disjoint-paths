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

    /**
     * Initialize with concrete lists of vertices
     *
     * @param startVertices a list of vertices Ai (1 <= i <= n).
     * @param endVertices a list of vertices Bi (1 <= i <= n) and (Ai, Bi) is a vertex pair.
     */
    public VertexPairs(List<V> startVertices, List<V> endVertices) {
        this.startVertices = startVertices;
        this.endVertices = endVertices;
    }

    public void addOneVertexPair(V startVertex, V endVertex) {

        this.startVertices.add(startVertex);
        this.endVertices.add(endVertex);
    }

    public List<V> getOneVertexPair(int i) {

        return List.of(startVertices.get(i), endVertices.get(i));
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
}
