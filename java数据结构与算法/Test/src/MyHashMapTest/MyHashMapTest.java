package MyHashMapTest;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * MyHashMap 的 JUnit 测试用例
 */
public class MyHashMapTest {

    private MyHashMap<String, Integer> map;

    @Before
    public void setUp() {
        map = new MyHashMap<>();
    }

    // ==================== 1. 基本 put / get / containsKey ====================

    @Test
    public void testPutGetAndContainsKey() {
        // 空 map
        assertTrue("新 map 应为空", map.isEmpty());
        assertEquals("size 应为 0", 0, map.size());

        // put 新增
        assertNull("新增应返回 null", map.put("a", 1));
        assertNull("新增应返回 null", map.put("b", 2));
        assertNull("新增应返回 null", map.put("c", 3));
        assertEquals("size 应为 3", 3, map.size());
        assertFalse("不应为空", map.isEmpty());

        // get
        assertEquals("get('a') = 1", Integer.valueOf(1), map.get("a"));
        assertEquals("get('b') = 2", Integer.valueOf(2), map.get("b"));
        assertEquals("get('c') = 3", Integer.valueOf(3), map.get("c"));
        assertNull("get 不存在的 key 应返回 null", map.get("nonexistent"));

        // containsKey
        assertTrue("应包含 'a'", map.containsKey("a"));
        assertTrue("应包含 'b'", map.containsKey("b"));
        assertFalse("不应包含 'nonexistent'", map.containsKey("nonexistent"));

        // put 更新已有 key
        assertEquals("更新应返回旧值 1", Integer.valueOf(1), map.put("a", 100));
        assertEquals("更新后 size 应不变", 3, map.size());
        assertEquals("更新后 get('a') = 100", Integer.valueOf(100), map.get("a"));
    }

    // ==================== 2. remove ====================

    @Test
    public void testRemove() {
        map.put("x", 10);
        map.put("y", 20);
        map.put("z", 30);
        assertEquals(3, map.size());

        // 删除存在的 key
        assertEquals("删除 'y' 应返回 20", Integer.valueOf(20), map.remove("y"));
        assertEquals("删除后 size = 2", 2, map.size());
        assertNull("删除后 get('y') 应为 null", map.get("y"));
        assertFalse("删除后不应包含 'y'", map.containsKey("y"));
        assertTrue("仍应包含 'x'", map.containsKey("x"));
        assertTrue("仍应包含 'z'", map.containsKey("z"));

        // 删除不存在的 key
        assertNull("删除不存在的 key 应返回 null", map.remove("nonexistent"));
        assertEquals("size 应不变", 2, map.size());

        // 删除链表头节点
        assertEquals("删除 'x'", Integer.valueOf(10), map.remove("x"));
        assertEquals(1, map.size());

        // 删除最后一个节点
        assertEquals("删除 'z'", Integer.valueOf(30), map.remove("z"));
        assertEquals(0, map.size());
        assertTrue("map 应为空", map.isEmpty());
    }

    // ==================== 3. null key 支持 ====================

    @Test
    public void testNullKey() {
        // null key 插入
        assertNull(map.put(null, 999));
        assertTrue("应包含 null key", map.containsKey(null));
        assertEquals("get(null) = 999", Integer.valueOf(999), map.get(null));

        // null key 更新
        assertEquals("更新 null key 应返回 999", Integer.valueOf(999), map.put(null, 888));
        assertEquals("更新后 get(null) = 888", Integer.valueOf(888), map.get(null));

        // null key 删除
        assertEquals("删除 null key 应返回 888", Integer.valueOf(888), map.remove(null));
        assertFalse("删除后不应包含 null", map.containsKey(null));
        assertNull("删除后 get(null) 应为 null", map.get(null));
    }

    // ==================== 4. null value 支持 ====================

    @Test
    public void testNullValue() {
        map.put("key", null);
        assertTrue("应包含 key", map.containsKey("key"));
        assertNull("get 应返回 null（value 为 null）", map.get("key"));

        // 通过 containsKey 区分"不存在"与"value 为 null"
        assertTrue("containsKey 可区分不存在与 value=null", map.containsKey("key"));
        assertFalse("不存在的 key", map.containsKey("other"));
    }

    // ==================== 5. 哈希冲突（拉链） ====================

