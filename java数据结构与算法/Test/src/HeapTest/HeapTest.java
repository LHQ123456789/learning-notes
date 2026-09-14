package HeapTest;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * MaxHeap / PriorityQueue / HeapSort 的 JUnit 测试。
 * 覆盖：基本功能、边界条件、批量建堆、大堆压力测试、堆排序正确性。
 */
public class HeapTest {

    // ==================== 1. MaxHeap 基本功能 ====================

    @Test
    public void testInsertAndExtractMax() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        int[] nums = { 5, 3, 8, 1, 9, 2, 7, 4, 6 };
        for (int n : nums) {
            heap.insert(n);
        }

        assertEquals(nums.length, heap.size());
        assertEquals(Integer.valueOf(9), heap.peek());

        // 依次取出，应为降序
        for (int expected = 9; expected >= 1; expected--) {
            assertEquals(Integer.valueOf(expected), heap.extractMax());
        }
        assertTrue(heap.isEmpty());
        assertNull(heap.extractMax());   // 空堆取值为 null
    }

    @Test
    public void testEmptyHeap() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
        assertNull(heap.peek());
        assertNull(heap.extractMax());
    }

    @Test
    public void testSingleElement() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        heap.insert(42);
        assertEquals(1, heap.size());
        assertEquals(Integer.valueOf(42), heap.peek());
        assertEquals(Integer.valueOf(42), heap.extractMax());
        assertTrue(heap.isEmpty());
    }

    @Test
    public void testDuplicateValues() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        heap.insert(7);
        heap.insert(7);
        heap.insert(7);
        assertEquals(3, heap.size());
        assertEquals(Integer.valueOf(7), heap.extractMax());
        assertEquals(Integer.valueOf(7), heap.extractMax());
        assertEquals(Integer.valueOf(7), heap.extractMax());
        assertTrue(heap.isEmpty());
    }

    /** 大量随机插入再逐个取出，验证始终维持堆序 */
    @Test
    public void testStressRandomInsert() {
        MaxHeap<Integer> heap = new MaxHeap<>();
        Random rng = new Random(1);
        Integer[] expected = new Integer[10_000];
        for (int i = 0; i < expected.length; i++) {
            expected[i] = rng.nextInt(1_000_000);
            heap.insert(expected[i]);
        }

        // 取出的序列应是非升序（降序或相等）
        Integer[] actual = new Integer[expected.length];
        for (int i = 0; i < actual.length; i++) {
            actual[i] = heap.extractMax();
        }
        Arrays.sort(expected, (a, b) -> b - a);   // 期望降序
        assertArrayEquals(expected, actual);
    }

    // ==================== 2. 批量建堆 heapify（O(n)） ====================

    @Test
    public void testHeapifyFromArray() {
        Integer[] arr = { 4, 10, 3, 5, 1, 8, 7, 2, 9, 6 };
        MaxHeap<Integer> heap = new MaxHeap<>(arr);

        // 原数组不应被修改（内部做了 copy）
        assertArrayEquals(new Integer[] { 4, 10, 3, 5, 1, 8, 7, 2, 9, 6 }, arr);

        assertEquals(arr.length, heap.size());
        assertEquals(Integer.valueOf(10), heap.peek());

        Integer[] result = new Integer[arr.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = heap.extractMax();
        }
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected, (a, b) -> b - a);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testHeapifyEmptyAndSingle() {
        MaxHeap<Integer> empty = new MaxHeap<>(new Integer[0]);
        assertTrue(empty.isEmpty());
        assertNull(empty.extractMax());

        MaxHeap<Integer> single = new MaxHeap<>(new Integer[] { 5 });
        assertEquals(Integer.valueOf(5), single.peek());
    }

    // ==================== 3. 扩容 ====================

    @Test
    public void testCapacityGrowth() {
        // 初始容量 2，插入远多于 2 个元素应触发扩容而不出错
        MaxHeap<Integer> heap = new MaxHeap<>(2);
        for (int i = 0; i < 100; i++) {
            heap.insert(i);
        }
        assertEquals(100, heap.size());
        assertEquals(Integer.valueOf(99), heap.peek());
    }

    // ==================== 4. PriorityQueue ====================

    @Test
    public void testPriorityQueueOrder() {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        int[] nums = { 30, 10, 50, 20, 40 };
        for (int n : nums) {
            pq.enqueue(n);
        }

        assertEquals(5, pq.size());
        assertEquals(Integer.valueOf(50), pq.peek());

        // 优先级最高（值最大）的先出队
        assertEquals(Integer.valueOf(50), pq.dequeue());
        assertEquals(Integer.valueOf(40), pq.dequeue());
        assertEquals(Integer.valueOf(30), pq.dequeue());
        assertEquals(Integer.valueOf(20), pq.dequeue());
        assertEquals(Integer.valueOf(10), pq.dequeue());

        assertTrue(pq.isEmpty());
        assertNull(pq.dequeue());   // 空队列返回 null
    }

    @Test
    public void testPriorityQueueEmpty() {
        PriorityQueue<String> pq = new PriorityQueue<>();
        assertTrue(pq.isEmpty());
        assertEquals(0, pq.size());
        assertNull(pq.peek());
        assertNull(pq.dequeue());
    }

    // ==================== 5. HeapSort ====================

    @Test
    public void testHeapSortBasic() {
        Integer[] arr = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        HeapSort.heapSort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test
    public void testHeapSortBoundary() {
        // 空数组
        Integer[] empty = new Integer[0];
        HeapSort.heapSort(empty);
        assertEquals(0, empty.length);

        // 单元素
        Integer[] one = { 42 };
        HeapSort.heapSort(one);
        assertEquals(Integer.valueOf(42), one[0]);

        // 双元素
        Integer[] two = { 9, 3 };
        HeapSort.heapSort(two);
        assertArrayEquals(new Integer[] { 3, 9 }, two);
    }

    @Test
    public void testHeapSortSpecialCases() {
        // 已有序
        Integer[] sorted = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        HeapSort.heapSort(sorted);
        assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, sorted);

        // 完全逆序
        Integer[] reverse = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        HeapSort.heapSort(reverse);
        assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, reverse);

        // 全相等
        Integer[] equal = { 5, 5, 5, 5, 5 };
        HeapSort.heapSort(equal);
        assertArrayEquals(new Integer[] { 5, 5, 5, 5, 5 }, equal);

        // 含负数
        Integer[] neg = { -5, 3, -8, 0, 12, -1, 7 };
        Integer[] expected = Arrays.copyOf(neg, neg.length);
        Arrays.sort(expected);
        HeapSort.heapSort(neg);
        assertArrayEquals(expected, neg);
    }

    @Test
    public void testHeapSortStress() {
        Random rng = new Random(7);
        Integer[] arr = new Integer[100_000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rng.nextInt();
        }
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        HeapSort.heapSort(arr);
        assertArrayEquals(expected, arr);
    }

    /** 堆排序对相同数据应与 JDK 排序结果完全一致（交叉验证） */
    @Test
    public void testHeapSortCrossValidation() {
        Random rng = new Random(99);
        Integer[] arr = new Integer[1000];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rng.nextInt(1000);   // 制造重复值
        }
        Integer[] expected = Arrays.copyOf(arr, arr.length);
        Arrays.sort(expected);

        HeapSort.heapSort(arr);
        assertArrayEquals(expected, arr);
    }
}
