package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.graph.Graph;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Utilities regarding graphs unavailable in {@link Graph guava graph classes}.
 */
public final class GraphUtils {

    /**
     * DSatur algorithm for graph coloring.
     * @param graph The {@link Graph graph}
     * @param availableColors Available Colors
     * @return An {@link Optional} of a {@link Map} of vertices to colors,
     * if the graph is empty, then the value is empty as well.
     * @param <V> Type of graph vertices.
     */
    public static <V> Optional<Map<V, Integer>> colorGraph(@NonNull Graph<V> graph, int availableColors) { // TODO: throw an error or return Optional.empty() if availableColors is lower than the chromatic number of the graph
        Preconditions.checkNotNull(graph);

        if (graph.nodes().isEmpty() || graph.edges().isEmpty()) {
            return Optional.empty();
        }

        record VertexInfo<V>(int saturation, int degree, V vertex) implements Comparable<VertexInfo<V>> {
            @Override
            public int compareTo(@NonNull VertexInfo<V> o) {
                Preconditions.checkNotNull(o);
                if (this.saturation != o.saturation) {
                    return saturation - o.saturation;
                } else if (degree != o.degree) {
                    return degree - o.degree;
                } else {
                    return vertex.hashCode() - o.vertex.hashCode();
                }
            }
        }

        BitSet usedColors = new BitSet(availableColors);
        V currVertex;
        int currColor;
        Map<V, Integer> colors = new Reference2IntArrayMap<>();
        Map<V, Integer> degrees = new Reference2IntArrayMap<>();
        Multimap<V, Integer> adjColors = HashMultimap.create();
        PriorityQueue<VertexInfo<V>> verticesToColor = new PriorityQueue<>(); // TODO: Replace with Fibonacci Heap

        for (V v : graph.nodes()) {
            colors.put(v, -1);
            degrees.put(v, graph.degree(v));
            verticesToColor.add(new VertexInfo<>(0, degrees.get(v), v));
        }

        while (!verticesToColor.isEmpty()) {
            VertexInfo<V> info = verticesToColor.poll();
            currVertex = info.vertex;
            Set<V> adj = graph.adjacentNodes(currVertex);
            // Set all unavailable colors.
            for (V v : adj) {
                if (colors.get(v) != -1) {
                    usedColors.set(colors.get(v));
                }
            }
            // Find first availableColor
            for (currColor = 0; currColor < availableColors; currColor++) {
                if (!usedColors.get(currColor)) {
                    break;
                }
            }
            // Reset color filter
            for (V v : adj) {
                if (colors.get(v) != -1) {
                    usedColors.set(colors.get(v), false);
                }
            }
            colors.put(currVertex, currColor); // Set color
            // Push adjacent vertices to coloring queue
            for (V v : adj) {
                if (colors.get(v) == -1) {
                    verticesToColor.remove(new VertexInfo<>(adjColors.get(v).size(),
                            degrees.get(v), v));
                    adjColors.put(v, currColor);
                    degrees.compute(v, (ignored, val) -> val != null ? --val : graph.degree(v)-1);
                    verticesToColor.add(new VertexInfo<>(adjColors.get(v).size(),
                            degrees.get(v), v));
                }
            }
        }

        return Optional.of(colors);
    }
}
