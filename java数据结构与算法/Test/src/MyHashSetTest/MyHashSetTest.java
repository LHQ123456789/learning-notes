package MyHashSetTest;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.*;

/**
 * MyHashSet 的 JUnit 测试用例
 */
public class MyHashSetTest {

    private MyHashSet<String> set;

    @Before
    public void setUp() {
        set = new MyHashSet<>();
    }

    // ==================== 1. 基本 add / contains / size ====================

    @Test
    public void testAddContainsAndSize() {
        // 空集合
        assertTrue("新集合应为空", set.isEmpty());
        assertEquals("size 应为 0", 0, set.size());

        // add 新增
        assertTrue("add('a') 应返回 true（新增）", set.add("a"));
        assertTrue("add('b') 应返回 true（新增）", set.add("b"));
        assertTrue("add('c') 应返回 true（新增）", set.add("c"));
        assertEquals("size 应为 3", 3, set.size());
        assertFalse("不应为空", set.isEmpty());

        // contains
        assertTrue("应包含 'a'", set.contains("a"));
        assertTrue("应包含 'b'", set.contains("b"));
        assertTrue("应包含 'c'", set.contains("c"));
        assertFalse("不应包含 'x'", set.contains("x"));

        // add 重复元素
        assertFalse("add('a') 重复应返回 false", set.add("a"));
        assertFalse("add('b') 重复应返回 false", set.add("b"));
        assertEquals("重复 add 后 size 应不变", 3, set.size());
    }

    // ==================== 2. remove ====================

    @Test
    public void testRemove() {
        set.add("x");
        set.add("y");
        set.add("z");
        assertEquals(3, set.size());

        // 删除存在的元素
        assertTrue("remove('y') 应返回 true", set.remove("y"));
        assertEquals("删除后 size = 2", 2, set.size());
        assertFalse("删除后不应包含 'y'", set.contains("y"));
        assertTrue("仍应包含 'x'", set.contains("x"));
        assertTrue("仍应包含 'z'", set.contains("z"));

        // 删除不存在的元素
        assertFalse("remove 不存在的元素应返回 false", set.remove("nonexistent"));
        assertEquals("size 应不变", 2, set.size());

        // 删除剩余元素
        assertTrue(set.remove("x"));
        assertTrue(set.remove("z"));
        assertEquals(0, set.size());
        assertTrue("集合应为空", set.isEmpty());

        // 对空集合删除
        assertFalse("空集合 remove 应返回 false", set.remove("any"));
    }

    // ==================== 3. null 元素支持 ====================

    @Test
    public void testNullElement() {
        // null 插入
        assertTrue("add(null) 应返回 true", set.add(null));
        assertTrue("应包含 null", set.contains(null));
        assertEquals(1, set.size());

        // null 重复插入
        assertFalse("重复 add(null) 应返回 false", set.add(null));
        assertEquals(1, set.size());

        // null 与普通元素共存
        set.add("hello");
        set.add("world");
        assertEquals(3, set.size());
        assertTrue("仍应包含 null", set.contains(null));
        assertTrue(set.contains("hello"));
        assertTrue(set.contains("world"));

        // 删除 null
        assertTrue("remove(null) 应返回 true", set.remove(null));
        assertFalse("删除后不应包含 null", set.contains(null));
        assertEquals(2, set.size());

        // 再次删除 null
        assertFalse("再次 remove(null) 应返回 false", set.remove(null));
    }

    // ==================== 4. 哈希冲突（拉链） ====================

