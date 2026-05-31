package com.cleanroommc.kirino.ui.simpletext;

public record ST_Config(
        ST_FontBackendType target,
        int pixelSize,
        int sdfPadding,
        int sdfSpread,
        int payload) {
}
