package TaskScheduler;

/**
 * 优先队列中的包装节点 —— 让"同一个任务"能以<b>优先级</b>参与堆的排序。
 *
 * <p>为什么需要这个包装类？</p>
 * <p>手写 {@link HeapTest.PriorityQueue}（底层 {@link HeapTest.MaxHeap}）要求元素实现
 * {@link Comparable}，并且"值更大"者先出队。但 {@link Task} 的自然序已经按 ID 排好了
 * （供 BST 使用），不能再按优先级排。于是用本类把 Task 包一层，改以<b>优先级</b>作为
 * 比较依据，从而复用同一套堆代码，不必为任务单独再写一个堆。</p>
 *
 * <p>比较规则：优先级高（数值大）的排前面；优先级相同时再按 ID 升序，保证出队顺序确定。</p>
 */
public class PriorityNode implements Comparable<PriorityNode> {

    /** 被包装的任务（与 BST 里存的是同一个 Task 对象引用） */
    private final Task task;

    public PriorityNode(Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    /**
     * 先比优先级（数值大的排前面）；优先级相同时按 ID <b>升序</b>（ID 小的先出队）。
     *
     * <p>注意：底层是<b>最大</b>堆（值更大者先弹出），所以要让"ID 更小"被视为"更大"，
     * 这里把 ID 比较结果反转 —— 返回 {@code o.id.compareTo(this.id)} 而非
     * {@code this.id.compareTo(o.id)}。</p>
     */
    @Override
    public int compareTo(PriorityNode o) {
        int cmp = Integer.compare(this.task.getPriority(), o.task.getPriority());
        if (cmp != 0) {
            return cmp;
        }
        return o.task.getId().compareTo(this.task.getId());   // 反转：ID 小的视为更大
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