    @Test
    public void testHashCollision() {
        // 默认容量 16，hashCode % 16 相等的 key 会落入同一桶
        // Integer.hashCode() 返回自身值，故 0, 16, 32 都在 0 号桶
        MyHashSet<Integer> intSet = new MyHashSet<>();

        assertTrue(intSet.add(0));
        assertTrue(intSet.add(16));
        assertTrue(intSet.add(32));
        assertEquals("三个元素应在同一桶中", 3, intSet.size());

        // 验证都能正确查找
        assertTrue(intSet.contains(0));
        assertTrue(intSet.contains(16));
        assertTrue(intSet.contains(32));
        assertFalse(intSet.contains(48));

        // 删除中间元素（16）
        assertTrue(intSet.remove(16));
        assertEquals(2, intSet.size());
        assertTrue(intSet.contains(0));
        assertTrue(intSet.contains(32));
        assertFalse(intSet.contains(16));

        // 删除头元素（0）
        assertTrue(intSet.remove(0));
        assertEquals(1, intSet.size());
        assertTrue(intSet.contains(32));

        // 删除尾元素（32）
        assertTrue(intSet.remove(32));
        assertEquals(0, intSet.size());
        assertTrue(intSet.isEmpty());
    }

    // ==================== 5. 扩容 + rehash ====================

    @Test
    public void testResizeAndRehash() {
        // 默认容量 16，负载因子 0.75 → 阈值 12
        assertEquals("初始容量 16", 16, set.capacity());

        // 插入 12 个元素，刚好达到阈值
        for (int i = 0; i < 12; i++) {
            assertTrue(set.add("key" + i));
        }
        assertEquals(12, set.size());
        assertEquals("容量仍为 16", 16, set.capacity());

        // 第 13 个元素触发扩容
        assertTrue(set.add("key12"));
        assertEquals(13, set.size());
        assertEquals("扩容后容量应为 32", 32, set.capacity());

        // 验证扩容后所有数据完整
        for (int i = 0; i <= 12; i++) {
            assertTrue("应包含 key" + i, set.contains("key" + i));
        }

        // 触发二次扩容：插入到 size = 24，再 +1 触发
        for (int i = 13; i < 24; i++) {
            set.add("key" + i);
        }
        // 24 个元素，阈值 = 32 * 0.75 = 24
        set.add("key24");   // 触发扩容 → 64
        assertEquals(25, set.size());
        assertEquals("二次扩容后容量应为 64", 64, set.capacity());

        // 所有数据仍正确
        for (int i = 0; i <= 24; i++) {
            assertTrue("扩容后应包含 key" + i, set.contains("key" + i));
        }
    }

    // ==================== 6. 边界条件 ====================

    @Test
    public void testEdgeCases() {
        // 自定义容量为 1
        MyHashSet<Integer> tiny = new MyHashSet<>(1);
        assertEquals(1, tiny.capacity());

        assertTrue(tiny.add(1));
        assertTrue(tiny.add(2));  // size=2，threshold=0，触发扩容到 2

        assertTrue("扩容后数据完整", tiny.contains(1));
        assertTrue("扩容后数据完整", tiny.contains(2));

        // 空集合的各种操作
        MyHashSet<String> empty = new MyHashSet<>();
        assertFalse("空集合 contains 返回 false", empty.contains("any"));
        assertFalse("空集合 remove 返回 false", empty.remove("any"));
        assertEquals(0, empty.size());
        assertTrue("空集合 isEmpty", empty.isEmpty());
    }

    // ==================== 7. 大量元素 ====================

    @Test
    public void testManyElements() {
        final int N = 1000;
        for (int i = 0; i < N; i++) {
            assertTrue("add(" + i + ") 应返回 true", set.add("element" + i));
        }
        assertEquals(N, set.size());

        // 验证全部存在
        for (int i = 0; i < N; i++) {
            assertTrue(set.contains("element" + i));
        }

        // 验证重复插入
        for (int i = 0; i < N; i++) {
            assertFalse(set.add("element" + i));
        }
        assertEquals(N, set.size());

        // 删除一半
        for (int i = 0; i < N; i += 2) {
            assertTrue(set.remove("element" + i));
        }
        assertEquals(N / 2, set.size());

        // 验证删除与保留
        for (int i = 0; i < N; i++) {
            if (i % 2 == 0) {
                assertFalse(set.contains("element" + i));
            } else {
                assertTrue(set.contains("element" + i));
            }
        }
    }
}
