package TaskScheduler;

/**
 * 任务实体 —— 调度器里最小的调度单元，包含唯一 ID、优先级和描述信息。
 *
 * <p>本类的 {@link #compareTo} 按 <b>ID</b> 排序（String 自然序），因此可直接作为
 * 二叉搜索树（BST）的键 —— 这样 BST 才能"按任务 ID"做到 O(log n) 查找/删除。
 * 而"按优先级"排序则交给 {@link PriorityNode} 处理（见该类）。</p>
 *
 * <p>为什么一个任务需要两种排序？</p>
 * <ul>
 *   <li>优先队列要求按<b>优先级</b>比较 —— 才能让最高优先级的任务先出队；</li>
 *   <li>BST 要求按<b>ID</b> 比较 —— 才能按 ID 快速定位某个任务。</li>
 * </ul>
 * Java 的单个对象只能有一个自然序（compareTo），无法同时按两个键排序，
 * 所以这里用「拆分职责」的办法：Task 负责 ID 键，PriorityNode 负责优先级键。</p>
 */
public class Task implements Comparable<Task> {

    /** 任务唯一标识（不可变） */
    private final String id;

    /** 优先级：数值越大优先级越高（不可变） */
    private final int priority;

    /** 任务描述信息 */
    private final String name;

    /** 是否已被取消（惰性删除标记，详见 TaskScheduler 类注释） */
    private boolean cancelled;

    public Task(String id, int priority, String name) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("任务 ID 不能为空");
        }
        this.id = id;
        this.priority = priority;
        this.name = name;
        this.cancelled = false;
    }

    /**
     * 仅供"探测查找"使用的构造：BST 的 compareTo 只看 ID，
     * 所以用一个只带 ID 的空壳任务即可在树中定位同名任务。
     */
    Task(String id) {
        this(id, 0, "");
    }

    /** 按 ID 排序（BST 的键）。ID 相同即视为同一个任务（不重复插入）。 */
    @Override
    public int compareTo(Task o) {
        return this.id.compareTo(o.id);
    }

    // ======================== getter ========================

    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    // ======================== 包内方法（供 TaskScheduler 调用） ========================

    /** 标记为已取消（惰性删除：先打标记，等浮到堆顶再跳过） */
    void markCancelled() {
        this.cancelled = true;
    }

    @Override
    public String toString() {
        return String.format("Task{id=%s, priority=%d, name=%s%s}",
                id, priority, name, cancelled ? ", [已取消]" : "");
    }
}
