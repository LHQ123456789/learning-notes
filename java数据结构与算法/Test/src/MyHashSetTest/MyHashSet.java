package MyHashSetTest;

import MyHashMapTest.MyHashMap;

/**
 * 手写 HashSet —— 基于 MyHashMap 实现的泛型哈希集合。
 * 底层复用 MyHashMap&lt;T, Object&gt;，元素作为 key 存储，
 * value 统一使用内部的占位对象 PRESENT。
 *
 * 支持 add/remove/contains/size/isEmpty，具备自动扩容能力。
 */
public class MyHashSet<T> {

    /** 占位对象：所有 key 对应的 value 都指向它 */
    private static final Object PRESENT = new Object();

    /** 底层哈希表 —— 只关心 key，value 全部用 PRESENT 填充 */
    private final MyHashMap<T, Object> map;

    // ==================== 构造函数 ====================

    public MyHashSet() {

        map = new MyHashMap<>();
    }

    public MyHashSet(int initialCapacity) {
        map = new MyHashMap<>(initialCapacity);
    }

    // ==================== 基本信息 ====================

    /** 集合中元素个数 */
    public int size() {
        return map.size();
    }

    /** 集合是否为空 */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** 当前底层桶数组容量（用于验证扩容） */
    public int capacity() {
        return map.capacity();
    }

    // ==================== 核心操作 ====================

    /**
     * 向集合中添加元素。
     *
     * @param element 待添加的元素
     * @return true  本次新增了元素（元素之前不存在）
     *         false 元素已存在，不做任何操作
     */
    public boolean add(T element) {
        // MyHashMap.put 返回旧值：
        //   返回 null  → key 之前不存在 → 新增成功 → 返回 true
        //   返回 PRESENT → key 已存在   → 仅更新  → 返回 false
        return map.put(element, PRESENT) == null;
    }

    /**
     * 从集合中删除元素。
     *
     * @param element 待删除的元素
     * @return true  成功删除
     *         false 元素不存在
     */
    public boolean remove(T element) {
        // MyHashMap.remove 返回被删除的 value：
        //   返回 PRESENT → key 存在并删除 → true
        //   返回 null    → key 不存在     → false
        return map.remove(element) != null;
    }

    /**
     * 判断集合是否包含指定元素。
     *
     * @param element 待查找的元素
     * @return true 存在，false 不存在
     */
    public boolean contains(T element) {
        return map.containsKey(element);
    }
}
