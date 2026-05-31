package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.utils.MedianUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MedianUtilsTest {

    @Test
    public void testIntSizeOdd() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new ArrayList<>(IntStream.range(1, 4096).boxed().toList());
        float median = (float) list.get((list.size() / 2));
        Collections.shuffle(list, rng);
        assertEquals(median, MedianUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testIntSizeEven() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new ArrayList<>(IntStream.range(1, 4097).boxed().toList());
        float median = (float) (list.get((list.size() / 2) - 1) + list.get((list.size() / 2))) / 2;
        Collections.shuffle(list, rng);
        assertEquals(median, MedianUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testMedianStrings() {
        String[] arr = {"6", "3", "5", "2", "4", "1", "7", "9", "8"};
        assertEquals("5", MedianUtils.median(arr, MedianUtils.MedianPolicy.LOWER));
    }

    @Test
    public void testLength1() {
        assertEquals(1, MedianUtils.median(new int[]{1}));
        assertEquals(1, MedianUtils.median(new Integer[]{1}));
        assertEquals(1, MedianUtils.median(new float[]{1}));
        assertEquals(1, MedianUtils.median(new Float[]{1f}));
    }

    @Test
    public void testEmpty() {
        assertEquals(0, MedianUtils.median(new int[0]));
        assertEquals(0, MedianUtils.median(new Integer[0]));
        assertEquals(0, MedianUtils.median(new float[0]));
        assertEquals(0, MedianUtils.median(new Float[0]));
    }

    @Test
    public void testUpperMedian() {
        assertEquals(2, MedianUtils.median(new int[]{1, 2}, MedianUtils.MedianPolicy.UPPER));
        assertEquals(2, MedianUtils.median(new Integer[]{1, 2}, MedianUtils.MedianPolicy.UPPER));
        assertEquals(2, MedianUtils.median(new float[]{1, 2}, MedianUtils.MedianPolicy.UPPER));
        assertEquals(2, MedianUtils.median(new Float[]{1f, 2f}, MedianUtils.MedianPolicy.UPPER));
    }

    @Test
    public void testLowerMedian() {
        assertEquals(1, MedianUtils.median(new int[]{1, 2}, MedianUtils.MedianPolicy.LOWER));
        assertEquals(1, MedianUtils.median(new Integer[]{1, 2}, MedianUtils.MedianPolicy.LOWER));
        assertEquals(1, MedianUtils.median(new float[]{1, 2}, MedianUtils.MedianPolicy.LOWER));
        assertEquals(1, MedianUtils.median(new Float[]{1f, 2f}, MedianUtils.MedianPolicy.LOWER));
    }

    @Test
    public void testAvgMedian() {
        assertEquals(1.5f, MedianUtils.median(new int[]{1, 2}, MedianUtils.MedianPolicy.AVG));
        assertEquals(1.5f, MedianUtils.median(new Integer[]{1, 2}, MedianUtils.MedianPolicy.AVG));
        assertEquals(1.5f, MedianUtils.median(new float[]{1, 2}, MedianUtils.MedianPolicy.AVG));
        assertEquals(1.5f, MedianUtils.median(new Float[]{1f, 2f}, MedianUtils.MedianPolicy.AVG));
        assertEquals(1.5f, MedianUtils.median(new int[]{1, 2}));
        assertEquals(1.5f, MedianUtils.median(new Integer[]{1, 2}));
        assertEquals(1.5f, MedianUtils.median(new float[]{1, 2}));
        assertEquals(1.5f, MedianUtils.median(new Float[]{1f, 2f}));
    }
}
