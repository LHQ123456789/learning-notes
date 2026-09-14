package HeapTest;

/**
 * 基于 {@link MaxHeap} 手写的优先级队列（最大优先队列）。
 *
 * <p>优先级队列的定义：元素按"优先级"而非"先进先出"的顺序出队。
 * 这里用最大堆实现，因此每次出队的是优先级最高（值最大）的元素。</p>
 *
 * <p>为什么不直接用链表/有序数组？因为堆的插入和删除都能做到 O(log n)，
 * 而有序数组插入是 O(n)、无序数组删除是 O(n)，堆是二者兼顾的最优选择。</p>
 *
 * <p>本类是对 {@link MaxHeap} 的<b>适配器（Adapter）</b>：本身不重复实现堆逻辑，
 * 而是把"入队/出队/查看队首"等队列语义翻译成堆的 insert / extractMax / peek。</p>
 */
public class PriorityQueue<T extends Comparable<T>> {

    /** 底层用最大堆存储元素，堆顶即优先级最高者 */
    private final MaxHeap<T> heap;

    public PriorityQueue() {
        heap = new MaxHeap<>();
    }

    public PriorityQueue(int capacity) {
        heap = new MaxHeap<>(capacity);
    }

    /** 入队：插入一个元素（上浮 O(log n)） */
    public void enqueue(T item) {
        heap.insert(item);
    }

    /** 出队：取出并删除优先级最高的元素（下沉 O(log n)）。队空返回 null。 */
    public T dequeue() {
        return heap.extractMax();
    }

    /** 查看队首（优先级最高的元素）但不删除。队空返回 null。 */
    public T peek() {
        return heap.peek();
    }

    /** 队列中元素个数 */
    public int size() {
        return heap.size();
    }

    /** 队列是否为空 */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    @Override
    public String toString() {
        return heap.toString();
    }
}
