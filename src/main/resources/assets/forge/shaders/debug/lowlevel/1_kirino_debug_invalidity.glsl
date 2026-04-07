#ifndef KIRINO_DEBUG_INVALIDITY
    #define KIRINO_DEBUG_INVALIDITY

    uint KirinoDebug_invalidUint()
    {
        return 0xffffffffu;
    }

    bool KirinoDebug_validUint(uint value)
    {
        return value != 0xffffffffu;
    }

    float KirinoDebug_invalidFloat()
    {
        return uintBitsToFloat(0x7fc00000u);
    }

    bool KirinoDebug_validFloat(float value)
    {
        return isnan(value);
    }

    vec3 KirinoDebug_invalidVec3()
    {
        return vec3(KirinoDebug_invalidFloat());
    }

    bool KirinoDebug_validVec3(vec3 value)
    {
        return all(isnan(value));
    }
#endif