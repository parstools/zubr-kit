package parstools.zubr.graph;

import java.util.*;

public class TarjansAlgorithm {
    private static Set<Integer> onStack;
    private static Deque<Integer> stack;
    private static Set<Integer> visited;

    private static Map<Integer, Integer> visitedTime;
    private static Map<Integer, Integer> lowTime;
    private static int time;

    private static List<List<Integer>> result;


    public static List<List<Integer>> calculateSCC(DG graph) {
        // Initialize all data structures
        setup();

        // start from any vertex in the graph
        for (DG.Vertex vertex : graph.getVertices()) {
            if (visited.contains(vertex.getId())) {
                continue;
            }
            calculateSCCUtil(vertex);
        }

        return result;
    }

    private static void calculateSCCUtil(DG.Vertex vertex) {
        visited.add(vertex.getId());

        visitedTime.put(vertex.getId(), time);
        lowTime.put(vertex.getId(), time);
        time++;

        stack.addFirst(vertex.getId());
        onStack.add(vertex.getId());

        for (DG.Vertex child : vertex.getAdjacentVertices()) {
            // If child hasn't been visited, see if it has a connection back to ancestor
            // -> Update low time to ancestor's visit time
            if (!visited.contains(child.getId())) {
                calculateSCCUtil(child);
                // Sets lowTime[vertex] = min(lowTime[vertex], lowTime[child])
                lowTime.compute(vertex.getId(), (v, low) -> Math.min(low, lowTime.get(child.getId())));
            // If child is on stack, check if visited before its low time. If so, update its low time to that
            } else if (onStack.contains(child.getId())) {
                lowTime.compute(vertex.getId(), (v, low) -> Math.min(low, visitedTime.get(child.getId())));
            }
        }

        // If vertex low time is same as visited time then this is the start vertex for SCC
        // Keep popping until finding current vertex. All part of SCC.
        if (visitedTime.get(vertex.getId()).equals(lowTime.get(vertex.getId()))) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pollFirst();
                onStack.remove(v);
                scc.add(v);
            } while (vertex.getId() != v);
            // Order the sCC in ascending fashion
            Collections.sort(scc);
            result.add(scc);
        }
    }



    // ### SETUP ###

    private static void setup() {
        // Initialize all stacks, lists, etc...
        onStack = new HashSet<>();
        stack = new LinkedList<>();
        visited = new HashSet<>();

        visitedTime = new HashMap<>();
        lowTime = new HashMap<>();
        time = 0;

        result = new ArrayList<>();
    }
}
