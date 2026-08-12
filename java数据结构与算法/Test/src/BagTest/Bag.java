package BagTest;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Bag<Item> implements Iterable<Item> {
    private Node first;
    private int N;

    // 链表节点
    private class Node {
        Item item;
        Node next;
    }

    /**
     * 创建一个空的背包
     */
    public Bag() {
        first = null;
        N = 0;
    }

    /**
     * 检查背包是否为空
     */
    public boolean isEmpty() {
        return first == null;
    }

    /**
     * 返回背包中的元素数量
     */
    public int size() {
        return N;
    }

    /**
     * 添加一个元素
     */
    public void add(Item item) {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.next = oldfirst;
        N++;
    }

    /**
     * 返回迭代器
     */
    @Override
    public Iterator<Item> iterator() {
        return new ListIterator();
    }

    // 自定义迭代器实现
    private class ListIterator implements Iterator<Item> {
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
