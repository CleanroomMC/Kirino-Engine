package com.cleanroommc.test.kirino.graph;

import com.cleanroommc.kirino.utils.GraphUtils;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphColoringTest {
    @Test
    public void testGraphColoring() {
        MutableGraph<Integer> graph = GraphBuilder.undirected().allowsSelfLoops(false).build();
        graph.putEdge(0, 1);
        graph.putEdge(1, 2);
        graph.putEdge(2, 3);
        graph.putEdge(3, 4);
        graph.putEdge(4, 5);
        graph.putEdge(5, 6);
        graph.putEdge(6, 0);
        graph.putEdge(0, 2);
        graph.putEdge(1, 3);
        graph.putEdge(2, 4);
        graph.putEdge(3, 5);
        graph.putEdge(4, 6);
        graph.putEdge(5, 0);
        graph.putEdge(6, 1);
        graph.putEdge(0, 3);
        graph.putEdge(1, 4);
        graph.putEdge(2, 5);
        Optional<Map<Integer, Integer>> colored = GraphUtils.colorGraph(graph, 3);
        assertTrue(colored.isPresent());
        Map<Integer, Integer> color = colored.get();
        for (var colorEntry : color.entrySet()) {
            for (Integer vertex : graph.adjacentNodes(colorEntry.getKey())) {
                assertNotEquals(colorEntry.getValue(), color.get(vertex));
            }
        }
    }
}
