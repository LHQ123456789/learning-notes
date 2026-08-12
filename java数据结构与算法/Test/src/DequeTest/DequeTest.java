package DequeTest;

import org.junit.Test;
import org.junit.Before;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * 双向队列 Deque 的 JUnit 测试用例
 */
public class DequeTest {

    private Deque<Integer> deque;

    @Before
    public void setUp() {
        deque = new Deque<Integer>();
    }

    // ==================== 1. 测试头部增删 ====================

    @Test
    public void testAddRemoveFirst() {
        // 头部添加三个元素：30 -> 20 -> 10
        deque.addFirst(10);
        deque.addFirst(20);
        deque.addFirst(30);

        assertEquals("size 应为 3", 3, deque.size());
        assertFalse("队列不应为空", deque.isEmpty());
        assertEquals("头部应为 30", Integer.valueOf(30), deque.getFirst());
        assertEquals("尾部应为 10", Integer.valueOf(10), deque.getLast());

        // 依次从头部删除
        assertEquals("removeFirst 应返回 30", Integer.valueOf(30), deque.removeFirst());
        assertEquals("removeFirst 应返回 20", Integer.valueOf(20), deque.removeFirst());
        assertEquals("removeFirst 应返回 10", Integer.valueOf(10), deque.removeFirst());
        assertTrue("全部删除后应为空", deque.isEmpty());
        assertEquals("size 应为 0", 0, deque.size());
    }

    // ==================== 2. 测试尾部增删 ====================

    @Test
    public void testAddRemoveLast() {
        // 尾部添加三个元素：10 -> 20 -> 30
        deque.addLast(10);
        deque.addLast(20);
        deque.addLast(30);

        assertEquals("size 应为 3", 3, deque.size());
        assertEquals("头部应为 10", Integer.valueOf(10), deque.getFirst());
        assertEquals("尾部应为 30", Integer.valueOf(30), deque.getLast());

        // 依次从尾部删除
        assertEquals("removeLast 应返回 30", Integer.valueOf(30), deque.removeLast());
        assertEquals("removeLast 应返回 20", Integer.valueOf(20), deque.removeLast());
        assertEquals("removeLast 应返回 10", Integer.valueOf(10), deque.removeLast());
        assertTrue("全部删除后应为空", deque.isEmpty());
    }

    // ==================== 3. 测试混合操作（兼作栈和队列） ====================

    @Test
    public void testMixedOperations() {
        // ---------- 当作栈 (LIFO) 使用：数据入头部，也从头部出 ----------
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addFirst(3);    // 栈顺序：头部 3 -> 2 -> 1 尾部

        assertEquals(Integer.valueOf(3), deque.removeFirst()); // 后进先出
        assertEquals(Integer.valueOf(2), deque.removeFirst());
        assertEquals(Integer.valueOf(1), deque.removeFirst());
        assertTrue("栈模式清空后应为空", deque.isEmpty());

        // ---------- 当作队列 (FIFO) 使用：数据入尾部，从头部出 ----------
        deque.addLast(100);
        deque.addLast(200);
        deque.addLast(300);   // 队列顺序：头部 100 -> 200 -> 300 尾部

        assertEquals(Integer.valueOf(100), deque.removeFirst()); // 先进先出
        assertEquals(Integer.valueOf(200), deque.removeFirst());
        assertEquals(Integer.valueOf(300), deque.removeFirst());
        assertTrue("队列模式清空后应为空", deque.isEmpty());

        // ---------- 头尾交叉操作 ----------
        deque.addFirst(10);   // 头部插入
        deque.addLast(30);    // 尾部插入
        deque.addFirst(5);    // 头部插入  → 头部 5 -> 10 -> 30 尾部

        assertEquals(Integer.valueOf(5), deque.getFirst());
        assertEquals(Integer.valueOf(30), deque.getLast());

        assertEquals(Integer.valueOf(5), deque.removeFirst());  // 从左出
        assertEquals(Integer.valueOf(30), deque.removeLast());  // 从右出
        assertEquals("应交错剩一个", 1, deque.size());
        assertEquals(Integer.valueOf(10), deque.getFirst());
        assertEquals(Integer.valueOf(10), deque.getLast());     // 首尾是同一个
    }

    // ==================== 4. 测试边界条件与异常 ====================

