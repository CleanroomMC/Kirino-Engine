package com.cleanroommc.kirino.gl.debug;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.nio.IntBuffer;
import java.util.List;

public final class KHRDebug {

    private static Logger logger;
    private static GLDebugMessageCallback callback;

    private static boolean enable = false;

    public static boolean isEnable() {
        return enable;
    }

    /**
     * <p>Note: <b>It must never be called by clients!</b></p>
     */
    public static void enable(@NonNull Logger logger, @NonNull List<@NonNull DebugMessageFilter> messageFilters) {
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(messageFilters);
        for (DebugMessageFilter filter : messageFilters) {
            Preconditions.checkNotNull(filter);
        }

        if (enable) {
            return;
        }

        KHRDebug.logger = logger;

        callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
            String msg = GLDebugMessageCallback.getMessage(length, message);
            logMessage(source, type, id, severity, userParam, msg);
        });

        GL43C.glDebugMessageCallback(callback, 0);

        // disable all
        GL43.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, (IntBuffer) null, false);

        for (DebugMessageFilter filter : messageFilters) {
            GL43.glDebugMessageControl(
                    filter.getSource().glValue,
                    filter.getType().glValue,
                    filter.getSeverity().glValue,
                    (IntBuffer) null,
                    true);
        }

        GL11.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        enable = true;
    }

    /**
     * <p>Note: <b>It must never be called by clients!</b></p>
     */
    public static void disable() {
        if (!enable) {
            return;
        }

        GL11.glDisable(GL43.GL_DEBUG_OUTPUT);
        GL11.glDisable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        GL43C.glDebugMessageCallback(null, 0);

        if (callback != null) {
            callback.free();
            callback = null;
        }

        enable = false;
    }

    private static void logMessage(int source, int type, int id, int severity, long userParam, String message) {
        DebugMsgSource msgSource = DebugMsgSource.parse(source);
        DebugMsgType msgType = DebugMsgType.parse(type);
        DebugMsgSeverity msgSeverity = DebugMsgSeverity.parse(severity);

        StringBuilder builder = new StringBuilder(256);

        builder.append("OpenGL Debug: ")
                .append("(Source=").append(msgSource)
                .append(", Type=").append(msgType)
                .append(", Severity=").append(msgSeverity)
                .append(", ID=").append(id)
                .append(") ")
                .append(message);

        appendCallStack(builder);

        logger.warn(builder.toString());
    }

    private static void appendCallStack(StringBuilder builder) {
        builder.append("\nCall stack:");

        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement frame : stack) {
            String className = frame.getClassName();
            if (className.equals(Thread.class.getName()) || className.equals(KHRDebug.class.getName())) {
                continue;
            }

            builder.append("\n  ")
                    .append(className)
                    .append('#')
                    .append(frame.getMethodName());

            if (frame.getFileName() != null) {
                builder.append(" [").append(frame.getFileName());
                if (frame.getLineNumber() >= 0) {
                    builder.append(':').append(frame.getLineNumber());
                }
                builder.append(']');
            }
        }
    }

    public static void pushGroup(@NonNull String name) {
        Preconditions.checkNotNull(name);

        GL43.glPushDebugGroup(GL43.GL_DEBUG_SOURCE_APPLICATION, 1, name);
    }

    public static void popGroup() {
        GL43.glPopDebugGroup();
    }

    public static void notify(@NonNull String arg) {
        Preconditions.checkNotNull(arg);

        GL43.glDebugMessageInsert(GL43.GL_DEBUG_SOURCE_APPLICATION, GL43.GL_DEBUG_TYPE_MARKER, 1, GL43.GL_DEBUG_SEVERITY_NOTIFICATION, arg);
    }
}
