package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Comparator;

public final class MedianUtils {

    private MedianUtils() {
    }

    public enum MedianPolicy {
        LOWER,
        UPPER,
        AVG
    }

    /**
     * It returns the median <i>(returns the mean of upper lower median when the count is even)</i>,
     * and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(int @NonNull [] array) {
        Preconditions.checkNotNull(array);

        if (array.length == 0) {
            return 0f;
        }
        if (array.length == 1) {
            return (float) array[0];
        }

        Integer[] arr = new Integer[array.length];
        for (int i  = 0; i < arr.length; i++) {
            arr[i] = array[i];
        }
        return median(arr);
    }

    /**
     * It returns the median <i>(returns the mean of upper lower median when the count is even)</i>,
     * and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(@NonNull Integer @NonNull [] array) {
        Preconditions.checkNotNull(array);
        for (Integer i : array) {
            Preconditions.checkNotNull(i);
        }

        int len = array.length;
        if (len == 0) {
            return 0f;
        }
        if (len == 1) {
            return (float) array[0];
        }

        if (len % 2 != 0) {
            return array[MedianUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)];
        } else {
            return (float) (array[MedianUtils.<Integer>select(array, 0, array.length - 1, (array.length >> 1) - 1, Integer::compareTo)] +
                    array[MedianUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)])
                    / 2f;
        }
    }

    /**
     * It returns the median, and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(int @NonNull [] array, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(policy);

        if (array.length == 0) {
            return 0f;
        }
        if (array.length == 1) {
            return (float) array[0];
        }

        Integer[] arr = new Integer[array.length];
        for (int i  = 0; i < arr.length; i++) {
            arr[i] = array[i];
        }
        return median(arr, policy);
    }

    /**
     * It returns the median, and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(@NonNull Integer @NonNull [] array, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        for (Integer i : array) {
            Preconditions.checkNotNull(i);
        }
        Preconditions.checkNotNull(policy);

        int len = array.length;
        if (len == 0) {
            return 0f;
        }
        if (len == 1) {
            return (float) array[0];
        }

        if (len % 2 != 0) {
            return array[MedianUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)];
        } else if (policy == MedianPolicy.UPPER || policy == MedianPolicy.LOWER) {
            int index = switch (policy) {
                case LOWER -> (array.length >> 1) - 1;
                case UPPER -> array.length >> 1;
                default -> -1;
            };
            return array[MedianUtils.<Integer>select(array, 0, array.length - 1, index, Integer::compareTo)];
        } else {
            return (float) (array[MedianUtils.<Integer>select(array, 0, array.length - 1, (array.length >> 1) - 1, Integer::compareTo)] +
                    array[MedianUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)])
                    / 2f;
        }
    }

    /**
     * It returns the median <i>(returns the mean of upper lower median when the count is even)</i>,
     * and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(float @NonNull [] array) {
        Preconditions.checkNotNull(array);

        if (array.length == 0) {
            return 0f;
        }
        if (array.length == 1) {
            return array[0];
        }

        Float[] arr = new Float[array.length];
        for (int i  = 0; i < arr.length; i++) {
            arr[i] = array[i];
        }
        return median(arr);
    }

    /**
     * It returns the median <i>(returns the mean of upper lower median when the count is even)</i>,
     * and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(@NonNull Float @NonNull [] array) {
        Preconditions.checkNotNull(array);
        for (Float f : array) {
            Preconditions.checkNotNull(f);
        }

        int len = array.length;
        if (len == 0) {
            return 0f;
        }
        if (len == 1) {
            return array[0];
        }

        if (len % 2 != 0) {
            return array[MedianUtils.<Float>select(array, 0, array.length - 1, array.length >> 1, Float::compareTo)];
        } else {
            return (array[MedianUtils.<Float>select(array, 0, array.length - 1, (array.length >> 1) - 1, Float::compareTo)] +
                    array[MedianUtils.<Float>select(array, 0, array.length - 1, array.length >> 1, Float::compareTo)])
                    / 2f;
        }
    }

    /**
     * It returns the median, and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(float @NonNull [] array, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(policy);

        if (array.length == 0) {
            return 0f;
        }
        if (array.length == 1) {
            return array[0];
        }

        Float[] arr = new Float[array.length];
        for (int i  = 0; i < arr.length; i++) {
            arr[i] = array[i];
        }
        return median(arr, policy);
    }

    /**
     * It returns the median, and returns <code>0</code> when the count is <code>0</code>.
     *
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Can be empty
     */
    public static float median(@NonNull Float @NonNull [] array, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        for (Float f : array) {
            Preconditions.checkNotNull(f);
        }
        Preconditions.checkNotNull(policy);

        int len = array.length;
        if (len == 0) {
            return 0f;
        }
        if (len == 1) {
            return array[0];
        }

        if (len % 2 != 0) {
            return array[MedianUtils.<Float>select(array, 0, array.length - 1, array.length >> 1, Float::compareTo)];
        } else if (policy == MedianPolicy.UPPER || policy == MedianPolicy.LOWER) {
            int index = switch (policy) {
                case LOWER -> (array.length >> 1) - 1;
                case UPPER -> array.length >> 1;
                default -> -1;
            };
            return array[MedianUtils.<Float>select(array, 0, array.length - 1, index, Float::compareTo)];
        } else {
            return (array[MedianUtils.<Float>select(array, 0, array.length - 1, (array.length >> 1) - 1, Float::compareTo)] +
                    array[MedianUtils.<Float>select(array, 0, array.length - 1, array.length >> 1, Float::compareTo)])
                    / 2f;
        }
    }

