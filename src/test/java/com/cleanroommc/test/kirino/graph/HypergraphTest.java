package com.cleanroommc.test.kirino.graph;

import com.cleanroommc.kirino.schemata.graph.Hypergraph;
import com.google.common.graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypergraphTest {
    @Test
    public void testDisjointedGraph() {
        Hypergraph<Integer, Integer> graph = new Hypergraph<>();
        graph.add(0,0);
        graph.add(0,1);
        graph.add(0,2);
        graph.add(1,0);
        graph.add(1,3);
        graph.add(1,4);
        graph.add(2,1);
        graph.add(2,3);
        graph.add(2,5);
        Graph<Integer> disjointed = graph.buildDisjointedGraph();
        assertTrue(disjointed.hasEdgeConnecting(0,5));
        assertTrue(disjointed.hasEdgeConnecting(1,4));
        assertTrue(disjointed.hasEdgeConnecting(2,3));
        assertTrue(disjointed.hasEdgeConnecting(2,4));
        assertTrue(disjointed.hasEdgeConnecting(2,5));
        assertFalse(disjointed.hasEdgeConnecting(0,1));
        assertFalse(disjointed.hasEdgeConnecting(0,2));
        assertFalse(disjointed.hasEdgeConnecting(1,2));
        assertFalse(disjointed.hasEdgeConnecting(1,3));
        assertFalse(disjointed.hasEdgeConnecting(1,5));
        assertFalse(disjointed.hasEdgeConnecting(3,4));
        assertFalse(disjointed.hasEdgeConnecting(3,5));
    }

    @Test
    public void testVertexDependencies() {
        Hypergraph<Integer, Integer> graph = new Hypergraph<>();
        graph.add(0,0);
        graph.add(1,1);
        graph.add(2,2);
        graph.add(3,3);
        graph.add(4,4);
        graph.add(5,5);
        graph.add(6,6);
        graph.addVertexDependency(0,1);
        graph.addVertexDependency(1,2);
        graph.addVertexDependency(2,3);
        graph.addVertexDependency(4,5);
        graph.addVertexDependency(5,6);
        graph.addVertexDependency(6,3);
        Graph<Integer> disjointed = graph.buildDisjointedGraph();
        assertTrue(disjointed.hasEdgeConnecting(0,4));
        assertTrue(disjointed.hasEdgeConnecting(1,5));
        assertTrue(disjointed.hasEdgeConnecting(2,6));
        for (int i = 0; i <= 6; i++) {
            if (i != 3) {
                assertFalse(disjointed.hasEdgeConnecting(i,3));
            }
        }
    }
}
