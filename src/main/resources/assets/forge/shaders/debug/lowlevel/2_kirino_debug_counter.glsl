#ifndef KIRINO_DEBUG_COUNTER
    #define KIRINO_DEBUG_COUNTER

    #ifndef KIRINO_DEBUG_COUNTER_BINDING
        #define KIRINO_DEBUG_COUNTER_BINDING {counter_binding}
    #endif

    #ifndef KIRINO_DEBUG_MAX_COUNTER
        #define KIRINO_DEBUG_MAX_COUNTER {max_counter}
    #endif

    layout(std430, binding = KIRINO_DEBUG_COUNTER_BINDING) buffer KirinoDebugCountersBuffer
    {
        uint KirinoDebug_counters[];
    };

    uint KirinoDebug_count(uint index)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        return KirinoDebug_counters[index];
    }

    uint KirinoDebug_countClamped(uint index, uint maxCount)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        uint _index = KirinoDebug_counters[index];
        return (_index < maxCount) ? _index : maxCount;
    }

    // no limit on counter addition; no limit on return value
    uint KirinoDebug_alloc(uint index)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        return atomicAdd(KirinoDebug_counters[index], 1u);
    }

    // no limit on counter addition; return value will be clamped
    uint KirinoDebug_allocSafe(uint index, uint maxCount)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        uint _index = atomicAdd(KirinoDebug_counters[index], 1u);
        return (_index < maxCount) ? _index : KirinoDebug_invalidUint();
    }

    // no limit on counter addition; return value will be clamped
    uint KirinoDebug_allocBlockSafe(uint index, uint count, uint maxCount)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        uint base = atomicAdd(KirinoDebug_counters[index], count);
        return (base + count <= maxCount) ? base : KirinoDebug_invalidUint();
    }

    // counter addition will be clamped; return value will be clamped
    uint KirinoDebug_allocClampSafe(uint index, uint maxCount)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return KirinoDebug_invalidUint();

        uint old;

        do
        {
            old = KirinoDebug_counters[index];

            if (old >= maxCount) return KirinoDebug_invalidUint();
        }
        while (atomicCompSwap(KirinoDebug_counters[index], old, old + 1u) != old);

        return old;
    }

    bool KirinoDebug_resetCounterNotThreadSafe(uint index)
    {
        if (index >= KIRINO_DEBUG_MAX_COUNTER) return false;

        KirinoDebug_counters[index] = 0u;

        return true;
    }
#endif