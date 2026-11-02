package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;

public enum TextureDimension {
    D1,
    D2,
    D3,
    CUBE,
    RECTANGLE,
    BUFFER;

    void validate(boolean arrayed, boolean multisample) {
        Preconditions.checkArgument(!arrayed || this == D1 || this == D2 || this == CUBE,
                "Texture dimension \"%s\" does not support arrays.", this);
        Preconditions.checkArgument(!multisample || this == D2,
                "Texture dimension \"%s\" does not support multisampling.", this);
    }
}
