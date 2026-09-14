package HeapTest;

import java.util.Arrays;

/**
 * 手写堆排序（原地，不借助额外数组）。
 *
 * <p>整体思路分两步，全程在原数组上操作：</p>
 * <ol>
 *   <li><b>建堆</b>：把数组原地调整成最大堆（复用 {@link MaxHeap} 的 O(n) heapify 思想）；</li>
 *   <li><b>逐个取出最大值</b>：堆顶是当前区间最大值，把它与区间末尾元素交换，
 *       然后让"区间长度 - 1"并对新的堆顶下沉。反复 n-1 次后，数组即<b>升序</b>。</li>
 * </ol>
 *
 * <pre>
 * 例：原始 [4, 10, 3, 5, 1]
 *   ① 建最大堆 → [10, 5, 3, 4, 1]
 *   ② 堆顶 10 与末尾 1 交换 → [1, 5, 3, 4, 10]，区间缩为前 4 个，下沉
 *   ③ 堆顶 5  与末尾 4 交换 → [4, 1, 3, 5, 10]，区间缩为前 3 个，下沉
 *   ④ … 依此类推，最终得到 [1, 3, 4, 5, 10]
 * </pre>
 *
 * <p><b>复杂度</b>：建堆 O(n) + n-1 次下沉 O(n log n) = <b>O(n log n)</b>，
 * 且是原地排序（空间 O(1)，忽略递归栈）。<b>不稳定</b>（相同元素可能被交换打乱顺序）。</p>
 */
public class HeapSort {

    /**
     * 对数组原地堆排序（升序）。
     *
     * @param arr 待排序数组
     */
    public static <T extends Comparable<T>> void heapSort(T[] arr) {
        int n = arr.length;

        // ---- 第一步：原地建最大堆 O(n) ----
        // 从最后一个非叶子节点开始，自底向上逐个下沉，理由同 MaxHeap.heapify
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(arr, i, n);
        }

        // ---- 第二步：反复把堆顶（最大值）换到当前区间末尾 ----
        // 区间 [0, end) 是"还在堆里"的部分，[end, n) 是已排好序的升序尾部
        for (int end = n - 1; end > 0; end--) {
            swap(arr, 0, end);          // 最大值归位到末尾
            siftDown(arr, 0, end);      // 对缩小的堆 [0, end) 重新下沉堆顶
        }
    }

    /**
     * 下沉：让 arr[k] 在长度为 len 的堆区间 [0, len) 内沉到正确位置。
     * 与 {@link MaxHeap#extractMax} 中的下沉逻辑一致，只是作用于"任意数组的任意区间"。
     */
    private static <T extends Comparable<T>> void siftDown(T[] arr, int k, int len) {
        while (leftChild(k) < len) {
            int left = leftChild(k);
            int right = rightChild(k);
            int larger = left;
            if (right < len && arr[right].compareTo(arr[left]) > 0) {
                larger = right;
            }
            if (arr[k].compareTo(arr[larger]) >= 0) {
                break;
            }
            swap(arr, k, larger);
            k = larger;
        }
    }

    private static int leftChild(int i) {
        return 2 * i + 1;
    }

    private static int rightChild(int i) {
        return 2 * i + 2;
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ======================== 主方法（演示用） ========================

    public static void main(String[] args) {
        Integer[] arr = { 4, 10, 3, 5, 1, 8, 7, 2, 9, 6 };
        System.out.println("原始数组：" + Arrays.toString(arr));
        heapSort(arr);
        System.out.println("堆排序后：" + Arrays.toString(arr));
    }
}
