package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * This is the designed to be an immutable schedule.
 *
 * <p>Implicit prerequisites include:</p>
 * <ul>
 *     <li>When HDR is on, it must contain a tone mapping subpass</li>
 *     <li>Must not contain duplicate subpasses, it'll fail silently during runtime</li>
 * </ul>
 */
public final class PostProcessingSchedule {

    public static final PostProcessingSchedule EMPTY = PostProcessingSchedule.builder().build();

    private final ImmutableList<ScheduledPass> subpasses;

    private PostProcessingSchedule(List<ScheduledPass> subpasses) {
        this.subpasses = ImmutableList.copyOf(subpasses);
    }

    public int getSubpassCount() {
        return subpasses.size();
    }

    @NonNull
    public ScheduledPass getSubpass(int index) {
        Preconditions.checkElementIndex(index, subpasses.size());

        return subpasses.get(index);
    }

    @NonNull
    public List<@NonNull ScheduledPass> getSubpasses() {
        return subpasses;
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    public static final class ScheduledPass {

        private final String registeredName;
        private final int priority;

        private ScheduledPass(String registeredName, int priority) {
            this.registeredName = registeredName;
            this.priority = priority;
        }

        @NonNull
        public String getRegisteredName() {
            return registeredName;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "ScheduledPass { registeredName=\"" + registeredName + "\", priority=" + priority + " }";
        }
    }

    public static final class EntryRef {

        private final Builder owner;
        private final int index;

        private EntryRef(Builder owner, int index) {
            this.owner = owner;
            this.index = index;
        }
    }

    public static final class Builder {

        private final List<Entry> entries = new ArrayList<>();

        /**
         * @param priority Larger the <code>priority</code>, less prioritized the pass
         */
        @NonNull
        public EntryRef addPass(@NonNull String registeredName, int priority) {
            Preconditions.checkNotNull(registeredName);

            return addSequence(priority, registeredName);
        }

        @NonNull
        public EntryRef addPass(@NonNull String registeredName) {
            Preconditions.checkNotNull(registeredName);

            return addPass(registeredName, 0);
        }

        /**
         * @param priority Larger the <code>priority</code>, less prioritized the pass
         */
        @NonNull
        public EntryRef addSequence(int priority, @NonNull String @NonNull ... registeredNames) {
            Preconditions.checkNotNull(registeredNames);
            for (String name : registeredNames) {
                Preconditions.checkNotNull(name);
            }
            Preconditions.checkArgument(registeredNames.length != 0,
                    "Argument \"registeredNames\" must not be empty.");

            List<String> names = new ArrayList<>(registeredNames.length);
            names.addAll(Arrays.asList(registeredNames));

            int index = entries.size();

            entries.add(new Entry(
                    index,
                    priority,
                    ImmutableList.copyOf(names)));

            return new EntryRef(this, index);
        }

        /**
         * It adds hard ordering constraints. It overrides the given priorities.
         * It doesn't guarantee adjacency.
         *
         * <p>Order: <code>first</code> -> <code>second</code></p>
         */
        @NonNull
        public Builder before(@NonNull EntryRef first, @NonNull EntryRef second) {
            Preconditions.checkNotNull(first);
            Preconditions.checkNotNull(second);

            Entry firstEntry = getEntry(first);
            Entry secondEntry = getEntry(second);

            firstEntry.outgoing.add(secondEntry.index);
            return this;
        }

        /**
         * It adds hard ordering constraints. It overrides the given priorities.
         * It doesn't guarantee adjacency.
         *
         * <p>Order: <code>entries[0]</code> -> <code>entries[1]</code> -> <code>entries[2]</code></p>
         */
        @NonNull
        public Builder chain(@NonNull EntryRef @NonNull ... refs) {
            Preconditions.checkNotNull(refs);
            for (EntryRef ref : refs) {
                Preconditions.checkNotNull(ref);
            }

            for (int i = 1; i < refs.length; i++) {
                before(refs[i - 1], refs[i]);
            }

            return this;
        }

        @NonNull
        public PostProcessingSchedule build() {
            int entryCount = entries.size();
            int[] indegree = new int[entryCount];

            for (Entry entry : entries) {
                for (int target : entry.outgoing) {
                    indegree[target]++;
                }
            }

            PriorityQueue<Integer> available =
                    new PriorityQueue<>(new Comparator<Integer>() {

                        @Override
                        public int compare(Integer leftIndex, Integer rightIndex) {
                            Entry left = entries.get(leftIndex);
                            Entry right = entries.get(rightIndex);

                            int priorityComparison = Integer.compare(left.priority, right.priority);
                            if (priorityComparison != 0) {
                                return priorityComparison;
                            }

                            return Integer.compare(left.index, right.index);
                        }
                    });

            for (int i = 0; i < entryCount; i++) {
                if (indegree[i] == 0) {
                    available.add(i);
                }
            }

            List<ScheduledPass> result = new ArrayList<>();
            int sortedEntryCount = 0;

            while (!available.isEmpty()) {
                int entryIndex = available.remove();
                Entry entry = entries.get(entryIndex);

                sortedEntryCount++;

                for (String registeredName : entry.registeredNames) {
                    result.add(new ScheduledPass(
                            registeredName,
                            entry.priority));
                }

                for (int target : entry.outgoing) {
                    indegree[target]--;

                    if (indegree[target] == 0) {
                        available.add(target);
                    }
                }
            }

            Preconditions.checkState(sortedEntryCount == entryCount,
                    "Post-processing schedule contains a hard-ordering cycle.");

            return new PostProcessingSchedule(result);
        }

        private Entry getEntry(EntryRef ref) {
            Preconditions.checkState(ref.owner == this,
                    "EntryRef belongs to another schedule builder.");

            return entries.get(ref.index);
        }

        private static final class Entry {

            private final int index;
            private final int priority;
            private final ImmutableList<String> registeredNames;
            private final Set<Integer> outgoing = new LinkedHashSet<>();

            private Entry(
                    int index,
                    int priority,
                    ImmutableList<String> registeredNames) {

                this.index = index;
                this.priority = priority;
                this.registeredNames = registeredNames;
            }
        }
    }
}
