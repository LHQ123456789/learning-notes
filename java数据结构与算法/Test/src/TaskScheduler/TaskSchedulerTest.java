package TaskScheduler;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TaskScheduler 的 JUnit 测试。
 * 覆盖：按优先级执行、ID 取消（惰性删除）、同优先级 tiebreak、重复 ID、边界条件。
 */
public class TaskSchedulerTest {

    // ==================== 1. 添加 + 按优先级执行 ====================

    @Test
    public void testExecuteByPriority() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("A", 3, "低");
        s.addTask("B", 5, "高");
        s.addTask("C", 1, "最低");
        s.addTask("D", 4, "中");

        assertEquals(4, s.size());
        // 优先级从高到低：B(5) -> D(4) -> A(3) -> C(1)
        assertEquals("B", s.executeNext().getId());
        assertEquals("D", s.executeNext().getId());
        assertEquals("A", s.executeNext().getId());
        assertEquals("C", s.executeNext().getId());

        assertTrue(s.isEmpty());
        assertNull(s.executeNext());   // 空调度器返回 null
    }

    // ==================== 2. 同优先级：按 ID 升序 tiebreak ====================

    @Test
    public void testTieBreakByPriority() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("X", 5, "x");
        s.addTask("A", 5, "a");
        s.addTask("M", 5, "m");

        // 优先级相同，ID 小的先出
        assertEquals("A", s.executeNext().getId());
        assertEquals("M", s.executeNext().getId());
        assertEquals("X", s.executeNext().getId());
    }

    // ==================== 3. 按 ID 取消（惰性删除） ====================

    @Test
    public void testCancelById() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("A", 1, "a");
        s.addTask("B", 2, "b");
        s.addTask("C", 3, "c");

        assertTrue(s.cancel("B"));
        assertEquals(2, s.size());               // BST 已删 B

        // 执行：B 已被取消，应跳过
        assertEquals("C", s.executeNext().getId());
        assertEquals("A", s.executeNext().getId());
        assertNull(s.executeNext());
    }

    /** 取消优先级最高的任务，下一个执行的应是次高优先级 */
    @Test
    public void testCancelHighestPriority() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("top", 100, "最高");
        s.addTask("mid", 50, "次高");

        assertTrue(s.cancel("top"));
        assertEquals("mid", s.executeNext().getId());
        assertNull(s.executeNext());
    }

    /** 取消不存在的 ID 返回 false */
    @Test
    public void testCancelNonexistent() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("A", 1, "a");

        assertFalse(s.cancel("no-such-id"));
        assertEquals(1, s.size());

        // 同一个任务不能取消两次
        assertTrue(s.cancel("A"));
        assertFalse(s.cancel("A"));
    }

    // ==================== 4. peek 不删除 ====================

    @Test
    public void testPeekNextDoesNotRemove() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("A", 1, "a");
        s.addTask("B", 9, "b");

        assertEquals("B", s.peekNext().getId());
        assertEquals("B", s.peekNext().getId()); // 再次 peek 仍是 B
        assertEquals(2, s.size());               // 未被删除
    }

    // ==================== 5. 重复 ID / 边界 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateIdRejected() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("same", 1, "first");
        s.addTask("same", 2, "second");   // 应抛异常
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyIdRejected() {
        new Task("", 1, "x");
    }

    @Test
    public void testEmptyScheduler() {
        TaskScheduler s = new TaskScheduler();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
        assertNull(s.peekNext());
        assertNull(s.executeNext());
        assertNull(s.findById("anything"));
    }

    // ==================== 6. 综合场景：边添加边取消边执行 ====================

    @Test
    public void testMixedScenario() {
        TaskScheduler s = new TaskScheduler();
        s.addTask("job-1", 10, "紧急");
        s.addTask("job-2", 20, "更紧急");
        s.addTask("job-3", 5, "普通");

        s.cancel("job-2");                        // 取消最高优先级
        assertEquals("job-1", s.executeNext().getId());

        s.addTask("job-4", 30, "最新紧急");
        s.cancel("job-3");                        // 取消低优先级

        assertEquals("job-4", s.executeNext().getId());
        assertNull(s.executeNext());              // job-3 已取消，被跳过
        assertTrue(s.isEmpty());
    }

    /** 大量随机任务 + 部分取消后，执行序列应始终按优先级非递增 */
    @Test
    public void testStressPriorityOrder() {
        TaskScheduler s = new TaskScheduler();
        int n = 500;
        for (int i = 0; i < n; i++) {
            s.addTask("T" + i, (i * 7919) % 1000, "task" + i);
        }
        // 取消偶数 ID
        for (int i = 0; i < n; i += 2) {
            s.cancel("T" + i);
        }

        int last = Integer.MAX_VALUE;
        List<String> executed = new ArrayList<>();
        Task t;
        while ((t = s.executeNext()) != null) {
            assertTrue("优先级应非递增", t.getPriority() <= last);
            last = t.getPriority();
            executed.add(t.getId());
        }

        // 偶数 ID 全部被取消，不应出现在执行结果中
        for (String id : executed) {
            int num = Integer.parseInt(id.substring(1));
            assertTrue("偶数 ID 不应被执行", num % 2 == 1);
        }
    }
}
