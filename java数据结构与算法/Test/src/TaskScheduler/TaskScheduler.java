package TaskScheduler;

import BinaryTreeTest.BinaryTree;
import HeapTest.PriorityQueue;

import java.util.List;

/**
 * 任务调度器 —— 用「优先队列 + 二叉搜索树」双索引实现。
 *
 * <p>数据结构分工：</p>
 * <ul>
 *   <li>{@link PriorityQueue}（底层 MaxHeap）：按<b>优先级</b>维护任务，
 *        O(log n) 拿到并弹出最高优先级任务；</li>
 *   <li>{@link BinaryTree}（BST）：按<b>ID</b> 维护任务，
 *        O(log n) 按 ID 查找/删除。</li>
 * </ul>
 *
 * <p>支持三种操作：</p>
 * <ol>
 *   <li>添加任务 {@link #addTask} —— 同时插入两个结构；</li>
 *   <li>执行最高优先级任务 {@link #executeNext} —— 从堆顶弹出，并从 BST 删除；</li>
 *   <li>按 ID 取消任务 {@link #cancel} —— 从 BST 删除，堆里用「惰性删除」标记作废。</li>
 * </ol>
 *
 * <p><b>核心难点：</b>堆只支持"删堆顶"，不支持"删任意元素"。
 * 所以取消任务时无法直接从堆里把它删掉，于是采用<b>惰性删除（lazy deletion / 墓碑法）</b>：
 * 先给任务打个"已取消"标记，等它以后"浮到堆顶"时再跳过。这样堆里虽然还留着占位节点，
 * 但出队时会被自动过滤，逻辑上等价于已删除。</p>
 */
public class TaskScheduler {

    /** 按 ID 索引任务（BST）。key 是 Task，其 compareTo 按 id 排序 */
    private final BinaryTree<Task> idIndex = new BinaryTree<>();

    /** 按优先级索引任务（最大优先队列）。元素是 PriorityNode，其 compareTo 按优先级排序 */
    private final PriorityQueue<PriorityNode> priorityQueue = new PriorityQueue<>();

    // ======================== 添加任务 ========================

    /**
     * 添加一个任务：同时插入 BST（按 ID）和优先队列（按优先级）。
     *
     * @throws IllegalArgumentException 当 ID 已存在时
     */
    public void addTask(String id, int priority, String name) {
        if (findById(id) != null) {
            throw new IllegalArgumentException("任务 ID 已存在：" + id);
        }
        Task task = new Task(id, priority, name);
        idIndex.put(task);                        // BST 按 ID 插入，O(log n)
        priorityQueue.enqueue(new PriorityNode(task));  // 堆按优先级插入，O(log n)
    }

    // ======================== 执行最高优先级任务 ========================

    /**
     * 执行（取出并删除）当前优先级最高的任务。
     * 弹出堆顶时跳过所有"已取消"的墓碑节点（惰性删除）。
     *
     * @return 被执行的任务；若没有待执行任务返回 null
     */
    public Task executeNext() {
        while (!priorityQueue.isEmpty()) {
            Task task = priorityQueue.dequeue().getTask();
            if (task.isCancelled()) {
                continue;                        // 墓碑：已被取消，跳过
            }
            idIndex.remove(task);                // 从 BST 中删除，O(log n)
            return task;
        }
        return null;
    }

    // ======================== 按 ID 取消任务 ========================

    /**
     * 查看下一个要执行的任务（不删除）。
     * 会顺带清掉堆顶的墓碑节点（它们已无意义）。
     */
    public Task peekNext() {
        while (!priorityQueue.isEmpty() && priorityQueue.peek().getTask().isCancelled()) {
            priorityQueue.dequeue();             // 丢弃堆顶墓碑
        }
        return priorityQueue.isEmpty() ? null : priorityQueue.peek().getTask();
    }

    /**
     * 按 ID 取消任务。
     * BST 立即删除；堆里打"已取消"标记做惰性删除（等浮到堆顶再跳过）。
     *
     * @return true=成功取消；false=该 ID 不存在或已被取消
     */
    public boolean cancel(String id) {
        Task task = findById(id);
        if (task == null || task.isCancelled()) {
            return false;
        }
        task.markCancelled();                    // 堆里留墓碑，等出队时跳过
        idIndex.remove(task);                    // BST 立即删除，O(log n)
        return true;
    }

    // ======================== 查询 ========================

    /** 当前待执行的任务数（BST 中的节点数 = 活跃任务数）。 */
    public int size() {
        return idIndex.size();
    }

    /** 是否没有待执行任务 */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 按 ID 查找任务（O(log n)）。
     * 用一个只带 ID 的"探测任务"在 BST 里定位（BST 的 compareTo 只看 ID）。
     *
     * @return 命中的任务；找不到返回 null
     */
    public Task findById(String id) {
        return idIndex.get(new Task(id));
    }

    /** 按 ID 升序打印所有待执行任务（BST 中序遍历天然有序）。 */
    public void printById() {
        System.out.println("按 ID 排序的待执行任务：" + idIndex.inorderRecursive());
    }

    // ======================== 演示用 main ========================

    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();

        System.out.println("========== 1. 添加任务 ==========");
        scheduler.addTask("T-A", 3, "发邮件");
        scheduler.addTask("T-B", 5, "紧急告警处理");
        scheduler.addTask("T-C", 1, "写周报");
        scheduler.addTask("T-D", 5, "数据库备份");
        scheduler.addTask("T-E", 4, "代码评审");
        scheduler.printById();
        System.out.println("当前待执行任务数：" + scheduler.size());

        System.out.println("\n========== 2. 执行最高优先级任务 ==========");
        System.out.println("下一个待执行（peek，不删除）：" + scheduler.peekNext());
        System.out.println("执行 -> " + scheduler.executeNext());   // 优先级 5，ID 小的 T-B 先出
        System.out.println("执行 -> " + scheduler.executeNext());   // 优先级 5 的 T-D

        System.out.println("\n========== 3. 按 ID 取消任务 ==========");
        System.out.println("取消 T-E：" + scheduler.cancel("T-E")); // true
        System.out.println("取消不存在的 T-Z：" + scheduler.cancel("T-Z")); // false
        scheduler.printById();

        System.out.println("\n========== 4. 把剩余任务全部执行完 ==========");
        List<Task> done = new java.util.ArrayList<>();
        Task next;
        while ((next = scheduler.executeNext()) != null) {
            done.add(next);                       // 被取消的 T-E 会在堆顶被跳过
        }
        System.out.println("执行顺序：" + done);
        System.out.println("剩余任务数：" + scheduler.size());
    }
}
