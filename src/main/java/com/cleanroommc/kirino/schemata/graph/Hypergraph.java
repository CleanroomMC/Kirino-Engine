package com.cleanroommc.kirino.schemata.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.graph.*;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jspecify.annotations.NonNull;

import java.util.Set;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Hypergraph">hypergraph</a>, a mathematical structure representing an
 * association of many vertices to many edges.
 * @param <V> Vertex Type
 * @param <E> Edge Type
 * @implNote Currently uses {@link Multimap Multimaps} for implementation.
 * Can probably be sped up with a better data structure, this is a draft.
 * This class does not implement all functions related to graph like graph degree
 * or graph coloring because YAGNI.
 */
public class Hypergraph<V,E> {
    private final Multimap<E,V> verticesPerEdge;
    private final Multimap<V,E> edgesPerVertex;
    private final MutableGraph<V> vertexToVertexDependencies;

    /**
     * Creates the hypergraph.
     */
    public Hypergraph() {
        this.verticesPerEdge = HashMultimap.create();
        this.edgesPerVertex = HashMultimap.create();
        this.vertexToVertexDependencies = GraphBuilder.<V>directed().allowsSelfLoops(false).build();
    }

    /**
     * Adds a vertex to an edge in the hypergraph, if the edge does not exist, it is created.
     * @param edge the edge
     * @param vertex the vertex
     */
    public void add(@NonNull E edge, @NonNull V vertex) {
        Preconditions.checkNotNull(edge);
        Preconditions.checkNotNull(vertex);

        this.edgesPerVertex.put(vertex, edge);
        this.verticesPerEdge.put(edge, vertex);
        this.vertexToVertexDependencies.addNode(vertex);
    }

    public void addVertexDependency(@NonNull V dependency, @NonNull V dependent) {
        Preconditions.checkNotNull(dependency);
        Preconditions.checkNotNull(dependent);

        this.vertexToVertexDependencies.putEdge(dependency, dependent);
    }

    /**
     * Removes an association from the hypergraph, if there are no more associations between the edge/vertex they are deleted.
     * @param edge the edge
     * @param vertex the vertex
     */
    public void remove(@NonNull E edge, @NonNull V vertex) {
        Preconditions.checkNotNull(edge);
        Preconditions.checkNotNull(vertex);

        this.edgesPerVertex.remove(vertex, edge);
        this.verticesPerEdge.remove(edge, vertex);
        this.vertexToVertexDependencies.removeNode(vertex);
    }

    /**
     * Gets all the vertices that share an edge with the vertex.
     * @param vertex the vertex
     * @return A set of all the vertices that share an edge with the vertex
     */
    @NonNull
    public Set<V> getNeighbours(@NonNull V vertex) {
        Preconditions.checkNotNull(vertex);

        ReferenceArraySet<V> neighbours = new ReferenceArraySet<>();

        for (E edge : this.edgesPerVertex.get(vertex)) {
            for (V neighbour : this.verticesPerEdge.get(edge)) {
                if (!neighbour.equals(vertex)) {
                    neighbours.add(neighbour);
                }
            }
        }

        neighbours.addAll(vertexToVertexDependencies.adjacentNodes(vertex));

        return neighbours;
    }

    private boolean canReach(@NonNull V from, @NonNull V to) {
        for (V vertex : Traverser.forGraph(vertexToVertexDependencies).breadthFirst(from)) {
            if (vertex.equals(to)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Squashed the hypergraph into a graph, strips edge metadata, then inverts it.
     * The resulting graph contains all the vertices of the hypergraph,
     * connected to vertices, that share no edges.
     * @return Inverted squashed graph.
     * @apiNote Uses classes and methods marked with {@link com.google.common.annotations.Beta @Beta}
     */
    @NonNull
    public Graph<V> buildDisjointedGraph() {
        ImmutableGraph.Builder<V> builder = GraphBuilder.undirected()
                .allowsSelfLoops(false)
                .expectedNodeCount(verticesPerEdge.keySet().size())
                .immutable();

        Set<V> vertices = edgesPerVertex.keySet();
        Multimap<V, V> bannedRelationships = MultimapBuilder.hashKeys().hashSetValues().build();

        for (V vertex : vertices) {
            builder.addNode(vertex);
            Set<V> neighbours = getNeighbours(vertex);
            for (V tmp : vertices) {
                if (!tmp.equals(vertex)
                        && !neighbours.contains(tmp)
                        && !bannedRelationships.containsEntry(vertex, tmp)) {
                    if (canReach(tmp, vertex)) {
                        bannedRelationships.put(vertex, tmp);
                        bannedRelationships.put(tmp, vertex);
                        continue;
                    }
                    builder.addNode(tmp);
                    builder.putEdge(vertex, tmp);
                    neighbours.add(tmp);
                    neighbours.addAll(vertexToVertexDependencies.adjacentNodes(tmp));
                }
            }
        }

        return builder.build();
    }
}
