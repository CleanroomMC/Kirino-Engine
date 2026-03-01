package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GLTestExtension.class)
public class VBOBufferUploadHintTest {

    @Test
    void testBufferUploadHint() {
        GLTestExtension.submit(() -> {
            VBOView view = new VBOView(new GLBuffer());
            view.bind();
            view.alloc(1, BufferUploadHint.DYNAMIC_DRAW);
            assertEquals(BufferUploadHint.DYNAMIC_DRAW, view.fetchBufferUploadHint());
        }).join();
    }
}
