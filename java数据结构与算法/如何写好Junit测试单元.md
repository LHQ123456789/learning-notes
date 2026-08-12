# 如何写好一个 JUnit 测试单元

## 一、基本原则

### 1.1 AAA 模式

每个测试用例应遵循 **Arrange → Act → Assert** 三步结构：

| 阶段 | 含义 | 说明 |
|------|------|------|
| **Arrange** | 准备 | 创建对象、初始化测试数据 |
| **Act** | 执行 | 调用被测试的方法 |
| **Assert** | 断言 | 验证结果是否符合预期 |

```java
@Test
public void testAdd() {
    // Arrange
    ArrayList<Integer> list = new ArrayList<>();

    // Act
    list.add(10);

    // Assert
    assertEquals(1, list.size());
    assertEquals(Integer.valueOf(10), list.get(0));
}
```

### 1.2 单一职责

**一个测试方法只测一件事**，不要在一个 `@Test` 里塞入过多无关的验证逻辑。

```java
// ❌ 不好：一个方法测了添加、删除、扩容三件事
@Test
public void testEverything() { ... }

// ✅ 好：各自独立
@Test
public void testAdd() { ... }

@Test
public void testRemove() { ... }

@Test
public void testAutoExpansion() { ... }
```

### 1.3 命名清晰

方法名要能描述**测什么**和**期望什么**：

```java
// 推荐命名风格（任选一种统一即可）

// 风格 A：test + 被测方法 + 场景
testAdd_NullElement_ShouldStoreNull()
testRemove_EmptyList_ShouldThrowException()

// 风格 B：中文注释 + 简短方法名
@Test
public void testAdd()          // 基本添加
@Test
public void testAddNull()      // 添加 null
@Test
public void testAddAtInvalidIndex() // 越界插入
```

---

## 二、常用断言

### 2.1 核心断言方法

```java
import static org.junit.Assert.*;

assertEquals(expected, actual);          // 相等
assertEquals("message", expected, actual); // 带失败提示

assertTrue(condition);                    // 条件为真
assertFalse(condition);                   // 条件为假
assertNull(obj);                          // 为空
assertNotNull(obj);                       // 非空
assertSame(expected, actual);             // 同一引用
assertArrayEquals(expectedArr, actualArr); // 数组内容相等
```

### 2.2 异常断言

推荐使用 `try-catch` + `fail()` 方式验证异常：

```java
@Test
public void testGetWithNegativeIndex() {
    ArrayList<Integer> list = new ArrayList<>();
    try {
        list.get(-1);
        fail("应抛出 IndexOutOfBoundsException");  // 如果走到这里说明没抛异常，测试失败
    } catch (IndexOutOfBoundsException e) {
        // 预期行为，测试通过
    }
}
```

也可以使用 `@Test(expected = ...)`（但不够灵活，无法在异常后继续验证）：

```java
@Test(expected = IndexOutOfBoundsException.class)
public void testGetWithNegativeIndex() {
    ArrayList<Integer> list = new ArrayList<>();
    list.get(-1);
}
```

### 2.3 带失败信息

断言第三个参数写清楚**失败时显示什么**，方便快速定位问题：

```java
// ❌ 不好：失败时只知道值不对
assertEquals(10, list.capacity());

// ✅ 好：失败时一眼看懂
assertEquals("初始容量应为 10", 10, list.capacity());
```

---

## 三、测试数据准备

### 3.1 @Before / @After

用 `@Before` 统一初始化，避免每个测试方法重复写创建代码：

```java
public class ArrayListTest {

    private ArrayList<Integer> list;

    @Before
    public void setUp() {
        list = new ArrayList<>();  // 每个 @Test 执行前都会调用
    }

    @After
    public void tearDown() {
        list = null;  // 每个 @Test 执行后调用（一般不需要）
    }

    @Test
    public void testAdd() {
        list.add(1);  // list 已经是初始化好的
        ...
    }
}
```

### 3.2 工厂方法

复杂对象的创建可抽取为私有工厂方法：

```java
private ArrayList<Integer> createListWithData(int... values) {
    ArrayList<Integer> list = new ArrayList<>();
    for (int v : values) {
        list.add(v);
    }
    return list;
}

@Test
public void testRemove() {
    ArrayList<Integer> list = createListWithData(1, 2, 3);
    list.remove(1);
    assertEquals(2, list.size());
}
```

---

## 四、边界条件与特殊值

每个方法至少覆盖以下场景：

| 场景 | 示例 |
|------|------|
| **空集合** | 空列表调用 `get(0)` 应抛异常 |
| **单元素** | 只有一个元素时删除是否正常 |
| **大量数据** | 验证扩容/缩容是否正确 |
| **null** | `add(null)`、`contains(null)` 是否正常 |
| **负数索引** | `get(-1)` 是否抛异常 |
| **超限索引** | `get(size)` 是否抛异常 |
| **边界位置** | 在 `index=0` 和 `index=size` 处插入/删除 |

