package DoubleLinkedListTest;

import org.junit.Test;
import org.junit.Before;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * 双向链表 DoubleLinkedList 的 JUnit 测试用例
 */
public class DoubleLinkedListTest {

    private DoubleLinkedList<Integer> list;

    @Before
    public void setUp() {
        list = new DoubleLinkedList<Integer>();
    }

    // ==================== 1. 测试头部添加、尾部添加与查询 ====================

    @Test
    public void testAddFirstAndAddLast() {
        // 从头部添加
        list.addFirst(20);
        list.addFirst(10);    // 此时顺序：10 -> 20

        assertEquals("size 应为 2", 2, list.size());
        assertFalse("列表不应为空", list.isEmpty());
        assertEquals("头部应为 10", Integer.valueOf(10), list.getFirst());
        assertEquals("尾部应为 20", Integer.valueOf(20), list.getLast());

        // 从尾部添加
        list.addLast(30);
        list.addLast(40);    // 此时顺序：10 -> 20 -> 30 -> 40

        assertEquals("size 应为 4", 4, list.size());
        assertEquals("头部仍应为 10", Integer.valueOf(10), list.getFirst());
        assertEquals("尾部应为 40", Integer.valueOf(40), list.getLast());

        // 验证完整顺序
        assertEquals("index=0 应为 10", Integer.valueOf(10), list.get(0));
        assertEquals("index=1 应为 20", Integer.valueOf(20), list.get(1));
        assertEquals("index=2 应为 30", Integer.valueOf(30), list.get(2));
        assertEquals("index=3 应为 40", Integer.valueOf(40), list.get(3));
    }

    // ==================== 2. 测试删除头部和尾部 ====================

    @Test
    public void testRemoveFirstAndRemoveLast() {
        // 准备数据：10 -> 20 -> 30 -> 40
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);
        list.addLast(40);
        assertEquals(4, list.size());

        // 删除头部
        Integer first = list.removeFirst();
        assertEquals("删除的头部应为 10", Integer.valueOf(10), first);
        assertEquals("size 应变 3", 3, list.size());
        assertEquals("新头部应为 20", Integer.valueOf(20), list.getFirst());

        // 删除尾部
        Integer last = list.removeLast();
        assertEquals("删除的尾部应为 40", Integer.valueOf(40), last);
        assertEquals("size 应变 2", 2, list.size());
        assertEquals("新尾部应为 30", Integer.valueOf(30), list.getLast());

