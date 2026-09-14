package HeapTest;

import java.util.Arrays;

/**
 * 手写最大堆 MaxHeap&lt;T&gt;（基于数组的完全二叉树）。
 *
 * <p>核心思想：用一维数组存储一棵<b>完全二叉树</b>，利用下标之间的固定关系
 * 隐式表达父子结构，无需真正的指针。</p>
 *
 * <pre>
 * 下标关系（数组从 0 开始）：
 *   父节点下标   parent(i)      = (i - 1) / 2
 *   左孩子下标   leftChild(i)   = 2 * i + 1
 *   右孩子下标   rightChild(i)  = 2 * i + 2
 *
 * 例：数组 [50, 30, 40, 20, 10, 15]
 *              50(0)
 *            /      \
 *         30(1)     40(2)
 *         /   \      /
 *      20(3) 10(4) 15(5)
 * </pre>
 *
 * <p><b>复杂度</b>：堆是一棵高度为 ⌊log n⌋ 的完全二叉树，因此</p>
 * <ul>
 *   <li>{@link #insert} 上浮 O(log n)</li>
 *   <li>{@link #extractMax} 下沉 O(log n)</li>
 *   <li>{@link #peek} / {@link #size} O(1)</li>
 *   <li>{@link #heapify} 批量建堆 O(n) —— 详见方法注释</li>
 * </ul>
 */
public class MaxHeap<T extends Comparable<T>> {

    /** 存放堆元素的数组，data[0] 是堆顶（最大元素） */
    private T[] data;

    /** 当前堆中元素个数（也是下一个待插入元素的下标） */
    private int size;

    /** 默认初始容量 */
    private static final int DEFAULT_CAPACITY = 16;

    // ======================== 构造方法 ========================

    /** 创建一个空的最大堆，初始容量 16 */
    public MaxHeap() {
        this(DEFAULT_CAPACITY);
    }

    /** 创建一个指定初始容量的空最大堆 */
    public MaxHeap(int capacity) {
        @SuppressWarnings("unchecked")
        T[] arr = (T[]) new Comparable[capacity];//用具体类型定义数组，再用泛型转换
        this.data = arr;
        this.size = 0;
    }

    /**
     * 批量建堆：直接把给定数组当作堆的存储，并在 O(n) 时间内调整为最大堆。
     *
     * <p>为什么是 O(n) 而不是 O(n log n)？</p>
     * <p>从最后一个非叶子节点（下标 size/2 - 1）开始，<b>自底向上</b>依次对每个节点
     * 执行下沉（siftDown）。虽然每次下沉最坏 O(log n)，但：</p>
     * <ul>
     *   <li>靠近底部的节点下沉距离短（绝大多数节点只下沉 0~1 层）；</li>
     *   <li>第 h 层最多有 n/2^(h+1) 个节点，每个最多下沉 h 层。</li>
     * </ul>
     * 求和得到总下沉次数 ≤ n，因此整体是 <b>O(n)</b>。
     * （直观理解：堆顶 1 个节点沉最多 log n 层，叶子层 n/2 个节点沉 0 层，相互抵消。）
     */
    public MaxHeap(T[] arr) {
        this.data = Arrays.copyOf(arr, arr.length);
        this.size = arr.length;
        // 从最后一个非叶子节点开始，向下逐个"修复"以该节点为根的子树
        for (int i = (size / 2) - 1; i >= 0; i--) {
            siftDown(i);
        }
    }

    // ======================== 核心操作 ========================

    /**
     * 插入元素：先放到数组末尾（完全二叉树的最后一个位置），再<b>上浮</b>到正确位置。
     *
     * <p>上浮（siftUp）：新节点不断与父节点比较，只要比父节点大就交换，
     * 直到到达堆顶或不再大于父节点为止。这样始终维持"父 ≥ 子"的堆序性质。</p>
     */
    public void insert(T item) {
        if (item == null) {
            throw new IllegalArgumentException("不允许插入 null 元素");
        }
        ensureCapacity(size + 1);   // 扩容检查
        data[size] = item;          // 先放到末尾
        siftUp(size);               // 再上浮
        size++;
    }

    /**
     * 取出并删除堆顶（最大值）：把最后一个元素换到堆顶，删除末尾，再<b>下沉</b>。
     *
     * <p>下沉（siftDown）：当前节点与左右孩子中较大的一个比较，
     * 若比它小则交换并继续下沉，直到满足"父 ≥ 子"或成为叶子。</p>
     *
     * @return 堆中的最大值；若堆为空返回 null
     */
    public T extractMax() {
        if (isEmpty()) {
            return null;
        }
        T max = data[0];                    // 堆顶即最大值
        size--;                             // 元素个数减一
        data[0] = data[size];               // 用最后一个元素填补堆顶空位
        data[size] = null;                  // 清空末尾引用，帮助 GC
        siftDown(0);                        // 从堆顶开始下沉，恢复堆序
        return max;
    }

    /** 查看堆顶（最大值）但不删除。堆为空返回 null。 */
    public T peek() {
        return isEmpty() ? null : data[0];
    }

    /** 堆中元素个数 */
    public int size() {
        return size;
    }

    /** 堆是否为空 */
    public boolean isEmpty() {
        return size == 0;
    }

    // ======================== 上浮 / 下沉 ========================

    /**
     * 上浮：把下标 k 处的节点向上移动到正确位置。
     * 循环中 k 始终指向当前节点，与父节点 (k-1)/2 比较。
     */
    private void siftUp(int k) {
        while (k > 0) {
            int parent = (k - 1) / 2;
            // 已满足堆序（当前 ≤ 父），停止上浮
            if (data[k].compareTo(data[parent]) <= 0) {
                break;
            }
            swap(k, parent);
            k = parent;                 // 继续向上检查
        }
    }

    /**
     * 下沉：把下标 k 处的节点向下移动到正确位置。
     * 每次选择左右孩子中较大的那个与当前节点比较，决定是否交换。
     */
    private void siftDown(int k) {
        // 只要还有左孩子就继续（左孩子存在是节点还有孩子的充分条件）
        while (leftChild(k) < size) {
            int left = leftChild(k);
            int right = rightChild(k);
            int larger = left;                      // 先假设左孩子更大

            // 若右孩子存在且比左孩子大，则更大的孩子是右孩子
            if (right < size && data[right].compareTo(data[left]) > 0) {
                larger = right;
            }

            // 当前节点 ≥ 最大的孩子，已满足堆序，停止下沉
            if (data[k].compareTo(data[larger]) >= 0) {
                break;
            }

            swap(k, larger);
            k = larger;                             // 继续向下检查
        }
    }

    // ======================== 工具方法 ========================

    /** 父节点下标 */
    private int parent(int i) {
        return (i - 1) / 2;
    }

    /** 左孩子下标 */
    private int leftChild(int i) {
        return 2 * i + 1;
    }

    /** 右孩子下标 */
    private int rightChild(int i) {
        return 2 * i + 2;
    }

    /** 交换两个下标处的元素 */
    private void swap(int i, int j) {
        T temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    /** 容量不足时扩容为原来的 2 倍（和 ArrayList 的摊销策略一致） */
    private void ensureCapacity(int needed) {
        if (needed > data.length) {
            data = Arrays.copyOf(data, Math.max(data.length * 2, needed));
        }
    }

    /** 调试用：按层打印堆中元素 */
    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(data, size));
    }
}
