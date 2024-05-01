package walks;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import util.State;
import util.TransitionMatrix;

import java.util.*;

public class WalkSubroutine extends Walk<Integer> {


    private final TransitionMatrix<Integer> transitionMatrixIHat;
    private final TransitionMatrix<Integer> transitionMatrixJ;
    private final int length;

    private final Integer aiStar;
    private final Integer biStar;

    private final Graph<Integer, DefaultEdge> graphIHat;
    private final Graph<Integer, DefaultEdge> graphJ;

    private final Integer wj;
    private final Integer neighbourOfWj;

    private List<Integer> path;

    public WalkSubroutine(int length,
                          int aiStar,
                          int biStar,
                          Graph<Integer, DefaultEdge> graphIHat,
                          Graph<Integer, DefaultEdge> graphJ,
                          int wj,
                          int neighbourOfWj) {

        super();

        this.aiStar = aiStar;
        this.biStar = biStar;
        this.graphIHat = graphIHat;
        this.graphJ = graphJ;
        this.wj = wj;
        this.neighbourOfWj = neighbourOfWj;



        this.length = length;

        this.path = new ArrayList<>();

        this.transitionMatrixIHat = new TransitionMatrix<>(graphIHat);
        this.transitionMatrixJ = new TransitionMatrix<>(graphJ);
    }




    @Override
    public void generateWalk() {

        State<Integer> pv = probabilitiesOfARandomWalk(aiStar, length, graphIHat, transitionMatrixIHat);

        Set<Integer> yJ = graphJ.vertexSet();

        double[] pvHatProbabilities = new double[yJ.size()];

        int i = 0;
        for (Integer v : yJ) {

            double sum = 0.0;
            List<Integer> neighbours = Graphs.neighborListOf(graphJ, v);

            for (Integer u : neighbours) {

                int neighboursSize = Graphs.neighborListOf(graphIHat, u).size();

                sum += probabilityOfARandomWalkBetween(neighbourOfWj, u, length, graphJ, transitionMatrixJ) / neighboursSize;
            }

            pvHatProbabilities[i++] = sum;
        }

        State<Integer> pvHat = new State<>(graphJ, pvHatProbabilities);


        double pMin = Arrays.stream(pv.getProbabilities()).min().getAsDouble();
        double pHatMax = Arrays.stream(pvHat.getProbabilities()).max().getAsDouble();

        while (true) {

            Walk<Integer> wr = null;//new RandomWalk(length, aiStar, graphIHat);

            Integer xr = wr.getEndVertex();

            double pxr = pv.getProbabilityToReachVertex(xr);
            double pHatXr = pvHat.getProbabilityToReachVertex(xr);

            double probability = (pHatXr * pMin) / (pxr * pHatMax);

            if (Math.random() < probability) {
                return;
            }
        }

    }


    private double probabilityOfARandomWalkBetween(Integer v1, Integer v2, int length, Graph<Integer, DefaultEdge> graph, TransitionMatrix<Integer> matrix) {

        State<Integer> initState = new State<>(graph, v1);

        return matrix
            .calculateNextStateAfterNTransitions(initState, length)
            .getProbabilityToReachVertex(v2);
    }

    private State<Integer> probabilitiesOfARandomWalk(Integer start, int length, Graph<Integer, DefaultEdge> graph, TransitionMatrix<Integer> matrix) {

        State<Integer> initState = new State<>(graph, start);

        return matrix.calculateNextStateAfterNTransitions(initState, length);
    }


}
