package SortingTest;

import org.junit.Test;
import org.junit.Before;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * 四大排序算法的 JUnit 测试用例。
 * 覆盖：基本功能、边界条件、大规模随机数据、稳定性验证。
 */
public class SortingTest {

    private static final int STRESS_SIZE = 10_000;   // 压力测试规模
    private Integer[] original;                        // 用于重置的参考数组

    // ---- 辅助方法 ----

    /** 断言数组有序（非降序） */
    private static <T extends Comparable<T>> void assertSorted(T[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1].compareTo(arr[i]) > 0) {
                fail("数组无序：arr[" + (i - 1) + "]=" + arr[i - 1]
                        + " > arr[" + i + "]=" + arr[i]
                        + "\n" + Arrays.toString(arr));
            }
        }
    }

    /** 生成随机数组 */
    private static Integer[] randomArray(int size) {
        Random rng = new Random(42);   // 固定种子，保证可复现
        Integer[] arr = new Integer[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rng.nextInt(size * 2);
        }
        return arr;
    }

    /** 检查排序后在值集合上是否与原数组一致（非破坏性检查） */
    private static <T extends Comparable<T>> void assertSameElements(T[] original, T[] sorted) {
        T[] copy = Arrays.copyOf(original, original.length);
        Arrays.sort(copy);
        assertArrayEquals("排序后元素集合应与 JDK 排序结果一致", copy, sorted);
    }

    // ==================== 1. 基本功能测试 ====================

    @Test
    public void testSelectionSort() {
        Integer[] arr = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        Sorting.selectionSort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test
    public void testInsertionSort() {
        Integer[] arr = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        Sorting.insertionSort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test
    public void testMergeSort() {
        Integer[] arr = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        Sorting.mergeSort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test
    public void testQuickSort() {
        Integer[] arr = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        Sorting.quickSort(arr);
        assertArrayEquals(expected, arr);
    }

    // ==================== 2. 边界条件测试 ====================

    @Test
    public void testEmptyArray() {
        Integer[] arr = new Integer[0];

        Sorting.selectionSort(arr);
        Sorting.insertionSort(arr);
        Sorting.mergeSort(arr);
        Sorting.quickSort(arr);
        // 空数组不应抛出异常
        assertEquals(0, arr.length);
    }

    @Test
    public void testSingleElement() {
        Integer[] arr1 = { 42 };
        Integer[] arr2 = { 42 };
        Integer[] arr3 = { 42 };
        Integer[] arr4 = { 42 };

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);

        assertEquals(Integer.valueOf(42), arr1[0]);
        assertEquals(Integer.valueOf(42), arr2[0]);
        assertEquals(Integer.valueOf(42), arr3[0]);
        assertEquals(Integer.valueOf(42), arr4[0]);
    }

    @Test
    public void testTwoElements() {
        // 逆序 → 有序
        Integer[] arr1 = { 9, 3 };
        Integer[] arr2 = { 9, 3 };
        Integer[] arr3 = { 9, 3 };
        Integer[] arr4 = { 9, 3 };

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);

        assertSorted(arr1);
        assertSorted(arr2);
        assertSorted(arr3);
        assertSorted(arr4);

        // 已有序 → 仍有序
        Integer[] arr5 = { 1, 5 };
        Integer[] arr6 = { 1, 5 };
        Sorting.insertionSort(arr5);
        Sorting.quickSort(arr6);
        assertSorted(arr5);
        assertSorted(arr6);

        // 相等元素
        Integer[] arr7 = { 7, 7 };
        Integer[] arr8 = { 7, 7 };
        Sorting.mergeSort(arr7);
        Sorting.quickSort(arr8);
        assertSorted(arr7);
        assertSorted(arr8);
    }

    @Test
    public void testAlreadySorted() {
        Integer[] arr1 = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        Integer[] arr2 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr3 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr4 = Arrays.copyOf(arr1, arr1.length);

        Sorting.selectionSort(arr1);   // O(n²) — 仍需全部比较
        Sorting.insertionSort(arr2);   // O(n) — 最优情况
        Sorting.mergeSort(arr3);       // O(n log n)
        Sorting.quickSort(arr4);       // O(n log n) — 三数取中防止退化

        assertSorted(arr1);
        assertSorted(arr2);
        assertSorted(arr3);
        assertSorted(arr4);
    }

    @Test
    public void testReverseOrder() {
        Integer[] arr1 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        Integer[] arr2 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr3 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr4 = Arrays.copyOf(arr1, arr1.length);

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);   // O(n²) — 最坏情况
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);       // 三数取中可避免 O(n²)

        assertSorted(arr1);
        assertSorted(arr2);
        assertSorted(arr3);
        assertSorted(arr4);
    }

    @Test
    public void testAllEqual() {
        Integer[] arr1 = { 5, 5, 5, 5, 5, 5, 5 };
        Integer[] arr2 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr3 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr4 = Arrays.copyOf(arr1, arr1.length);

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);       // 全相等时 pivot 难以均分，但三数取中能缓解

        assertSorted(arr1);
        assertSorted(arr2);
        assertSorted(arr3);
        assertSorted(arr4);
    }

    @Test
    public void testWithNegatives() {
        Integer[] arr1 = { -5, 3, -8, 0, 12, -1, 7 };
        Integer[] arr2 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr3 = Arrays.copyOf(arr1, arr1.length);
        Integer[] arr4 = Arrays.copyOf(arr1, arr1.length);

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);

        assertSorted(arr1);
        assertSorted(arr2);
        assertSorted(arr3);
        assertSorted(arr4);

        Integer[] expected = { -8, -5, -1, 0, 3, 7, 12 };
        assertArrayEquals(expected, arr1);
    }

    // ==================== 3. 压力测试（大规模随机数据） ====================

    @Test
    public void testStressSelection() {
        Integer[] arr = randomArray(STRESS_SIZE);
        Sorting.selectionSort(arr);
        assertSorted(arr);
    }

    @Test
    public void testStressInsertion() {
        Integer[] arr = randomArray(STRESS_SIZE);
        Sorting.insertionSort(arr);
        assertSorted(arr);
    }

    @Test
    public void testStressMerge() {
        Integer[] arr = randomArray(STRESS_SIZE);
        Sorting.mergeSort(arr);
        assertSorted(arr);
    }

    @Test
    public void testStressQuick() {
        Integer[] arr = randomArray(STRESS_SIZE);
        Sorting.quickSort(arr);
        assertSorted(arr);
    }

    // ==================== 4. 四种排序交叉验证 ====================

    @Test
    public void testCrossValidation() {
        // 四种排序对同样的数据应产生一致的结果
        Integer[] reference = randomArray(500);
        Integer[] expected = Arrays.copyOf(reference, reference.length);
        Arrays.sort(expected);

        Integer[] arr1 = Arrays.copyOf(reference, reference.length);
        Integer[] arr2 = Arrays.copyOf(reference, reference.length);
        Integer[] arr3 = Arrays.copyOf(reference, reference.length);
        Integer[] arr4 = Arrays.copyOf(reference, reference.length);

        Sorting.selectionSort(arr1);
        Sorting.insertionSort(arr2);
        Sorting.mergeSort(arr3);
        Sorting.quickSort(arr4);

        assertArrayEquals("Selection 结果应一致", expected, arr1);
        assertArrayEquals("Insertion 结果应一致", expected, arr2);
        assertArrayEquals("Merge 结果应一致",     expected, arr3);
        assertArrayEquals("Quick 结果应一致",     expected, arr4);
    }

    // ==================== 5. 稳定性验证 ====================

    /**
     * 包装类：携带原始下标，用于验证稳定性。
     * compareTo 仅比较 value，但 equals 比较 value+index。
     */
    private static class IndexedValue implements Comparable<IndexedValue> {
        final int value;
        final int index;       // 原始位置，仅用于验证稳定性

        IndexedValue(int value, int index) {
            this.value = value;
            this.index = index;
        }

        @Override
        public int compareTo(IndexedValue o) {
            return Integer.compare(this.value, o.value);   // 仅按值比较
        }

        @Override
        public String toString() {
            return value + "@" + index;
        }
    }

    @Test
    public void testStability() {
        // 构造数据：相同 value 的元素有不同的 index
        // [3@0, 1@1, 2@2, 3@3, 1@4, 2@5]
        IndexedValue[] arr = {
            new IndexedValue(3, 0),
            new IndexedValue(1, 1),
            new IndexedValue(2, 2),
            new IndexedValue(3, 3),
            new IndexedValue(1, 4),
            new IndexedValue(2, 5),
        };

        IndexedValue[] forInsertion = Arrays.copyOf(arr, arr.length);
        IndexedValue[] forMerge     = Arrays.copyOf(arr, arr.length);

        // Insertion Sort 和 Merge Sort 是稳定的
        Sorting.insertionSort(forInsertion);
        Sorting.mergeSort(forMerge);

        // 验证稳定性：相同 value 的元素，index 较小的应排在前面
        assertStable(forInsertion, "Insertion Sort");
        assertStable(forMerge,     "Merge Sort");

        // Selection Sort 和 Quick Sort 不是稳定的 —— 不强制要求，只做记录
        IndexedValue[] forSelection = Arrays.copyOf(arr, arr.length);
        IndexedValue[] forQuick     = Arrays.copyOf(arr, arr.length);
        Sorting.selectionSort(forSelection);
        Sorting.quickSort(forQuick);

        // 至少保证结果有序（即使不稳定）
        assertSorted(forSelection);
        assertSorted(forQuick);
    }

    /** 验证排序结果稳定：同值元素保持原有相对顺序 */
    private static void assertStable(IndexedValue[] arr, String algoName) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1].value == arr[i].value) {
                assertTrue(algoName + " 不稳定：value=" + arr[i].value
                        + " 的 index=" + arr[i - 1].index
                        + " 排到了 index=" + arr[i].index + " 前面",
                        arr[i - 1].index < arr[i].index);
            }
        }
    }
}
