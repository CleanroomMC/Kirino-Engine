package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingSchedule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PostProcessingScheduleTest {

    @Test
    public void testBuilder1() {
        PostProcessingSchedule.Builder builder = PostProcessingSchedule.builder();
        var a = builder.addPass("A");
        var b = builder.addPass("B");
        var c = builder.addPass("C");
        PostProcessingSchedule schedule = builder
                .before(c, a)
                .build();

        assertEquals(3, schedule.getSubpassCount());
        assertEquals("B", schedule.getSubpass(0).getRegisteredName());
        assertEquals("C", schedule.getSubpass(1).getRegisteredName());
        assertEquals("A", schedule.getSubpass(2).getRegisteredName());
    }

    @Test
    public void testBuilder2() {
        PostProcessingSchedule.Builder builder = PostProcessingSchedule.builder();
        var a = builder.addPass("A", 100);
        var b = builder.addPass("B");
        var c = builder.addPass("C");
        var d = builder.addPass("D", 90);
        PostProcessingSchedule schedule = builder
                .before(c, a)
                .build();

        assertEquals(4, schedule.getSubpassCount());
        assertEquals("B", schedule.getSubpass(0).getRegisteredName());
        assertEquals("C", schedule.getSubpass(1).getRegisteredName());
        assertEquals("D", schedule.getSubpass(2).getRegisteredName());
        assertEquals("A", schedule.getSubpass(3).getRegisteredName());
    }

    @Test
    public void testBuilder3() {
        PostProcessingSchedule.Builder builder = PostProcessingSchedule.builder();
        var a = builder.addPass("A");
        var b = builder.addPass("B");
        var c = builder.addPass("C");
        builder.before(a, b).before(b, c).before(c, a);

        assertThrows(IllegalStateException.class, builder::build);
    }
}
