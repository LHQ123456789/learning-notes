package DoubleLinkedListTest;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 双向链表 —— 基于哨兵节点实现的双向循环链表。
 * 哨兵节点的 next 指向第一个真实节点，prev 指向最后一个真实节点；
 * 空链表时哨兵节点的 next 和 prev 都指向自己。
 */
public class DoubleLinkedList<Item> implements Iterable<Item> {
    private Node sentinel;   // 哨兵节点
    private int N;           // 当前元素数量

    private class Node {
        Item item;
        Node next;
        Node prev;
    }

    /** 创建一个空的双向链表 */
    public DoubleLinkedList() {
        sentinel = new Node();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        N = 0;
    }

    /** 链表是否为空 */
    public boolean isEmpty() {
        return N == 0;
    }

    /** 链表元素个数 */
    public int size() {
        return N;
    }

    // ---------- 增 ----------

    /** 在头部添加元素 */
    public void addFirst(Item item) {
        Node newNode = new Node();
        newNode.item = item;
        newNode.next = sentinel.next;
        newNode.prev = sentinel;
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        N++;
    }

    /** 在尾部添加元素 */
    public void addLast(Item item) {
        Node newNode = new Node();
        newNode.item = item;
        newNode.next = sentinel;
        newNode.prev = sentinel.prev;
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        N++;
    }

    // ---------- 删 ----------

    /** 删除并返回头部元素 */
    public Item removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("链表为空，无法删除头部元素");
        }
        Node node = sentinel.next;
        sentinel.next = node.next;
        node.next.prev = sentinel;
        N--;
        return node.item;
    }

    /** 删除并返回尾部元素 */
    public Item removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("链表为空，无法删除尾部元素");
        }
        Node node = sentinel.prev;
        sentinel.prev = node.prev;
        node.prev.next = sentinel;
        N--;
        return node.item;
    }

    /** 删除第一个匹配的元素，返回是否删除成功 */
    public boolean remove(Item item) {
        Node current = sentinel.next;
        while (current != sentinel) {
            if (item == null ? current.item == null : item.equals(current.item)) {
                current.prev.next = current.next;
                current.next.prev = current.prev;
                N--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** 清空链表 */
    public void clear() {
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        N = 0;
    }

    // ---------- 查 ----------

    /** 获取头部元素（不删除） */
    public Item getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("链表为空");
        }
        return sentinel.next.item;
    }

    /** 获取尾部元素（不删除） */
    public Item getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("链表为空");
        }
        return sentinel.prev.item;
    }

    /** 是否包含该元素 */
    public boolean contains(Item item) {
        Node current = sentinel.next;
        while (current != sentinel) {
            if (item == null ? current.item == null : item.equals(current.item)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /** 查找元素首次出现的位置，未找到返回 -1 */
    public int indexOf(Item item) {
        Node current = sentinel.next;
        int index = 0;
        while (current != sentinel) {
            if (item == null ? current.item == null : item.equals(current.item)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /** 获取指定位置的元素 */
    public Item get(int index) {
        if (index < 0 || index >= N) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + N);
        }
        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    // ---------- 迭代器 ----------

    @Override
    public Iterator<Item> iterator() {
        return new DoubleLinkedListIterator();
    }

    private class DoubleLinkedListIterator implements Iterator<Item> {
        private Node current = sentinel.next;

        @Override
        public boolean hasNext() {
            return current != sentinel;
        }

        @Override
        public Item next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Item item = current.item;
            current = current.next;
            return item;
        }
    }

    // ---------- 工具 ----------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node current = sentinel.next;
        while (current != sentinel) {
            sb.append(current.item);
            current = current.next;
            if (current != sentinel) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
