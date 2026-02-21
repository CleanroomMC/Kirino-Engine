package com.cleanroommc.kirino.engine.render.platform.debug.data.impl;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataService;
import com.google.common.base.Preconditions;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshletGpuTimeline implements DebugDataService {

    public record TimeSpan(int start, int end) {
    }

    public enum State {
        IDLE_NO_MESHLET_UPDATE("[IDLE] No Meshlet Update", new Color(93, 75, 255, 140)),
        ARMED_FINISH_WRITING("[IDLE->ARMED] Finish Writing", new Color(147, 75, 255, 140)),
        ARMED_BEGIN_WRITING("[IDLE->ARMED] Begin Writing", new Color(240, 74, 246, 140)),
        ARMED_ALREADY_WRITING("[IDLE->ARMED] Already Writing", new Color(255, 75, 102, 140)),
        COMPUTABLE_FINISH_WRITING("[COMPUTABLE] Finish Writing", new Color(255, 117, 91, 140)),
        COMPUTABLE_BEGIN_COMPUTING("[COMPUTABLE] Begin Computing", new Color(255, 160, 90, 140)),
        COMPUTABLE_BEGIN_WRITING("[COMPUTABLE] Begin Writing", new Color(255, 217, 90, 140)),
        COMPUTABLE_FINISH("[COMPUTABLE->IDLE] Finish", new Color(219, 255, 90, 140));

        public final String name;
        public final Color color;

        State(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public static final int RECORD_TICK_SPAN = 100;

    private int currTickIndex = -1;
    private int writeTaskStartTime = -1; // -1 stands for not recording atm
    private int computeTaskStartTime = -1; // -1 stands for not recording atm

    private int timelineViewStartIndex = 0;

    private final List<TimeSpan> writeTimeline = new ArrayList<>();
    private final List<TimeSpan> computeTimeline = new ArrayList<>();

    private final boolean[] meshletUpdates = new boolean[RECORD_TICK_SPAN];

    @SuppressWarnings("unchecked")
    private final List<State>[] frameStateFlows = (List<State>[]) new List[RECORD_TICK_SPAN];

    public boolean[] getMeshletUpdates() {
        return meshletUpdates;
    }

    public List<State>[] getFrameStateFlows() {
        return frameStateFlows;
    }

    public int getTimelineViewStartIndex() {
        return timelineViewStartIndex;
    }

    public void setTimelineViewStartIndex(int index) {
        timelineViewStartIndex = index;
    }

    public List<TimeSpan> getWriteTimeline() {
        return writeTimeline;
    }

    public List<TimeSpan> getComputeTimeline() {
        return computeTimeline;
    }

    public void loadInNewWorld() {
        writeTimeline.clear();
        computeTimeline.clear();
        Arrays.fill(meshletUpdates, false);
        Arrays.fill(frameStateFlows, null);
        timelineViewStartIndex = 0;
        writeTaskStartTime = -1;
        computeTaskStartTime = -1;
        currTickIndex = 0; // activates the service
    }

    private void deactivate() {
        currTickIndex = -1;
        if (writeTaskStartTime != -1) {
            // unfinished
            writeTimeline.add(new TimeSpan(writeTaskStartTime, RECORD_TICK_SPAN + 1));
            writeTaskStartTime = -1;
        }
        if (computeTaskStartTime != -1) {
            // unfinished
            computeTimeline.add(new TimeSpan(computeTaskStartTime, RECORD_TICK_SPAN + 1));
            computeTaskStartTime = -1;
        }
    }

    public void worldTick() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        if (currTickIndex < RECORD_TICK_SPAN) {
            if (++currTickIndex == RECORD_TICK_SPAN) {
                deactivate();
            }
        } else {
            deactivate();
        }
    }

    public void hasMeshletUpdate() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        meshletUpdates[currTickIndex] = true;
    }

    public void pushFrameState(State state) {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        if (frameStateFlows[currTickIndex] == null) {
            frameStateFlows[currTickIndex] = new ArrayList<>();
        }

        frameStateFlows[currTickIndex].add(state);
    }

    public void beginWriting() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        Preconditions.checkState(writeTaskStartTime == -1,
                "Must not be recording write timeline already.");

        writeTaskStartTime = currTickIndex;
    }

    public void finishWriting() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        Preconditions.checkState(writeTaskStartTime != -1,
                "Must be recording write timeline already.");

        writeTimeline.add(new TimeSpan(writeTaskStartTime, currTickIndex));

        writeTaskStartTime = -1;
    }

    public void beginComputing() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        Preconditions.checkState(computeTaskStartTime == -1,
                "Must not be recording compute timeline already.");

        computeTaskStartTime = currTickIndex;
    }

    public void finishComputing() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        Preconditions.checkState(computeTaskStartTime != -1,
                "Must be recording compute timeline already.");

        computeTimeline.add(new TimeSpan(computeTaskStartTime, currTickIndex));

        computeTaskStartTime = -1;
    }
}
