package StackTest;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 下压栈（LIFO）——基于链表实现的标准数据结构。
 * @param <Item> 栈中元素的类型
 */
public class Stack<Item> implements Iterable<Item> {
    private Node first;   // 栈顶
    private int N;        // 元素数量

    private class Node {
        Node next;
        Item item;
    }

    /** 创建一个空栈 */
    public Stack() {
        first = null;
        N = 0;
    }

    /** 栈是否为空 */
    public boolean isEmpty() {
        return first == null;   // 等价于 N == 0
    }

    /** 栈中元素个数 */
    public int size() {
        return N;
    }

    /** 压入一个元素到栈顶 */
    public void push(Item item) {
        Node oldFirst = first;
        first = new Node();
        first.item = item;
        first.next = oldFirst;
        N++;
    }

    /** 弹出并返回栈顶元素 */
    public Item pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack underflow");
        }
        Item item = first.item;
        first = first.next;
        N--;
        return item;
    }

    /** 查看栈顶元素（不弹出） */
    public Item peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack underflow");
        }
        return first.item;
    }

    /** 返回迭代器（自顶向下） */
    @Override
    public Iterator<Item> iterator() {
        return new StackIterator();
    }

    private class StackIterator implements Iterator<Item> {
        private Node current = first;

        @Override
        public boolean hasNext() {
            return current != null;
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
}
