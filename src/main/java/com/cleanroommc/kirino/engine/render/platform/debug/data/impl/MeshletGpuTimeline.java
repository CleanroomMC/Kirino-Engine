package com.cleanroommc.kirino.engine.render.platform.debug.data.impl;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataService;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MeshletGpuTimeline implements DebugDataService {

    @Override
    public boolean isActive() {
        return true;
    }

    public static final int RECORD_TICK_SPAN = 100;

    public record TimeSpan(int start, int end) {
    }

    private int currTickIndex = -1;
    private int writeTaskStartTime = -1; // -1 stands for not recording atm
    private int computeTaskStartTime = -1; // -1 stands for not recording atm

    private int timelineViewStartIndex = 0;

    private final List<TimeSpan> writeTimeline = new ArrayList<>();
    private final List<TimeSpan> computeTimeline = new ArrayList<>();

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
        timelineViewStartIndex = 0;
        writeTaskStartTime = -1;
        computeTaskStartTime = -1;
        currTickIndex = 0; // activates the service
    }

    public void worldTick() {
        // proceed only if when active
        if (currTickIndex == -1) {
            return;
        }

        if (currTickIndex < RECORD_TICK_SPAN) {
            currTickIndex++;
        } else {
            // deactivates the service
            currTickIndex = -1;
        }
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