    @Test
    public void testHashCollision() {
        // 构造哈希冲突：利用 Integer 的 hashCode 就是其值本身，手动算桶索引
        // 默认容量 16，任何 hashCode % 16 相等的 key 会落入同一个桶
        // 例如：0, 16, 32 都在 0 号桶
        MyHashMap<Integer, String> intMap = new MyHashMap<>();

        intMap.put(0, "zero");
        intMap.put(16, "sixteen");
        intMap.put(32, "thirty-two");
        assertEquals("三个 key 应在同一桶中", 3, intMap.size());

        // 验证都能正确读取
        assertEquals("zero", intMap.get(0));
        assertEquals("sixteen", intMap.get(16));
        assertEquals("thirty-two", intMap.get(32));

        // 删除中间节点（16）
        assertEquals("sixteen", intMap.remove(16));
        assertEquals(2, intMap.size());
        assertEquals("zero", intMap.get(0));
        assertEquals("thirty-two", intMap.get(32));
        assertFalse(intMap.containsKey(16));

        // 删除头节点（0）
        assertEquals("zero", intMap.remove(0));
        assertEquals(1, intMap.size());
        assertEquals("thirty-two", intMap.get(32));

        // 删除尾节点（32）
        assertEquals("thirty-two", intMap.remove(32));
        assertEquals(0, intMap.size());
    }

    // ==================== 6. 扩容 + rehash ====================

    @Test
    public void testResizeAndRehash() {
        // 默认容量 16，负载因子 0.75 → 阈值 12
        assertEquals("初始容量 16", 16, map.capacity());

        // 插入 12 个元素，刚好达到阈值（但不触发）
        for (int i = 0; i < 12; i++) {
            map.put("key" + i, i);
        }
        assertEquals(12, map.size());
        assertEquals("容量仍为 16", 16, map.capacity());

        // 插入第 13 个元素，触发扩容
        map.put("key12", 12);
        assertEquals(13, map.size());
        assertEquals("扩容后容量应为 32", 32, map.capacity());

        // 验证扩容后所有数据完整（rehash 正确）
        for (int i = 0; i <= 12; i++) {
            assertTrue("应包含 key" + i, map.containsKey("key" + i));
            assertEquals("key" + i + " 值应正确",
                Integer.valueOf(i), map.get("key" + i));
        }

        // 继续插入更多元素触发二次扩容
        for (int i = 13; i < 24; i++) {
            map.put("key" + i, i);
        }
        // 此时已有 24 个元素，阈值 = 32 * 0.75 = 24
        map.put("key24", 24);   // 触发扩容 → 64
        assertEquals(25, map.size());
        assertEquals("二次扩容后容量应为 64", 64, map.capacity());

        // 所有数据仍正确
        for (int i = 0; i <= 24; i++) {
            assertEquals(Integer.valueOf(i), map.get("key" + i));
        }
    }

    // ==================== 7. 边界条件 ====================

    @Test
    public void testEdgeCases() {
        // 自定义容量为 1
        MyHashMap<Integer, String> tiny = new MyHashMap<>(1);
        assertEquals(1, tiny.capacity());

        tiny.put(1, "a");
        tiny.put(2, "b");   // size=2，threshold=0，扩容到 2

        assertTrue("扩容后数据完整", tiny.containsKey(1));
        assertTrue("扩容后数据完整", tiny.containsKey(2));

        // 非法初始容量
        try {
            new MyHashMap<String, String>(0);
            fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期
        }

        try {
            new MyHashMap<String, String>(-1);
            fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期
        }

        // 空 map 的各种操作
        MyHashMap<String, String> empty = new MyHashMap<>();
        assertNull("空 map get 返回 null", empty.get("any"));
        assertFalse("空 map containsKey 返回 false", empty.containsKey("any"));
        assertNull("空 map remove 返回 null", empty.remove("any"));
        assertEquals(0, empty.size());
        assertTrue("空 map isEmpty", empty.isEmpty());
    }

    // ==================== 8. 多次更新不改变 size ====================

    @Test
    public void testRepeatedPutSameKey() {
        map.put("x", 1);
        map.put("x", 2);
        map.put("x", 3);
        assertEquals("重复 put 同一 key，size 应为 1", 1, map.size());
        assertEquals("值应为最后一次 put 的值", Integer.valueOf(3), map.get("x"));
    }
}
