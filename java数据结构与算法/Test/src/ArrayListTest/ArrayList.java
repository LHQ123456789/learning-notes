package ArrayListTest;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 动态数组 —— 基于数组扩容/缩容实现的标准数据结构。
 * 底层为 Object[]，当元素数量达到容量时自动扩容为 2 倍，低于 1/4 时缩容为 1/2。
 */
public class ArrayList<Item> implements Iterable<Item> {
    private static final int DEFAULT_CAPACITY = 10;  // 默认容量，避免频繁扩容

    private Item[] data;   // 底层数组
    private int N;         // 当前元素数量

    /** 创建一个默认容量（10）的空数组 */
    @SuppressWarnings("unchecked")
    public ArrayList() {
        data = (Item[]) new Object[DEFAULT_CAPACITY];
        N = 0;
    }

    /** 创建一个指定容量的空数组 */
    @SuppressWarnings("unchecked")
    public ArrayList(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive: " + capacity);
        }
        data = (Item[]) new Object[capacity];
        N = 0;
    }


    /** 元素个数 */
    public int size() {
        return N;
    }

    /** 是否为空 */
    public boolean isEmpty() {
        return N == 0;
    }

    /** 当前内部数组容量 */
    public int capacity() {
        return data.length;
    }

    // ---------- 增 ----------

    /** 在末尾添加元素 */
    public void add(Item item) {
        if (N == data.length) {
            resize(2 * data.length);
        }
        data[N++] = item;
    }

    /** 在指定位置插入元素 */
    public void add(int index, Item item) {
        checkIndexForAdd(index);
        if (N == data.length) {
            resize(2 * data.length);
        }
        // 将 index 及之后的元素整体后移一位
        System.arraycopy(data, index, data, index + 1, N - index);
        data[index] = item;
        N++;
    }

    // ---------- 删 ----------

    /** 删除并返回指定位置的元素 */
    public Item remove(int index) {
        checkIndex(index);
        Item removed = data[index];
        // 将 index 之后的元素整体前移一位
        System.arraycopy(data, index + 1, data, index, N - index - 1);
        data[--N] = null;   // 避免对象游离

        // 负载因子低于 1/4 时缩容
        if (N > 0 && N == data.length / 4) {
            resize(data.length / 2);
        }
        return removed;
    }

    /** 删除第一个匹配的元素，返回是否删除成功 */
    public boolean remove(Item item) {
        int i = indexOf(item);
        if (i == -1) {
            return false;
        }
        remove(i);
        return true;
    }

    /** 清空所有元素 */
    @SuppressWarnings("unchecked")
    public void clear() {
        data = (Item[]) new Object[DEFAULT_CAPACITY];
        N = 0;
    }

    // ---------- 改 ----------

    /** 将指定位置的元素替换为新值，返回旧值 */
    public Item set(int index, Item item) {
        checkIndex(index);
        Item old = data[index];
        data[index] = item;
        return old;
    }

    // ---------- 查 ----------

    /** 获取指定位置的元素 */
    public Item get(int index) {
        checkIndex(index);
        return data[index];
    }

    /** 是否包含该元素 */
    public boolean contains(Item item) {
        return indexOf(item) != -1;
    }

    /** 查找元素首次出现的位置，未找到返回 -1 */
    public int indexOf(Item item) {
        if (item == null) {
            for (int i = 0; i < N; i++) {
                if (data[i] == null) return i;
            }
        } else {
            for (int i = 0; i < N; i++) {
                if (item.equals(data[i])) return i;
            }
        }
        return -1;
    }

    /** 查找元素最后一次出现的位置 */
    public int lastIndexOf(Item item) {
        if (item == null) {
            for (int i = N - 1; i >= 0; i--) {
                if (data[i] == null) return i;
            }
        } else {
            for (int i = N - 1; i >= 0; i--) {
                if (item.equals(data[i])) return i;
            }
        }
        return -1;
    }

    // ---------- 内部工具 ----------

    /** 调整底层数组大小 */
    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Item[] newData = (Item[]) new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, N);// 将原数组拷贝到新数组
        data = newData;
    }

    /** 检查索引是否越界（访问/修改/删除用） */
    private void checkIndex(int index) {
        if (index < 0 || index >= N) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + N);
        }
    }

    /** 检查索引是否越界（插入用，允许 index == N 即末尾插入） */
    private void checkIndexForAdd(int index) {
        if (index < 0 || index > N) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + N);
        }
    }

    // ---------- 迭代器 ----------

    @Override
    public Iterator<Item> iterator() {
        return new ArrayListIterator();
    }

    private class ArrayListIterator implements Iterator<Item> {
        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < N;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return data[cursor++];
        }
    }
}
