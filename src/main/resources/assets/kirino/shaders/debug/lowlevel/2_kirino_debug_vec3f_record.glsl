#ifndef KIRINO_DEBUG_VEC3F_RECORD
    #define KIRINO_DEBUG_VEC3F_RECORD

    #ifndef KIRINO_DEBUG_VEC3F_RECORD_BINDING
        #define KIRINO_DEBUG_VEC3F_RECORD_BINDING {vec3f_record_binding}
    #endif

    #ifndef KIRINO_DEBUG_MAX_VEC3F_RECORD
        #define KIRINO_DEBUG_MAX_VEC3F_RECORD {max_vec3f_record}
    #endif

    layout(std430, binding = KIRINO_DEBUG_VEC3F_RECORD_BINDING) buffer KirinoDebugVec3fRecordsBuffer
    {
        vec4 KirinoDebug_vec3fRecords[];
    };

    bool KirinoDebug_writeVec3fRecord(uint index, vec3 value)
    {
        if (index >= KIRINO_DEBUG_MAX_VEC3F_RECORD) return false;

        KirinoDebug_vec3fRecords[index] = vec4(value, KirinoDebug_vec3fRecords[index].w);

        return true;
    }

    bool KirinoDebug_writeVec3fRecordMeta(uint index, vec3 value, uint meta)
    {
        if (index >= KIRINO_DEBUG_MAX_VEC3F_RECORD) return false;

        KirinoDebug_vec3fRecords[index] = vec4(value, uintBitsToFloat(meta));

        return true;
    }

    vec3 KirinoDebug_readVec3fRecord(uint index)
    {
        if (index >= KIRINO_DEBUG_MAX_VEC3F_RECORD) return KirinoDebug_invalidVec3();

        return KirinoDebug_vec3fRecords[index].xyz;
    }

    uint KirinoDebug_readVec3fRecordMeta(uint index)
    {
        if (index >= KIRINO_DEBUG_MAX_VEC3F_RECORD) return KirinoDebug_invalidUint();

        return floatBitsToUint(KirinoDebug_vec3fRecords[index].w);
    }
#endif