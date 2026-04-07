package com.cleanroommc.kirino.engine.render.platform.debug.hud.impl;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.platform.debug.data.impl.MeshletGpuTimeline;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MeshletGpuTimelineHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        var meshletGpuTimeline = KirinoClientCore.DEBUG_SERVICE.get(MeshletGpuTimeline.class);
        boolean drawTimeline = false;
        List<MeshletGpuTimeline.TimeSpan> writeTimeline = null;
        List<MeshletGpuTimeline.TimeSpan> computeTimeline = null;
        boolean[] meshletUpdates = null;
        List<MeshletGpuTimeline.State>[] frameStateFlows = null;
        var meshletGpuTimelineValue = meshletGpuTimeline.fetch();
        if (meshletGpuTimelineValue != null) {
            drawTimeline = true;
            writeTimeline = meshletGpuTimelineValue.getWriteTimeline();
            computeTimeline = meshletGpuTimelineValue.getComputeTimeline();
            meshletUpdates = meshletGpuTimelineValue.getMeshletUpdates();
            frameStateFlows = meshletGpuTimelineValue.getFrameStateFlows();
        }

        MeshletGpuTimeline.State[] states = MeshletGpuTimeline.State.values();
        for (MeshletGpuTimeline.State state : states) {
            hud.beginHorizontal();
            float x = hud.getPivotX();
            float y = hud.getPivotY();
            hud.empty(12, 12);
            hud.drawRect(x + 2, y + 2, 8, 8, state.color.getRGB());
            hud.text(state.name);
            hud.endHorizontal();
        }

        if (drawTimeline) {
            handleInput(meshletGpuTimelineValue);

            int startIndex = meshletGpuTimelineValue.getTimelineViewStartIndex();

            hud.text("[Row 1] Writing Task; Count: " + writeTimeline.size());
            hud.text("[Row 2] Computing Task; Count: " + computeTimeline.size());

            float x = hud.getPivotX();
            float y = hud.getPivotY();
            hud.empty(120, 10 + 77);

            drawTimeline(
                    hud,
                    x + 5, y + 5, startIndex,
                    writeTimeline,
                    computeTimeline,
                    meshletUpdates,
                    frameStateFlows);
        }
    }

    private static boolean lastLeft = false;
    private static boolean lastRight = false;

    private static void handleInput(MeshletGpuTimeline meshletGpuTimeline) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            return;
        }

        boolean leftDown = Keyboard.isKeyDown(Keyboard.KEY_LEFT);
        if (leftDown && !lastLeft) {
            int index = meshletGpuTimeline.getTimelineViewStartIndex();
            index--;
            index = Math.max(index, 0);
            index = Math.min(index, MeshletGpuTimeline.RECORD_TICK_SPAN - 11);
            meshletGpuTimeline.setTimelineViewStartIndex(index);
        }
        lastLeft = leftDown;

        boolean rightDown = Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
        if (rightDown && !lastRight) {
            int index = meshletGpuTimeline.getTimelineViewStartIndex();
            index++;
            index = Math.max(index, 0);
            index = Math.min(index, MeshletGpuTimeline.RECORD_TICK_SPAN - 11);
            meshletGpuTimeline.setTimelineViewStartIndex(index);
        }
        lastRight = rightDown;
    }

    //<editor-fold desc="draw timeline; independent helper method">
    private static final Color EMPTY_SLOT_COLOR = new Color(28, 28, 28, 71);
    private static final Color START_SLOT_COLOR = new Color(191, 255, 75, 140);
    private static final Color END_SLOT_COLOR = new Color(255, 146, 79, 140);

    // temporary data (per draw call)
    private static final int[] writeTimelineSlots = new int[11];
    private static final int[] computeTimelineSlots = new int[11];
    private static final List<Vector2f> writeLineSegments = new ArrayList<>();
    private static final List<Vector2f> computeLineSegments = new ArrayList<>();

    // overcomplicated but works
    @SuppressWarnings({"SameParameterValue", "ConstantConditions"})
    private static void genLineSegments(
            int startIndex, /* input */
            List<MeshletGpuTimeline.TimeSpan> writeTimeline, /* input */
            List<MeshletGpuTimeline.TimeSpan> computeTimeline, /* input */
            int[] writeTimelineSlots, /* input */
            int[] computeTimelineSlots, /* input */
            List<Vector2f> writeLineSegments, /* output */
            List<Vector2f> computeLineSegments /* output */) {

        float startPos = 0f;
        boolean waitingStart = true;
        boolean waitingEnd = false;
        int index = 0;
        while (index < 11) {
            // if the "start" has an index<0; case 1
            if (index == 0 && writeTimelineSlots[index] == 0) {
                // move to next slot until it hits something
                // flag: whether it hits anything
                boolean flag = true;
                while (writeTimelineSlots[++index] == 0) {
                    if (index == 10) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    // hits "end" slot
                    if (writeTimelineSlots[index] == 2) {
                        writeLineSegments.add(new Vector2f(0, index + 0.5f));
                        startPos = 0;
                        waitingStart = true;
                        waitingEnd = false;
                        if (++index >= 11) {
                            break;
                        }

                    // hits "end/start" slot
                    } else if (writeTimelineSlots[index] == 4) {
                        writeLineSegments.add(new Vector2f(0, index + 0.25f));
                        startPos = index + 0.75f;
                        waitingStart = false;
                        waitingEnd = true;
                        if (++index >= 11) {
                            writeLineSegments.add(new Vector2f(startPos, 11f));
                            break;
                        }
                    }
                }
            }

            // if the "start" has an index<0; case 2
            if (index == 0 && writeTimelineSlots[index] == 2) {
                startPos = 0f;
                waitingStart = true;
                waitingEnd = false;
                index++;
                writeLineSegments.add(new Vector2f(0f, 0.5f));
            }

            // if the "start" has an index<0; case 3
            if (index == 0 && writeTimelineSlots[index] == 4) {
                startPos = 0.75f;
                waitingStart = false;
                waitingEnd = true;
                index++;
                writeLineSegments.add(new Vector2f(0f, 0.25f));
            }

            // get "start" or "start/end" slot
            if (waitingStart && (writeTimelineSlots[index] == 1 || writeTimelineSlots[index] == 3)) {
                // "start" slot
                if (writeTimelineSlots[index] == 1) {
                    startPos = index + 0.5f;
                    waitingStart = false;
                    waitingEnd = true;
                    if (++index >= 11) {
                        writeLineSegments.add(new Vector2f(startPos, 11f));
                        break;
                    }
                    continue;

                // "start/end" slot
                } else if (writeTimelineSlots[index] == 3) {
                    writeLineSegments.add(new Vector2f(index + 0.25f, index + 0.75f));
                    startPos = 0f;
                    waitingStart = true;
                    waitingEnd = false;
                    index++;
                    continue;
                }

            // get "end" or "end/start" slot
            } else if (waitingEnd && (writeTimelineSlots[index] == 2 || writeTimelineSlots[index] == 4)) {
                // "end" slot
                if (writeTimelineSlots[index] == 2) {
                    writeLineSegments.add(new Vector2f(startPos, index + 0.5f));
                    startPos = 0f;
                    waitingStart = true;
                    waitingEnd = false;
                    index++;
                    continue;

                // "end/start" slot
                } else if (writeTimelineSlots[index] == 4) {
                    writeLineSegments.add(new Vector2f(startPos, index + 0.25f));
                    startPos = index + 0.75f;
                    waitingStart = false;
                    waitingEnd = true;
                    if (++index >= 11) {
                        writeLineSegments.add(new Vector2f(startPos, 11f));
                        break;
                    }
                    continue;
                }

            } else if (waitingEnd) {
                // move to next slot until it hits something
                // flag: whether it hits anything
                boolean flag = true;
                if (index == 10) {
                    flag = false;
                } else {
                    while (writeTimelineSlots[++index] == 0) {
                        if (index == 10) {
                            flag = false;
                            break;
                        }
                    }
                }

                if (flag) {
                    // jump to the next non-empty slot
                    continue;
                } else {
                    // "end" has an index>10
                    writeLineSegments.add(new Vector2f(startPos, 11f));
                    break;
                }
            }

            index++;
        }

        startPos = 0f;
        waitingStart = true;
        waitingEnd = false;
        index = 0;
        while (index < 11) {
            // if the "start" has an index<0; case 1
            if (index == 0 && computeTimelineSlots[index] == 0) {
                // move to next slot until it hits something
                // flag: whether it hits anything
                boolean flag = true;
                while (computeTimelineSlots[++index] == 0) {
                    if (index == 10) {
                        flag = false;
                        break;
                    }
                }

                if (flag) {
                    // hits "end" slot
                    if (computeTimelineSlots[index] == 2) {
                        computeLineSegments.add(new Vector2f(0, index + 0.5f));
                        startPos = 0;
                        waitingStart = true;
                        waitingEnd = false;
                        if (++index >= 11) {
                            break;
                        }

                    // hits "end/start" slot
                    } else if (computeTimelineSlots[index] == 4) {
                        computeLineSegments.add(new Vector2f(0, index + 0.25f));
                        startPos = index + 0.75f;
                        waitingStart = false;
                        waitingEnd = true;
                        if (++index >= 11) {
                            computeLineSegments.add(new Vector2f(startPos, 11f));
                            break;
                        }
                    }
                }
            }

            // if the "start" has an index<0; case 2
            if (index == 0 && computeTimelineSlots[index] == 2) {
                startPos = 0f;
                waitingStart = true;
                waitingEnd = false;
                index++;
                computeLineSegments.add(new Vector2f(0f, 0.5f));
            }

            // if the "start" has an index<0; case 3
            if (index == 0 && computeTimelineSlots[index] == 4) {
                startPos = 0.75f;
                waitingStart = false;
                waitingEnd = true;
                index++;
                computeLineSegments.add(new Vector2f(0f, 0.25f));
            }

            // get "start" or "start/end" slot
            if (waitingStart && (computeTimelineSlots[index] == 1 || computeTimelineSlots[index] == 3)) {
                // "start" slot
                if (computeTimelineSlots[index] == 1) {
                    startPos = index + 0.5f;
                    waitingStart = false;
                    waitingEnd = true;
                    if (++index >= 11) {
                        computeLineSegments.add(new Vector2f(startPos, 11f));
                        break;
                    }
                    continue;

                // "start/end" slot
                } else if (computeTimelineSlots[index] == 3) {
                    computeLineSegments.add(new Vector2f(index + 0.25f, index + 0.75f));
                    startPos = 0f;
                    waitingStart = true;
                    waitingEnd = false;
                    index++;
                    continue;
                }

            // get "end" or "end/start" slot
            } else if (waitingEnd && (computeTimelineSlots[index] == 2 || computeTimelineSlots[index] == 4)) {
                // "end" slot
                if (computeTimelineSlots[index] == 2) {
                    computeLineSegments.add(new Vector2f(startPos, index + 0.5f));
                    startPos = 0f;
                    waitingStart = true;
                    waitingEnd = false;
                    index++;
                    continue;

                // "end/start" slot
                } else if (computeTimelineSlots[index] == 4) {
                    computeLineSegments.add(new Vector2f(startPos, index + 0.25f));
                    startPos = index + 0.75f;
                    waitingStart = false;
                    waitingEnd = true;
                    if (++index >= 11) {
                        computeLineSegments.add(new Vector2f(startPos, 11f));
                        break;
                    }
                    continue;
                }

            } else if (waitingEnd) {
                // move to next slot until it hits something
                // flag: whether it hits anything
                boolean flag = true;
                if (index == 10) {
                    flag = false;
                } else {
                    while (computeTimelineSlots[++index] == 0) {
                        if (index == 10) {
                            flag = false;
                            break;
                        }
                    }
                }

                if (flag) {
                    // jump to the next non-empty slot
                    continue;
                } else {
                    // "end" has an index>10
                    computeLineSegments.add(new Vector2f(startPos, 11f));
                    break;
                }
            }

            index++;
        }

        // now "local" line segments are generated based on writeTimelineSlots & computeTimelineSlots
        // we need to add back some "global" line segments too

        boolean emptyTimeline = true;
        for (int i = 0; i < 11; i++) {
            if (writeTimelineSlots[i] != 0) {
                emptyTimeline = false;
                break;
            }
        }

        if (emptyTimeline) {
            for (MeshletGpuTimeline.TimeSpan span : writeTimeline) {
                if (span.start() < startIndex && span.end() > startIndex + 10) {
                    writeLineSegments.add(new Vector2f(0f, 11f));
                    break;
                }
            }
        }

        emptyTimeline = true;
        for (int i = 0; i < 11; i++) {
            if (computeTimelineSlots[i] != 0) {
                emptyTimeline = false;
                break;
            }
        }

        if (emptyTimeline) {
            for (MeshletGpuTimeline.TimeSpan span : computeTimeline) {
                if (span.start() < startIndex && span.end() > startIndex + 10) {
                    computeLineSegments.add(new Vector2f(0f, 11f));
                    break;
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void genTimelineSlots(
            int startIndex, /* input */
            List<MeshletGpuTimeline.TimeSpan> writeTimeline, /* input */
            List<MeshletGpuTimeline.TimeSpan> computeTimeline, /* input */
            int[] writeTimelineSlots, /* output */
            int[] computeTimelineSlots /* output */) {

        for (MeshletGpuTimeline.TimeSpan span : writeTimeline) {
            if (span.start() == span.end()) {
                if (span.start() >= startIndex && span.start() <= startIndex + 10) {
                    writeTimelineSlots[span.start() - startIndex] = 3;
                }
            } else {
                if (span.start() >= startIndex && span.start() <= startIndex + 10) {
                    if (writeTimelineSlots[span.start() - startIndex] == 2) {
                        writeTimelineSlots[span.start() - startIndex] = 4;
                    } else {
                        writeTimelineSlots[span.start() - startIndex] = 1;
                    }
                }
                if (span.end() >= startIndex && span.end() <= startIndex + 10) {
                    writeTimelineSlots[span.end() - startIndex] = 2;
                }
            }
        }

        for (MeshletGpuTimeline.TimeSpan span : computeTimeline) {
            if (span.start() == span.end()) {
                if (span.start() >= startIndex && span.start() <= startIndex + 10) {
                    computeTimelineSlots[span.start() - startIndex] = 3;
                }
            } else {
                if (span.start() >= startIndex && span.start() <= startIndex + 10) {
                    if (computeTimelineSlots[span.start() - startIndex] == 2) {
                        computeTimelineSlots[span.start() - startIndex] = 4;
                    } else {
                        computeTimelineSlots[span.start() - startIndex] = 1;
                    }
                }
                if (span.end() >= startIndex && span.end() <= startIndex + 10) {
                    computeTimelineSlots[span.end() - startIndex] = 2;
                }
            }
        }
    }

    private static void drawTimeline(
            HUDContext hud,
            float x, float y, int startIndex,
            List<MeshletGpuTimeline.TimeSpan> writeTimeline,
            List<MeshletGpuTimeline.TimeSpan> computeTimeline,
            boolean[] meshletUpdates,
            List<MeshletGpuTimeline.State>[] frameStateFlows) {

        // panel width = 110, panel height = 35 + 6 * 7 = 77

        for (int i = 0; i < 11; i++) {
            writeTimelineSlots[i] = 0;
            computeTimelineSlots[i] = 0;
        }
        writeLineSegments.clear();
        computeLineSegments.clear();

        genTimelineSlots(
                startIndex,
                writeTimeline,
                computeTimeline,
                writeTimelineSlots,
                computeTimelineSlots);

        genLineSegments(
                startIndex,
                writeTimeline,
                computeTimeline,
                writeTimelineSlots,
                computeTimelineSlots,
                writeLineSegments,
                computeLineSegments);

        for (int i = 0; i < 11; i++) {
            if (writeTimelineSlots[i] == 0) {
                hud.drawRect(x + 10 * i, y, 10, 10, EMPTY_SLOT_COLOR.getRGB());
            } else if (writeTimelineSlots[i] == 1) {
                hud.drawRect(x + 10 * i, y, 10, 10, START_SLOT_COLOR.getRGB());
            } else if (writeTimelineSlots[i] == 2) {
                hud.drawRect(x + 10 * i, y, 10, 10, END_SLOT_COLOR.getRGB());
            } else if (writeTimelineSlots[i] == 3) {
                hud.drawRect(x + 10 * i, y, 5, 10, START_SLOT_COLOR.getRGB());
                hud.drawRect(x + 10 * i + 5, y, 5, 10, END_SLOT_COLOR.getRGB());
            } else if (writeTimelineSlots[i] == 4) {
                hud.drawRect(x + 10 * i, y, 5, 10, END_SLOT_COLOR.getRGB());
                hud.drawRect(x + 10 * i + 5, y, 5, 10, START_SLOT_COLOR.getRGB());
            }
        }

        for (int i = 0; i < 11; i++) {
            if (computeTimelineSlots[i] == 0) {
                hud.drawRect(x + 10 * i, y + 10, 10, 10, EMPTY_SLOT_COLOR.getRGB());
            } else if (computeTimelineSlots[i] == 1) {
                hud.drawRect(x + 10 * i, y + 10, 10, 10, START_SLOT_COLOR.getRGB());
            } else if (computeTimelineSlots[i] == 2) {
                hud.drawRect(x + 10 * i, y + 10, 10, 10, END_SLOT_COLOR.getRGB());
            } else if (computeTimelineSlots[i] == 3) {
                hud.drawRect(x + 10 * i, y + 10, 5, 10, START_SLOT_COLOR.getRGB());
                hud.drawRect(x + 10 * i + 5, y + 10, 5, 10, END_SLOT_COLOR.getRGB());
            } else if (computeTimelineSlots[i] == 4) {
                hud.drawRect(x + 10 * i, y + 10, 5, 10, END_SLOT_COLOR.getRGB());
                hud.drawRect(x + 10 * i + 5, y + 10, 5, 10, START_SLOT_COLOR.getRGB());
            }
        }

        hud.drawRect(x, y, 110, 1, Color.GRAY.getRGB());
        hud.drawRect(x, y + 10, 110, 1, Color.GRAY.getRGB());
        hud.drawRect(x, y + 20, 110, 1, Color.GRAY.getRGB());
        hud.drawRect(x, y, 1, 20, Color.GRAY.getRGB());
        hud.drawRect(x + 110, y, 1, 21, Color.GRAY.getRGB());

        for (int i = 0; i < 10; i++) {
            hud.drawRect(x + 10 * (i + 1), y, 1, 20, Color.GRAY.getRGB());
        }

        for (int i = 0; i < 11; i++) {
            if (meshletUpdates[startIndex + i]) {
                hud.drawRect(x + 10 * i + 2, y + 21, 7, 7, Color.RED.getRGB());
            }
        }

        for (int i = 0; i < 11; i++) {
            if (frameStateFlows[startIndex + i] != null) {
                List<MeshletGpuTimeline.State> list = frameStateFlows[startIndex + i];
                for (int j = 0; j < list.size(); j++) {
                    hud.drawRect(x + 10 * i + 2, y + 30 + 7 * j, 7, 7, list.get(j).color.getRGB());
                }
            }
        }

        hud.drawText(Integer.toString(startIndex), x, y + 22, Color.WHITE.getRGB());
        hud.drawText(Integer.toString(startIndex + 10), x + 101, y + 22, Color.WHITE.getRGB());

        for (Vector2f line : writeLineSegments) {
            hud.drawRect(x + line.x * 10, y + 5.25f, (line.y - line.x) * 10, 0.5f, Color.RED.getRGB());
            if (line.x != 0f) {
                hud.drawRect(x + line.x * 10, y + 5, 1.1f, 1.1f, Color.RED.getRGB());
            }
            if (line.y != 11f) {
                hud.drawRect(x + line.y * 10, y + 5, 1.1f, 1.1f, Color.RED.getRGB());
            }
        }

        for (Vector2f line : computeLineSegments) {
            hud.drawRect(x + line.x * 10, y + 15.25f, (line.y - line.x) * 10, 0.5f, Color.RED.getRGB());
            if (line.x != 0f) {
                hud.drawRect(x + line.x * 10, y + 15, 1.1f, 1.1f, Color.RED.getRGB());
            }
            if (line.y != 11f) {
                hud.drawRect(x + line.y * 10, y + 15, 1.1f, 1.1f, Color.RED.getRGB());
            }
        }
    }
    //</editor-fold>
}