    @Test
    public void testExceptions() {
        // 空队列 removeFirst
        try {
            deque.removeFirst();
            fail("空队列 removeFirst 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空队列 removeLast
        try {
            deque.removeLast();
            fail("空队列 removeLast 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空队列 getFirst
        try {
            deque.getFirst();
            fail("空队列 getFirst 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 空队列 getLast
        try {
            deque.getLast();
            fail("空队列 getLast 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // 删除再添加后应正常工作（哨兵指针不变性检查）
        deque.addFirst(42);
        assertEquals(Integer.valueOf(42), deque.removeFirst());
        assertTrue(deque.isEmpty());

        deque.addLast(99);
        assertEquals(Integer.valueOf(99), deque.removeLast());
        assertTrue(deque.isEmpty());

        // 单元素时首尾相同
        deque.addFirst(77);
        assertEquals(Integer.valueOf(77), deque.getFirst());
        assertEquals(Integer.valueOf(77), deque.getLast());
        deque.removeFirst();
        assertTrue(deque.isEmpty());
    }

    // ==================== 5. 测试 null 元素支持 ====================

    @Test
    public void testNullElement() {
        // 头部添加 null
        deque.addFirst(null);
        deque.addLast(1);
        deque.addLast(null);
        deque.addLast(2);

        assertEquals("size 应为 4", 4, deque.size());

        // 验证顺序：null -> 1 -> null -> 2
        assertNull("头部应为 null", deque.getFirst());
        assertNull("removeFirst 应返回 null", deque.removeFirst());

        assertEquals(Integer.valueOf(1), deque.getFirst());
        assertEquals(Integer.valueOf(1), deque.removeFirst());

        assertNull("removeFirst 应返回 null", deque.removeFirst());

        assertEquals(Integer.valueOf(2), deque.getFirst());
        assertEquals(Integer.valueOf(2), deque.removeLast());

        assertTrue("全部删除后应为空", deque.isEmpty());
    }

    // ==================== 6. 测试迭代器 ====================

    @Test
    public void testIterator() {
        // 准备数据：10 -> 20 -> 30
        deque.addLast(10);
        deque.addLast(20);
        deque.addLast(30);

        // 基本迭代
        Iterator<Integer> it = deque.iterator();
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

        // 空队列迭代器
        Deque<String> emptyDeque = new Deque<String>();
        Iterator<String> emptyIt = emptyDeque.iterator();
        assertFalse("空队列 hasNext 应为 false", emptyIt.hasNext());
        try {
            emptyIt.next();
            fail("空队列 next 应抛出 NoSuchElementException");
        } catch (NoSuchElementException e) {
            // 预期行为
        }

        // for-each 循环
        int sum = 0;
        for (int val : deque) {
            sum += val;
        }
        assertEquals("for-each 求和应为 60", 60, sum);
    }

    // ==================== 7. 测试大量元素 ====================

    @Test
    public void testLargeDataset() {
        final int COUNT = 1000;

        // 头部方向全部添加
        for (int i = 0; i < COUNT; i++) {
            deque.addFirst(i);
        }
        assertEquals("size 应为 1000", COUNT, deque.size());
        assertEquals("头部应为 999", Integer.valueOf(COUNT - 1), deque.getFirst());
        assertEquals("尾部应为 0", Integer.valueOf(0), deque.getLast());

        // 从尾部全部删除
        for (int i = 0; i < COUNT; i++) {
            assertEquals(Integer.valueOf(i), deque.removeLast());
        }
        assertTrue("从尾部全部删除后应为空", deque.isEmpty());

        // 尾部方向全部添加
        for (int i = 0; i < COUNT; i++) {
            deque.addLast(i);
        }
        assertEquals(COUNT, deque.size());
        assertEquals("头部应为 0", Integer.valueOf(0), deque.getFirst());
        assertEquals("尾部应为 999", Integer.valueOf(COUNT - 1), deque.getLast());

        // 从头部全部删除
        for (int i = 0; i < COUNT; i++) {
            assertEquals(Integer.valueOf(i), deque.removeFirst());
        }
        assertTrue("从头部全部删除后应为空", deque.isEmpty());

        // 头尾交替添加，头尾交替删除
        for (int i = 0; i < COUNT / 2; i++) {
            deque.addFirst(i);
            deque.addLast(COUNT - i);
        }
        assertEquals(COUNT, deque.size());

        for (int i = 0; i < COUNT / 2; i++) {
            deque.removeFirst();
            deque.removeLast();
        }
        assertTrue("交替删除后应为空", deque.isEmpty());
    }
}
