package ArrayListTest;

import org.junit.Test;
import org.junit.Before;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * 动态数组 ArrayList 的 JUnit 测试用例
 */
public class ArrayListTest {

    private ArrayList<Integer> list;

    @Before
    public void setUp() {
        list = new ArrayList<Integer>();
    }

    // ==================== 1. 测试基本添加、查询、修改操作 ====================

    @Test
    public void testAddGetAndSet() {
        // 末尾添加
        list.add(10);
        list.add(20);
        list.add(30);

        assertEquals("size 应为 3", 3, list.size());
        assertFalse("列表不应为空", list.isEmpty());
        assertTrue("应包含 20", list.contains(20));
        assertFalse("不应包含 99", list.contains(99));

        // 按索引获取
        assertEquals("index=0 应为 10", Integer.valueOf(10), list.get(0));
        assertEquals("index=2 应为 30", Integer.valueOf(30), list.get(2));

        // 修改元素
        Integer old = list.set(1, 200);
        assertEquals("旧值应为 20", Integer.valueOf(20), old);
        assertEquals("新值应为 200", Integer.valueOf(200), list.get(1));
    }

    // ==================== 2. 测试指定位置插入与删除 ====================

    @Test
    public void testInsertAndRemoveAtIndex() {
        // 准备数据
        list.add(1);   // [0]
        list.add(2);   // [1]
        list.add(3);   // [2]

        // 在中间插入
        list.add(1, 99);    // [0]=1, [1]=99, [2]=2, [3]=3
        assertEquals(Integer.valueOf(99), list.get(1));
        assertEquals(Integer.valueOf(2), list.get(2));
        assertEquals(4, list.size());

        // 在末尾插入（index == size）
        list.add(4, 999);   // [0]=1, [1]=99, [2]=2, [3]=3, [4]=999
        assertEquals(Integer.valueOf(999), list.get(4));

        // 按索引删除
        Integer removed = list.remove(1);   // 删除 99
        assertEquals("删除的应为 99", Integer.valueOf(99), removed);
        assertEquals("size 应变回 4", 4, list.size());
        assertEquals("删除后 index=1 应为 2", Integer.valueOf(2), list.get(1));
    }

    // ==================== 3. 测试自动扩容 ====================

    @Test
    public void testAutoExpansion() {
        // 默认容量为 10，添加 20 个元素触发扩容
        assertEquals("初始容量应为 10", 10, list.capacity());

        for (int i = 0; i < 20; i++) {
            list.add(i);
            assertEquals(i + 1, list.size());
        }

        // 经过扩容，容量应为 20（10→20）或 40（20→40）
        assertTrue("容量应 >= 20", list.capacity() >= 20);
        assertEquals("size 应为 20", 20, list.size());

        // 验证数据完整性
        for (int i = 0; i < 20; i++) {
            assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    // ==================== 4. 测试自动缩容 ====================

    @Test
    public void testAutoShrink() {
        // 添加 33 个元素（扩容后容量为 40）
        for (int i = 1; i <= 33; i++) {
            list.add(i);
        }
        int capacityBefore = list.capacity(); // 40
        assertEquals(40, capacityBefore);

        // 删除到只剩 9 个元素：33 - 24 = 9 < 40/4 = 10，触发缩容
        // 缩容条件：N == data.length / 4
        for (int i = 0; i < 24; i++) {
            list.remove(0);
        }
        assertEquals(9, list.size());

        // 容量应缩小到 20（40/2）
        assertEquals("容量应从 40 缩至 20", 20, list.capacity());

        // 验证剩余数据正确（最后 9 个元素：25..33）
        for (int i = 0; i < 9; i++) {
            assertEquals(Integer.valueOf(25 + i), list.get(i));
        }
    }

    // ==================== 5. 测试边界条件与异常 ====================

    @Test
    public void testEdgeCasesAndExceptions() {
        // 空列表
        assertTrue("新列表应为空", list.isEmpty());
        assertEquals(0, list.size());
        assertFalse("空列表不应包含 null", list.contains(null));

        // indexOf / lastIndexOf
        assertEquals(-1, list.indexOf(999));
        assertEquals(-1, list.lastIndexOf(999));

        // 删除不存在的元素
        assertFalse("删除不存在的元素应返回 false", list.remove(Integer.valueOf(99)));

        // null 元素支持
        list.add(null);
        list.add(1);
        assertTrue("应包含 null", list.contains(null));
        assertEquals(0, list.indexOf(null));
        list.remove(0);
        assertFalse("null 应已被删除", list.contains(null));

        // 越界异常 - get
        try {
            list.get(100);
            fail("应抛出 IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // 预期行为
        }

        // 越界异常 - 负数索引插入
        try {
            list.add(-1, 5);
            fail("应抛出 IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // 预期行为
        }

        // 越界异常 - 删除时
        try {
            list.remove(list.size());
            fail("应抛出 IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // 预期行为
        }

        // 容量为 0 构造
        try {
            new ArrayList<Integer>(0);
            fail("应抛出 IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // 预期行为
        }

        // 迭代器 —— 此时列表中已有 [1]（null 测试残留）
        list.add(100);
        list.add(200);
        Iterator<Integer> it = list.iterator();
        assertTrue("应有下一个", it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());      // 残留元素
        assertEquals(Integer.valueOf(100), it.next());
        assertEquals(Integer.valueOf(200), it.next());
        assertFalse("不应再有下一个", it.hasNext());

        // 迭代器耗尽抛异常
        try {
            it.next();
            fail("应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 根据值删除
        list.add(300);
        assertTrue("删除存在的值应返回 true", list.remove(Integer.valueOf(300)));
        assertEquals(3, list.size());   // 之前有 3 个元素：1, 100, 200

        // 清空
        list.clear();
        assertTrue("清空后应为空", list.isEmpty());
        assertEquals(0, list.size());
        assertEquals(10, list.capacity()); // 恢复到默认容量
    }
}



