package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

final class FSMPreconditions {

    private FSMPreconditions() {
    }

    static int rangeSize(int lowerBound, int upperBound) {
        long size = (long) upperBound - lowerBound + 1;

        Preconditions.checkArgument(lowerBound <= upperBound,
                "Argument \"lowerBound\"=%s must not exceed \"upperBound\"=%s.",
                lowerBound, upperBound);
        Preconditions.checkArgument(size <= Integer.MAX_VALUE,
                "Range from \"lowerBound\"=%s to \"upperBound\"=%s is too large for an array-backed FSM.",
                lowerBound, upperBound);

        return (int) size;
    }

    static int transitionCount(int stateCount, int inputCount) {
        long size = (long) stateCount * inputCount;

        Preconditions.checkArgument(size <= Integer.MAX_VALUE,
                "Transition table for \"stateCount\"=%s and \"inputCount\"=%s is too large.",
                stateCount, inputCount);

        return (int) size;
    }

    static <E extends Enum<E>> E @NonNull [] enumConstants(@NonNull Class<E> enumClass) {
        Preconditions.checkNotNull(enumClass);
        Preconditions.checkArgument(enumClass.isEnum(),
                "Argument \"enumClass\"=%s must be an enum class.",
                enumClass);

        return enumClass.getEnumConstants();
    }
}
