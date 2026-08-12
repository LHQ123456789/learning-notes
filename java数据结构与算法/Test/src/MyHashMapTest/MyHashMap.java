package MyHashMapTest;

/**
 * 手写 HashMap —— 基于拉链法（separate chaining）实现的泛型哈希表。
 * 底层为 Node&lt;K,V&gt;[] 桶数组，冲突时通过链表链接。
 * 支持 put/get/remove/containsKey，当负载因子达到阈值时自动扩容并 rehash。
 */
public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K, V>[] buckets;   // 桶数组
    private int size;               // 键值对数量
    private final float loadFactor; // 负载因子
    private int threshold;          // 扩容阈值 = capacity * loadFactor

    // ---------- 链表节点 ----------

    //定义节点，数据包含key和value
    private static class Node<K, V> {
        //简化key和value，符合java规定
        final K key;
        V value;
        Node<K, V> next;

        //构造方法给node对象赋值
        Node(K key, V value, Node<K, V> next) {
            this.key = key; //final属性需要对key初始赋值，所以后续有修改key的方法无法实现
            this.value = value;
            this.next = next;
        }
    }

    // ---------- 构造函数 ----------

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.buckets = (Node<K, V>[]) new Node[DEFAULT_CAPACITY];   //后面的Node时Node数据类型的数组
        this.threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be positive: " + initialCapacity);
        }
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.buckets = (Node<K, V>[]) new Node[initialCapacity];
        this.threshold = (int) (initialCapacity * DEFAULT_LOAD_FACTOR);
        this.size = 0;
    }

    // ---------- 基本信息 ----------

    /** 键值对数量 */
    public int size() {
        return size;
    }

    /** 是否为空 */
    public boolean isEmpty() {
        return size == 0;
    }

    /** 当前桶数组长度 */
    public int capacity() {
        return buckets.length;
    }

    // ---------- 哈希函数 ----------

    /**
     * 计算 key 对应的桶下标。
     * 使用 hashCode 取绝对值后对桶长度取模。
     */
    private int hash(K key) {
        if (key == null) {
            return 0;   // null 固定存放在 0 号桶
        }
        return Math.abs(key.hashCode()) % buckets.length;   //继承object类中的hashcode（）；常见数据类型的值的hashcode固定用来查找
    }

    // ---------- 增 / 改 ----------

    /**
     * 插入键值对。若 key 已存在则更新旧值。
     * 插入前检查是否需要扩容，满足条件则先 resize + rehash。
     *
     * @return key 对应的旧值，若为新增则返回 null
     */
    public V put(K key, V value) {
        // 先检查扩容：size >= threshold 时触发
        if (size >= threshold) {
            resize(buckets.length * 2);
        }

        int idx = hash(key);
        Node<K, V> head = buckets[idx];

        // 桶为空，直接放入新节点作为链表头
        if (head == null) {
            buckets[idx] = new Node<>(key, value, null);
            size++;
            return null;
        }

        // 遍历链表：查找 key 是否存在
        Node<K, V> cur = head;
        while (cur != null) {
            if (keyEquals(cur.key, key)) {
                // key 存在 → 更新旧值
                V oldValue = cur.value;
                cur.value = value;
                return oldValue;
            }
            cur = cur.next;
        }

        // key 不存在 → 头插法（简化实现，也可尾插）
        buckets[idx] = new Node<>(key, value, head);
        size++;
        return null;
    }

    // ---------- 查 ----------

    /**
     * 根据 key 获取 value，不存在则返回 null。
     * 注意：返回 null 也可能是 key 存在但 value 为 null，可结合 containsKey 区分。
     */
    public V get(K key) {
        int idx = hash(key);
        Node<K, V> cur = buckets[idx];

        while (cur != null) {
            if (keyEquals(cur.key, key)) {
                return cur.value;
            }
            cur = cur.next;
        }
        return null;
    }

    /** 是否包含指定的 key */
    public boolean containsKey(K key) {
        int idx = hash(key);
        Node<K, V> cur = buckets[idx];

        while (cur != null) {
            if (keyEquals(cur.key, key)) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    // ---------- 删 ----------

    /**
     * 删除 key 对应的键值对。
     *
     * @return 被删除的 value，若 key 不存在则返回 null
     */
    public V remove(K key) {
        int idx = hash(key);
        Node<K, V> head = buckets[idx];

        if (head == null) {
            return null;
        }

        // 链表头即为目标
        if (keyEquals(head.key, key)) {
            buckets[idx] = head.next;
            size--;
            return head.value;
        }

        // 遍历链表查找前驱节点
        Node<K, V> prev = head;
        Node<K, V> cur = head.next;
        while (cur != null) {
            if (keyEquals(cur.key, key)) {
                prev.next = cur.next;
                size--;
                return cur.value;
            }
            prev = cur;
            cur = cur.next;
        }
        return null;
    }

    // ---------- 扩容 + rehash ----------

    /**
     * 扩容并重新哈希所有键值对。
     * 创建一个新容量为 newCapacity 的桶数组，遍历所有旧桶中的链表节点，
     * 重新计算哈希后插入新桶。
     */
    @SuppressWarnings("unchecked")
    public void resize(int newCapacity) {
        Node<K, V>[] oldBuckets = buckets;
        Node<K, V>[] newBuckets = (Node<K, V>[]) new Node[newCapacity];

        // 遍历所有旧桶
        for (int i = 0; i < oldBuckets.length; i++) {
            Node<K, V> cur = oldBuckets[i];
            while (cur != null) {
                Node<K, V> next = cur.next;   // 暂存后继，避免断链

                // 在新桶中计算下标（头插法）
                int newIdx = Math.abs(
                    (cur.key == null) ? 0 : cur.key.hashCode()
                ) % newCapacity;

                cur.next = newBuckets[newIdx];
                newBuckets[newIdx] = cur;

                cur = next;
            }
        }

        buckets = newBuckets;
        threshold = (int) (newCapacity * loadFactor);
    }

    // ---------- 内部工具 ----------

    /** 比较两个 key 是否相等（兼容 null） */
    private boolean keyEquals(K a, K b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
