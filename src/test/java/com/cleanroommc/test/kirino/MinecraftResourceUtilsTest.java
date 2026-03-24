package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MinecraftResourceUtilsTest {

    @Test
    public void testRead() {
        assertDoesNotThrow(() -> {
            MinecraftResourceUtils.readText(
                    new ResourceLocation("forge:shaders/test.vert"),
                    MinecraftResourceUtils.NewLineType.BACK_SLASH_N);
        });
    }
}
