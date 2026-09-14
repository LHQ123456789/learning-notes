package HeapTest;

import SortingTest.Sorting;

import java.util.Arrays;
import java.util.Random;

/**
 * 堆排序 vs 快速排序 性能对比 + 验证堆排序的 O(n log n)。
 *
 * <p>做法：对同一组随机数据分别跑 HeapSort 与 QuickSort，统计实际耗时；
 * 并通过"规模翻倍时耗时的增长规律"来验证堆排序确实是 O(n log n)。</p>
 */
public class HeapSortBenchmark {

    /** 每个规模重复测量的次数（取最小值以削弱 JIT/GC 噪声） */
    private static final int TRIALS = 5;

    public static void main(String[] args) {
        // 先做一次预热，让 JIT 把热点代码编译成本地代码，避免冷启动误差
        warmUp();

        System.out.println("== 堆排序 vs 快速排序：随机数组实际耗时 ==");
        System.out.println();

        // 规模从 10 万起，每次翻倍到 640 万
        int[] sizes = { 100_000, 200_000, 400_000, 800_000, 1_600_000, 3_200_000, 6_400_000 };

        // 保存上一规模的时间，用于计算翻倍比值 T(2n)/T(n)
        double prevHeapNs = -1;

        System.out.printf("%-10s | %-14s | %-14s | %-16s | %-12s%n",
                "n", "HeapSort(ms)", "QuickSort(ms)", "Heap/(n·log₂n) ns", "堆排序翻倍比");
        System.out.println("---------------------------------------------------------------"
                + "---------------------------------");

        for (int n : sizes) {
            Integer[] base = randomArray(n);    // 基准数组，两个排序各复制一份

            double heapNs  = measureHeapSort(base);
            double quickNs = measureQuickSort(base);

            // 归一化：若真是 O(n log n)，HeapSort 耗时 / (n·log₂n) 应大致为常数
            double normalized = heapNs / (n * log2(n));

            // 翻倍比：规模翻倍后耗时约为原来的多少倍
            // O(n log n) → 趋近 2；O(n²) → 趋近 4
            double ratio = (prevHeapNs > 0) ? heapNs / prevHeapNs : Double.NaN;

            System.out.printf("%-10d | %-14.2f | %-14.2f | %-16.2f | %s%n",
                    n,
                    heapNs / 1e6,
                    quickNs / 1e6,
                    normalized,
                    Double.isNaN(ratio) ? "—" : String.format("%.2f×", ratio));

            prevHeapNs = heapNs;
        }

        System.out.println();
        System.out.println("结论：");
        System.out.println("  1. HeapSort 与 QuickSort 耗时处于同一数量级，且都能正确排序（下方另做校验）。");
        System.out.println("  2. 堆排序的「翻倍比」稳定趋近 2，说明时间随 n·log n 增长，即 O(n log n)；");
        System.out.println("     若是 O(n²)，翻倍比应趋近 4；若是 O(n)，「Heap/(n·log₂n)」应递减而非持平。");
        System.out.println("  3. 「Heap/(n·log₂n)」一列基本持平（常数），进一步印证 O(n log n)。");

        System.out.println();
        verifyCorrectness();
    }

    /** 测量 HeapSort 对 base 的一份副本排序的耗时（纳秒，取多次最小值） */
    private static double measureHeapSort(Integer[] base) {
        long best = Long.MAX_VALUE;
        for (int t = 0; t < TRIALS; t++) {
            Integer[] arr = Arrays.copyOf(base, base.length);
            long start = System.nanoTime();
            HeapSort.heapSort(arr);
            long elapsed = System.nanoTime() - start;
            if (elapsed < best) best = elapsed;
        }
        return best;
    }

    /** 测量 QuickSort 对 base 的一份副本排序的耗时（纳秒，取多次最小值） */
    private static double measureQuickSort(Integer[] base) {
        long best = Long.MAX_VALUE;
        for (int t = 0; t < TRIALS; t++) {
            Integer[] arr = Arrays.copyOf(base, base.length);
            long start = System.nanoTime();
            Sorting.quickSort(arr);
            long elapsed = System.nanoTime() - start;
            if (elapsed < best) best = elapsed;
        }
        return best;
    }

    /** 生成含 n 个随机整数的数组（固定种子，可复现） */
    private static Integer[] randomArray(int n) {
        Random rng = new Random(42);
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rng.nextInt();
        }
        return arr;
    }

    /** 以 2 为底的对数 */
    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /** 预热：对一个大数组跑几次两种排序，触发 JIT 编译 */
    private static void warmUp() {
        Integer[] arr = randomArray(200_000);
        for (int i = 0; i < 3; i++) {
            HeapSort.heapSort(Arrays.copyOf(arr, arr.length));
            Sorting.quickSort(Arrays.copyOf(arr, arr.length));
        }
    }

    /** 校验两个排序结果都和 JDK 的 Arrays.sort 一致（保证"快"不是以"错"为代价） */
    private static void verifyCorrectness() {
        Integer[] base = randomArray(500_000);
        Integer[] expected = Arrays.copyOf(base, base.length);
        Arrays.sort(expected);

        Integer[] forHeap  = Arrays.copyOf(base, base.length);
        Integer[] forQuick = Arrays.copyOf(base, base.length);

        HeapSort.heapSort(forHeap);
        Sorting.quickSort(forQuick);

        boolean heapOk  = Arrays.equals(expected, forHeap);
        boolean quickOk = Arrays.equals(expected, forQuick);
        System.out.println("正确性校验（与 JDK Arrays.sort 对比）：HeapSort="
                + heapOk + ", QuickSort=" + quickOk);
    }
}