    /**
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Must not be empty
     * @param policy Must not be {@link MedianPolicy#AVG}
     */
    public static <T extends Comparable<T>> T median(@NonNull T @NonNull [] array, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        for (T t : array) {
            Preconditions.checkNotNull(t);
        }
        Preconditions.checkNotNull(policy);
        Preconditions.checkArgument(array.length > 0,
                "Argument \"array\" must not be empty.");
        Preconditions.checkArgument(policy != MedianPolicy.AVG,
                "Argument \"policy\" must not be AVG.");

        if (array.length == 1) {
            return array[0];
        }

        int index;
        if ((array.length & 1) != 0) {
            index = array.length >> 1;
        } else {
            index = switch (policy) {
                case LOWER -> (array.length >> 1) - 1;
                case UPPER -> array.length >> 1;
                default -> -1;
            };
        }

        return array[select(array, 0, array.length - 1, index, T::compareTo)];
    }

    /**
     * <p>Note: It'll re-order the input array!</p>
     *
     * @param array Must not be empty
     * @param policy Must not be {@link MedianPolicy#AVG}
     */
    public static <T> T median(@NonNull T @NonNull [] array, @NonNull Comparator<T> comparator, @NonNull MedianPolicy policy) {
        Preconditions.checkNotNull(array);
        for (T t : array) {
            Preconditions.checkNotNull(t);
        }
        Preconditions.checkNotNull(policy);
        Preconditions.checkNotNull(comparator);
        Preconditions.checkArgument(array.length > 0,
                "Argument \"array\" must not be empty.");
        Preconditions.checkArgument(policy != MedianPolicy.AVG,
                "Argument \"policy\" must not be AVG.");

        if (array.length == 1) {
            return array[0];
        }

        int index;
        if ((array.length & 1) != 0) {
            index = array.length >> 1;
        } else {
            index = switch (policy) {
                case LOWER -> (array.length >> 1) - 1;
                case UPPER -> array.length >> 1;
                default -> -1;
            };
        }

        return array[select(array, 0, array.length - 1, index, comparator)];
    }