```java
@Test
public void testBoundaryConditions() {
    ArrayList<Integer> list = new ArrayList<>();

    // 末尾插入（index == size）
    list.add(0, 1);     // 空列表在 index=0 插入

    // 开头删除
    list.add(2);
    list.remove(0);     // 删第一个

    // null 元素
    list.add(null);
    assertTrue(list.contains(null));
}
```

---

## 五、数据完整性验证

除了验证返回值，还要验证**副作用**（side effect）：

```java
@Test
public void testRemoveAtIndex() {
    ArrayList<Integer> list = createListWithData(10, 20, 30);

    Integer removed = list.remove(1);

    // 验证返回值
    assertEquals(Integer.valueOf(20), removed);

    // 验证 size 变化
    assertEquals(2, list.size());

    // 验证其他元素位置是否正确
    assertEquals(Integer.valueOf(10), list.get(0));
    assertEquals(Integer.valueOf(30), list.get(1)); // 30 前移了一位
}
```

---

## 六、测试组织结构

### 6.1 按功能分测试类

```
src/test/
  └── ArrayListTest/
        ├── ArrayListAddTest.java        // 只测添加相关
        ├── ArrayListRemoveTest.java     // 只测删除相关
        └── ArrayListEdgeCaseTest.java   // 只测边界
```

简化版：一个测试类，用注释分隔区块：

```java
public class ArrayListTest {

    // ====== 添加操作 ======
    @Test public void testAdd() { ... }
    @Test public void testAddAtIndex() { ... }

    // ====== 删除操作 ======
    @Test public void testRemove() { ... }
    @Test public void testRemoveByValue() { ... }

    // ====== 扩容缩容 ======
    @Test public void testExpansion() { ... }
    @Test public void testShrink() { ... }

    // ====== 边界条件 ======
    @Test public void testEmptyList() { ... }
    @Test public void testNullElement() { ... }
}
```

---

## 七、常见误区

| 误区 | 说明 |
|------|------|
| 测试方法没有 `assert` | 不抛异常就算过——没用 |
| 一个测试依赖另一个测试的顺序 | JUnit 不保证执行顺序，必须彼此独立 |
| 断言后不验证副作用 | 只测返回值，不测内部状态是否正确 |
| 测试数据间互相污染 | `@Before` 必须把对象恢复到干净状态 |
| 不写失败信息 | 出问题时排查困难 |
| 只测正常路径 | 不测异常、null、越界等边界 |
| 测试代码有复杂逻辑 | 测试代码应该是"面条代码"——越直白越好 |

---

## 八、一个完整示例

```java
package ArrayListTest;

import org.junit.Test;
import org.junit.Before;
import java.util.NoSuchElementException;
import static org.junit.Assert.*;

public class ArrayListTest {

    private ArrayList<Integer> list;

    @Before
    public void setUp() {
        list = new ArrayList<>();
    }

    @Test
    public void testAddAndGet() {
        list.add(10);
        list.add(20);

        assertEquals("size 应为 2", 2, list.size());
        assertFalse("不应为空", list.isEmpty());
        assertEquals(Integer.valueOf(10), list.get(0));
        assertEquals(Integer.valueOf(20), list.get(1));
    }

    @Test
    public void testAutoExpansion() {
        assertEquals("初始容量", 10, list.capacity());

        for (int i = 0; i < 20; i++) {
            list.add(i);
        }

        assertTrue("容量应已扩容", list.capacity() >= 20);
        assertEquals("size 应为 20", 20, list.size());
    }

    @Test
    public void testRemoveAtIndex() {
        list.add(1); list.add(2); list.add(3);

        Integer removed = list.remove(1);

        assertEquals(Integer.valueOf(2), removed);
        assertEquals(2, list.size());
        assertEquals(Integer.valueOf(3), list.get(1)); // 3 前移
    }

    @Test
    public void testAutoShrink() {
        for (int i = 1; i <= 33; i++) { list.add(i); }
        assertEquals(40, list.capacity());      // 10→20→40

        for (int i = 0; i < 24; i++) { list.remove(0); }
        assertEquals(9, list.size());
        assertEquals(20, list.capacity());      // 40→20
    }

    @Test
    public void testEdgeCases() {
        // 空列表
        assertTrue(list.isEmpty());
        assertEquals(-1, list.indexOf(999));

        // null
        list.add(null);
        assertTrue(list.contains(null));

        // 越界
        try {
            list.get(100);
            fail("应抛出异常");
        } catch (IndexOutOfBoundsException e) {
            // 预期通过
        }
    }
}
```

---

## 九、检查清单

写完每个测试方法后自查：

- [ ] 是否覆盖了**正常路径**？
- [ ] 是否覆盖了**边界值**（0、1、最大值、null）？
- [ ] 是否覆盖了**异常情况**（越界、非法参数）？
- [ ] 断言是否包含了**失败信息**？
- [ ] 断言的**期望值**和**实际值**位置是否正确？`assertEquals(expected, actual)`
- [ ] 测试之间是否**相互独立**？（单独运行也能通过）
- [ ] 是否验证了**副作用**（不只是返回值，还有 size、其他元素位置等）？
