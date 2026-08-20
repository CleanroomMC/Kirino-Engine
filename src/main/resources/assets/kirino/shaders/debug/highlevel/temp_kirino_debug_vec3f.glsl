uint KirinoDebug_beginVec3Stream()
{
    return KirinoDebug_allocClampSafe(0u, 0xFFFFFFFFu);
}

int KirinoDebug_putVec3(uint header, vec3 data)
{
    uint slot = KirinoDebug_allocClampSafe(1u, KIRINO_DEBUG_MAX_VEC3F_RECORD);

    if (!KirinoDebug_validUint(slot)) return -1;
    if (!KirinoDebug_writeVec3fRecordMeta(slot, data, header)) return -2;

    return int(slot);
}