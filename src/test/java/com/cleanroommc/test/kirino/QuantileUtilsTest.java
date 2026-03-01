package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.utils.QuantileUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuantileUtilsTest {

    @Test
    public void testIntSizeOdd() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new java.util.ArrayList<>(IntStream.range(1, 4096).boxed().toList());
        float median = (float) list.get((list.size() / 2));
        Collections.shuffle(list, rng);
        assertEquals(median, QuantileUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testIntSizeEven() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new java.util.ArrayList<>(IntStream.range(1, 4097).boxed().toList());
        float median = (float) (list.get((list.size() / 2) - 1) + list.get((list.size() / 2))) / 2;
        Collections.shuffle(list, rng);
        assertEquals(median, QuantileUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testMedianStrings() {
        String[] arr = {"6", "3", "5", "2", "4", "1", "7", "9", "8"};
        assertEquals("5", QuantileUtils.median(arr));
    }
}