    private static <T> int select(@NonNull T @NonNull [] array,
                                  int left, int right, int n, Comparator<T> comparator) {

        Preconditions.checkState(right > left);
        Preconditions.checkElementIndex(left, array.length);
        Preconditions.checkElementIndex(right, array.length);
        Preconditions.checkPositionIndex(n, right);

        while (true) {
            if (left == right) {
                return left;
            }

            int pivotIdx = pivot(array, left, right, comparator);
            pivotIdx = partition(array, left, right, pivotIdx, n, comparator);
            if (pivotIdx == n) {
                return n;
            } else if (n < pivotIdx) {
                right = pivotIdx - 1;
            } else {
                left = pivotIdx + 1;
            }
        }
    }

    private static <T> int pivot(@NonNull T @NonNull [] array,
                                 int left, int right, Comparator<T> comparator) {

        Preconditions.checkState(right > left);
        Preconditions.checkElementIndex(left, array.length);
        Preconditions.checkElementIndex(right, array.length);

        if (right - left < 5) {
            return partition5(array, left, right, comparator);
        }

        for (int i = left; i <= right; i += 5) {
            int subRight = i + 4;
            if (subRight > right) {
                subRight = right;
            }
            int median5 = partition5(array, i, subRight, comparator);
            swap(array, median5, left + Math.floorDiv(i - left, 5));
        }

        int mid = Math.floorDiv(right - left, 10) + left;
        return select(array, left, left + Math.floorDiv(right - left, 5), mid, comparator);
    }

    private static <T> int partition(@NonNull T @NonNull [] array,
                                     int left, int right,
                                     int pivotIdx, int n, Comparator<T> comparator) {

        Preconditions.checkState(right > left);
        Preconditions.checkElementIndex(left, array.length);
        Preconditions.checkElementIndex(right, array.length);

        T pivotValue = array[pivotIdx];
        swap(array, pivotIdx, right);
        int storeIdx = left;

        for (int i = left; i < right; i++) {
            if (comparator.compare(array[i], pivotValue) < 0) {
                swap(array, storeIdx, i);
                storeIdx++;
            }
        }

        int storeIdxEq = storeIdx;

        for (int j = storeIdx; j < right; j++) {
            if (comparator.compare(array[j], pivotValue) == 0) {
                swap(array, storeIdxEq, j);
                storeIdxEq++;
            }
        }

        swap(array, right, storeIdxEq);

        if (n < storeIdx) {
            return storeIdx;
        }
        if (n <= storeIdxEq) {
            return n;
        }
        return storeIdx;
    }

    private static <T> int partition5(@NonNull T @NonNull [] array,
                                      int left, int right, Comparator<T> comparator) {

        Preconditions.checkState(right >= left, "%s is less than %s.", right, left);
        Preconditions.checkElementIndex(left, array.length);
        Preconditions.checkElementIndex(right, array.length);

        switch (right-left) {
            case 0:
                break;
            case 1:
                if (comparator.compare(array[left],array[right]) > right) {
                    swap(array, left, right);
                }
                break;
            case 2:
                if (comparator.compare(array[left],array[right-1]) <= 0) {
                    if (comparator.compare(array[left+1],array[right]) > 0) {
                        if (comparator.compare(array[left],array[right]) < 0) {
                            swap(array, left+1, right);
                        } else {
                            T tmp = array[left];
                            array[left] = array[right];
                            array[right] = array[left+1];
                            array[left+1] = tmp;
                        }
                    }
                } else {
                    if (comparator.compare(array[left+1],array[right]) < 0) {
                        if (comparator.compare(array[left],array[right]) < 0) {
                            swap(array, left, right-1);
                        } else {
                            T tmp = array[left];
                            array[left] = array[left+1];
                            array[left+1] = array[right];
                            array[right] = tmp;
                        }
                    } else {
                        swap(array, left, right);
                    }
                }
                break;
            case 3:
            case 4:
                Arrays.sort(array, left, right+1, comparator);
                break;
        }

        return left + ((right - left) >>> 1);
    }

    private static <T> void swap(@NonNull T @NonNull [] array, int left, int right) {
        Preconditions.checkElementIndex(left, array.length);
        Preconditions.checkElementIndex(right, array.length);

        T tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }
}
