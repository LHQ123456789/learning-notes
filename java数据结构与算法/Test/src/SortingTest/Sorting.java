package SortingTest;

import java.util.Arrays;
import java.util.Random;


public class Sorting {

    // ======================== Selection Sort 选择排序 ========================


    public static <T extends Comparable<T>> void selectionSort(T[] arr) {
        int n = arr.length;

        // i：已排序区间的末尾，也是本轮最小值要放入的位置
        // 只需排到 n-2，因为最后一个元素自动就位
        for (int i = 0; i < n - 1; i++) {

            // 假设当前位置就是最小值
            int minIdx = i;

            // j：遍历未排序区间 [i+1, n-1]，找到真正最小值的下标
            // 比较次数：第 i 轮需要比较 (n-1-i) 次
            for (int j = i + 1; j < n; j++) {
                if (less(arr[j], arr[minIdx])) {
                    minIdx = j;                     // 记录更小元素的位置
                }
            }

            // 如果最小值不在当前位置，就交换
            // 本轮之后 arr[0..i] 就是前 i+1 个最小的元素，且已排好序
            if (minIdx != i) {
                swap(arr, i, minIdx);               // 交换次数：总共最多 n-1 次
            }
        }
    }

    // ======================== Insertion Sort 插入排序 ========================


    public static <T extends Comparable<T>> void insertionSort(T[] arr) {
        int n = arr.length;

        // i：当前要插入的"牌"的下标，从第 2 个元素（index=1）开始
        for (int i = 1; i < n; i++) {

            T key = arr[i];                         // 暂存当前元素，因为后续移位会覆盖它
            int j = i - 1;                          // 从已排序区间的末尾开始向左扫描

            // 只要前面的元素比 key 大，就把它右移一格，为 key 腾出空间
            // 使用 less(key, arr[j]) 而非 less(arr[j], key) 保证稳定性
            while (j >= 0 && less(key, arr[j])) {
                arr[j + 1] = arr[j];                // 右移：把大元素往后挪
                j--;                                // 继续向左扫描
            }

            // 此时 j 指向 <= key 的位置（或 j == -1），空位在 j+1
            arr[j + 1] = key;                       // 插入 key 到正确位置
        }
    }

    // ======================== Merge Sort 归并排序 ========================


    public static <T extends Comparable<T>> void mergeSort(T[] arr) {
        @SuppressWarnings("unchecked")
        T[] aux = (T[]) new Comparable[arr.length]; // 全局辅助数组，避免频繁创建
        mergeSort(arr, aux, 0, arr.length - 1);
    }


    private static <T extends Comparable<T>> void mergeSort(T[] arr, T[] aux, int lo, int hi) {
        // 递归终止条件：区间长度为 0 或 1 时天然有序
        if (lo >= hi) {
            return;
        }

        // 计算中点（防溢出版本，等价于 (lo + hi) / 2）
        int mid = lo + (hi - lo) / 2;

        // 递归排序左半部分 [lo, mid]
        mergeSort(arr, aux, lo, mid);

        // 递归排序右半部分 [mid+1, hi]
        mergeSort(arr, aux, mid + 1, hi);

        // 合并两个有序子数组
        merge(arr, aux, lo, mid, hi);
    }


    private static <T extends Comparable<T>> void merge(T[] arr, T[] aux, int lo, int mid, int hi) {
        // 1. 将待合并区间复制到辅助数组（使原数组可用于写回结果）
        System.arraycopy(arr, lo, aux, lo, hi - lo + 1);
        // 等价于：for (int k = lo; k <= hi; k++) aux[k] = arr[k];

        // 2. 双指针遍历：i 从左半开始，j 从右半开始
        int i = lo;          // 左半部分指针（aux 中）
        int j = mid + 1;     // 右半部分指针（aux 中）

        // k 是写回 arr 的位置，遍历整个 [lo, hi]
        for (int k = lo; k <= hi; k++) {

            if (i > mid) {                              // 左半已耗尽 → 取右半
                arr[k] = aux[j++];
            } else if (j > hi) {                        // 右半已耗尽 → 取左半
                arr[k] = aux[i++];
            } else if (less(aux[j], aux[i])) {          // 右半当前元素更小 → 取右半
                arr[k] = aux[j++];
            } else {                                    // 左半当前元素更小或相等 → 取左半
                arr[k] = aux[i++];                      //    相等取左半 → 保证稳定性
            }
        }
    }

    // ======================== Quick Sort 快速排序 ========================


