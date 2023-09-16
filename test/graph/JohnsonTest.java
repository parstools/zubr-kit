package graph;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JohnsonTest {
    @Test
    void vertexCycle() {
        DG graph = new DG(3);
        graph.addEdge(0,1,3);
        graph.addEdge(1,2,5);
        graph.addEdge(2,0,7);
        graph.addEdge(1,2,11);
        List<List<VertexEdge>> johnsonResult = JohnsonsAlgorithm.calculateCycles(graph);
        assertEquals("[[[0,-1], [1,3], [2,5], [0,7]], [[0,-1], [1,3], [2,11], [0,7]]]",johnsonResult.toString());
    }
}