        // 继续删除到空
        list.removeFirst();  // 删除 20
        list.removeLast();   // 删除 30
        assertTrue("列表应为空", list.isEmpty());
        assertEquals("size 应为 0", 0, list.size());
    }

    // ==================== 3. 测试按值删除和查找 ====================

    @Test
    public void testRemoveByValueAndContains() {
        // 准备数据：10 -> 20 -> 30 -> 20 -> 40
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);
        list.addLast(20);   // 重复元素
        list.addLast(40);
        assertEquals(5, list.size());

        // 测试 contains
        assertTrue("应包含 20", list.contains(20));
        assertTrue("应包含 40", list.contains(40));
        assertFalse("不应包含 99", list.contains(99));

        // 测试 indexOf
        assertEquals("20 首次出现位置应为 1", 1, list.indexOf(20));

        // 删除第一个匹配的 20
        boolean removed = list.remove(Integer.valueOf(20));
        assertTrue("删除应成功", removed);
        assertEquals("size 应变 4", 4, list.size());
        assertTrue("仍应包含另一个 20", list.contains(20));

        // 验证删除后顺序：10 -> 30 -> 20 -> 40
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(30), list.get(1));
        assertEquals(Integer.valueOf(20), list.get(2));
        assertEquals(Integer.valueOf(40), list.get(3));

        // 删除不存在的元素
        assertFalse("删除不存在的元素应返回 false", list.remove(Integer.valueOf(99)));

        // 删除所有元素直到为空
        list.remove(Integer.valueOf(10));
        list.remove(Integer.valueOf(30));
        list.remove(Integer.valueOf(20));
        list.remove(Integer.valueOf(40));
        assertTrue("全部删除后应为空", list.isEmpty());
    }

    // ==================== 4. 测试 null 元素支持 ====================

    @Test
    public void testNullElement() {
        list.addFirst(null);
        list.addLast(1);
        list.addLast(null);
        list.addLast(2);

        assertEquals("size 应为 4", 4, list.size());
        assertTrue("应包含 null", list.contains(null));

        // null 首次出现位置应为 0
        assertEquals("null 首次索引应为 0", 0, list.indexOf(null));

        // 验证顺序
        assertNull("index=0 应为 null", list.get(0));
        assertEquals(Integer.valueOf(1), list.get(1));
        assertNull("index=2 应为 null", list.get(2));
        assertEquals(Integer.valueOf(2), list.get(3));

        // 删除第一个 null
        assertTrue("删除 null 应成功", list.remove(null));
        assertEquals("size 应变 3", 3, list.size());
        assertEquals("删除第一个 null 后头部应为 1", Integer.valueOf(1), list.getFirst());

        // 第二个 null 仍在
        assertTrue("仍应包含 null", list.contains(null));
        assertNull("index=1 应为 null", list.get(1));
    }

    // ==================== 5. 测试边界条件与异常 ====================

    @Test
    public void testEdgeCasesAndExceptions() {
        // 空列表状态
        assertTrue("新列表应为空", list.isEmpty());
        assertEquals("size 应为 0", 0, list.size());
        assertFalse("空列表不应包含任何元素", list.contains(1));
        assertEquals("空列表 indexOf 应返回 -1", -1, list.indexOf(1));

        // 空列表 removeFirst 应抛异常
        try {
            list.removeFirst();
            fail("空列表 removeFirst 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空列表 removeLast 应抛异常
        try {
            list.removeLast();
            fail("空列表 removeLast 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空列表 getFirst 应抛异常
        try {
            list.getFirst();
            fail("空列表 getFirst 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空列表 getLast 应抛异常
        try {
            list.getLast();
            fail("空列表 getLast 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 越界访问
        list.addLast(100);
        try {
            list.get(5);
            fail("越界 get 应抛出 IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // 预期行为
        }

        try {
            list.get(-1);
            fail("负数索引 get 应抛出 IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // 预期行为
        }

        // 单个元素的添加和删除
        assertEquals(Integer.valueOf(100), list.removeFirst());
        assertTrue("删除唯一元素后应为空", list.isEmpty());

        // clear 测试
        list.addFirst(1);
        list.addFirst(2);
        list.addFirst(3);
        list.clear();
        assertTrue("clear 后应为空", list.isEmpty());
        assertEquals("clear 后 size 应为 0", 0, list.size());
    }

    // ==================== 6. 测试迭代器 ====================

    @Test
    public void testIterator() {
        // 准备数据：10 -> 20 -> 30
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        // 基本迭代
        Iterator<Integer> it = list.iterator();
        assertTrue("应有下一个", it.hasNext());
        assertEquals(Integer.valueOf(10), it.next());
        assertTrue("应有下一个", it.hasNext());
        assertEquals(Integer.valueOf(20), it.next());
        assertTrue("应有下一个", it.hasNext());
        assertEquals(Integer.valueOf(30), it.next());
        assertFalse("不应再有下一个", it.hasNext());

        // 迭代器耗尽应抛异常
        try {
            it.next();
            fail("耗尽后调用 next 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空列表迭代器
        DoubleLinkedList<String> emptyList = new DoubleLinkedList<String>();
        Iterator<String> emptyIt = emptyList.iterator();
        assertFalse("空列表迭代器 hasNext 应为 false", emptyIt.hasNext());
        try {
            emptyIt.next();
            fail("空列表迭代器 next 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // for-each 循环
        int sum = 0;
        for (int val : list) {
            sum += val;
        }
        assertEquals("for-each 求和应为 60", 60, sum);
    }

    // ==================== 7. 测试大量元素的稳定性 ====================

    @Test
    public void testLargeDataSet() {
        final int COUNT = 1000;

        // 从头部大量添加
        for (int i = 0; i < COUNT; i++) {
            list.addFirst(i);
        }
        assertEquals("size 应为 1000", COUNT, list.size());
        assertEquals("头部应为 999", Integer.valueOf(COUNT - 1), list.getFirst());
        assertEquals("尾部应为 0", Integer.valueOf(0), list.getLast());

        // 从头部全部删除
        for (int i = COUNT - 1; i >= 0; i--) {
            assertEquals(Integer.valueOf(i), list.removeFirst());
        }
        assertTrue("全部删除后应为空", list.isEmpty());

        // 从尾部大量添加
        for (int i = 0; i < COUNT; i++) {
            list.addLast(i);
        }
        assertEquals(COUNT, list.size());

        // 从尾部全部删除
        for (int i = COUNT - 1; i >= 0; i--) {
            assertEquals(Integer.valueOf(i), list.removeLast());
        }
        assertTrue("再次全部删除后应为空", list.isEmpty());
    }
}
