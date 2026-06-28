package com.cleanroommc.kirino.ecs.system.exegraph.scheduler;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.schemata.graph.Hypergraph;
import com.cleanroommc.kirino.utils.GraphUtils;
import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static com.cleanroommc.kirino.ecs.system.exegraph.SystemExeFlowGraph.Builder;


public final class SystemScheduler {

    private final Hypergraph<Vertex, Edge> graph;
    private final Map<CleanSystem, Integer> priorities;
    private final ComponentRegistry componentRegistry;
    private final int resourceCount;

    public SystemScheduler(ComponentRegistry componentRegistry,
                           ResourceLayout layout) {
        Preconditions.checkNotNull(componentRegistry);
        Preconditions.checkNotNull(layout);

        this.componentRegistry = componentRegistry;
        this.priorities = new Object2IntArrayMap<>();

        //<editor-fold desc="Get Resource Count">
        try {
            final Class<ResourceLayout> resourceLayoutClass = ResourceLayout.class;
            final Field nextIDField = resourceLayoutClass.getDeclaredField("nextId");
            this.resourceCount = nextIDField.getInt(layout);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        //</editor-fold>

        this.graph = new Hypergraph<>();
    }

    /**
     * Add a system-component relation. If the system or component are not present,
     * they will be added ot the hypergraph.
     * @param system the system.
     * @param priority the priority of the system, determines which system will run first.
     * @param componentDependency the component the system depends on.
     * @see Hypergraph#add(Object, Object)
     */
    public void add(@NonNull CleanSystem system, int priority, @NonNull String componentDependency) {
        Preconditions.checkNotNull(system);
        Preconditions.checkState(priority >= 0);
        Preconditions.checkNotNull(componentDependency);
        Preconditions.checkArgument(componentRegistry.componentExists(componentDependency));

        priorities.put(system, priority);
        this.graph.add(new Edge(componentDependency), new Vertex(system, priority));
    }

    /**
     * Add a system-resource relation. If the system or resource are not present,
     * they will be added ot the hypergraph.
     * @param system the system
     * @param priority the priority of the system, determines which system will run first.
     * @param resourceDependency the resource the system depends on.
     * @see Hypergraph#add(Object, Object)
     */
    public void add(@NonNull CleanSystem system, int priority, int resourceDependency) {
        Preconditions.checkNotNull(system);
        Preconditions.checkState(priority >= 0);
        Preconditions.checkPositionIndex(resourceDependency, resourceCount);

        priorities.put(system, priority);
        this.graph.add(new Edge(resourceDependency), new Vertex(system, priority));
    }

    public void dependency(@NonNull CleanSystem dependency, @NonNull CleanSystem dependent) {
        Preconditions.checkNotNull(dependency);
        Preconditions.checkNotNull(dependent);

        graph.addVertexDependency(new Vertex(dependency, priorities.get(dependency)),
                new Vertex(dependent, priorities.get(dependent)));
    }

    public <TFlowGraph extends SystemExeFlowGraph> Builder<TFlowGraph> scheduleSystemExecution(
            Builder<TFlowGraph> builder) {
        final int colors = Runtime.getRuntime().availableProcessors();
        Graph<Vertex> disjointed = this.graph.buildDisjointedGraph();
        Optional<Map<Vertex, Integer>> colored = GraphUtils.colorGraph(disjointed, colors);
        if (colored.isPresent()) {
            PriorityQueue<Vertex>[] colorQueues = new PriorityQueue[colors];
            for (int i = 0; i < colors; i++) {
                colorQueues[i] = new ObjectHeapPriorityQueue<Vertex>();
            }
            for (Map.Entry<Vertex, Integer> entry : colored.get().entrySet()) {
                colorQueues[entry.getValue()].enqueue(entry.getKey());
            }
            for (int i = colors; i < colorQueues.length; i++) {
                while (!colorQueues[i].isEmpty()) {
                    Vertex vertex = colorQueues[i].dequeue();
                    // TODO: implement the actual building
                }
            }
        }

        return builder;
    }

    //<editor-fold desc="vertices">
    private record Vertex(CleanSystem system, int priority) implements Comparable<Vertex> {

        @Override
        public int compareTo(@NonNull Vertex o) {
            Preconditions.checkNotNull(system);

            return this.priority - o.priority;
        }
    }
    //</editor-fold>
    //<editor-fold desc="edges">
    private enum EdgeType {
        COMPONENT,
        RESOURCE
    }

    private record Edge(EdgeType type, Object id) {

        public Edge(int id) {
            this(EdgeType.RESOURCE, id);
        }

        public Edge(String name) {
            this(EdgeType.COMPONENT, name);
        }

        public String component() {
            Preconditions.checkState(type == EdgeType.COMPONENT);
            return (String)id;
        }

        public int resource() {
            Preconditions.checkState(type == EdgeType.RESOURCE);
            return (Integer)id;
        }
    }
    //</editor-fold>
}