    public static <T extends Comparable<T>> void quickSort(T[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    /** 小数组阈值：小于此值使用插入排序，避免递归过深 */
    private static final int INSERTION_THRESHOLD = 15;

    /**
     * 递归快排 arr[lo..hi]。
     */
    private static <T extends Comparable<T>> void quickSort(T[] arr, int lo, int hi) {
        if (lo >= hi) {
            return;
        }

        // 优化1：小数组切换到插入排序
        // 对于小规模数据，插入排序的常数因子更小，且能利用局部有序性
        if (hi - lo + 1 < INSERTION_THRESHOLD) {
            insertionSortRange(arr, lo, hi);
            return;
        }

        // 将数组切分为 [lo, pivotIdx-1] < pivot < [pivotIdx+1, hi]
        int pivotIdx = partition(arr, lo, hi);

        // 递归排序左半部分（不含 pivot）
        quickSort(arr, lo, pivotIdx - 1);

        // 递归排序右半部分（不含 pivot）
        quickSort(arr, pivotIdx + 1, hi);
    }


    private static <T extends Comparable<T>> int partition(T[] arr, int lo, int hi) {
        // ---- 三数取中（median-of-three）选 pivot，避免已有序时退化为 O(n²) ----
        int mid = lo + (hi - lo) / 2;

        // 三次比较将中值放到 arr[lo]（lo 为 pivot 的起始位置）
        // 确保 arr[lo] <= arr[mid] <= arr[hi] 的中值位于 arr[lo]
        if (less(arr[mid], arr[lo]))  swap(arr, lo, mid);    // lo 取较小者
        if (less(arr[hi],  arr[lo]))  swap(arr, lo, hi);     // lo 取三者最小
        if (less(arr[hi],  arr[mid])) swap(arr, mid, hi);    // mid 取三者中间值
        // 现在 arr[mid] 是三数的中值，将它换到 arr[lo] 作为 pivot
        swap(arr, lo, mid);

        T pivot = arr[lo];              // 基准值
        int i = lo;                     // 左指针：从最左开始
        int j = hi + 1;                 // 右指针：从最右+1开始（do-while 会先自减）

        // 主循环：i 和 j 相向而行，交换逆序对
        while (true) {

            // i 向右移动，直到找到 >= pivot 的元素（即不该在左边的元素）
            // 使用 less 而非 <= 是为了让等于 pivot 的元素也可以停在左边，
            // 它们最终会被换到正确的一侧
            do { i++; } while (i <= hi && less(arr[i], pivot));

            // j 向左移动，直到找到 <= pivot 的元素（即不该在右边的元素）
            do { j--; } while (j >= lo && less(pivot, arr[j]));

            // 指针相遇或交错 → 切分完成
            if (i >= j) {
                break;
            }

            // 交换逆序对：把左边的大元素和右边的小元素互换
            swap(arr, i, j);
        }

        // 将 pivot 放到最终位置
        // 此时 arr[lo+1..j] <= pivot，arr[j+1..hi] >= pivot
        // 且 arr[j] <= pivot，所以 arr[lo] 和 arr[j] 交换后 pivot 就位
        swap(arr, lo, j);

        return j;                       // pivot 的最终下标
    }

    // ======================== 工具方法 ========================

    /**
     * 比较 a 是否严格小于 b。
     * 统一使用此方法而非直接用 compareTo，便于后续统一修改比较逻辑。
     */
    private static <T extends Comparable<T>> boolean less(T a, T b) {
        return a.compareTo(b) < 0;
    }

    /** 交换数组中两个位置的元素 */
    private static <T> void swap(T[] arr, int i, int j) {
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }


    private static <T extends Comparable<T>> void insertionSortRange(T[] arr, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= lo && less(key, arr[j])) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    // ======================== 主方法（演示用） ========================

    /** 演示四种排序的运行结果 */
    public static void main(String[] args) {
        Integer[] original = { 64, 34, 25, 12, 22, 11, 90, 45, 7, 33 };
        System.out.println("原始数组：" + Arrays.toString(original));
        System.out.println();

        // 每种排序都从原始数组复制一份，便于对比
        Integer[] arr;

        arr = Arrays.copyOf(original, original.length);
        selectionSort(arr);
        System.out.println("Selection: " + Arrays.toString(arr));

        arr = Arrays.copyOf(original, original.length);
        insertionSort(arr);
        System.out.println("Insertion: " + Arrays.toString(arr));

        arr = Arrays.copyOf(original, original.length);
        mergeSort(arr);
        System.out.println("Merge:     " + Arrays.toString(arr));

        arr = Arrays.copyOf(original, original.length);
        quickSort(arr);
        System.out.println("Quick:     " + Arrays.toString(arr));
    }
}
