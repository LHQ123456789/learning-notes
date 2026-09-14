package BinaryTreeTest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 二叉树（Binary Tree）—— 基于链式存储的标准实现。
 *
 * 本类包含：
 *  1. 节点定义（Node 静态内部类）
 *  2. 递归遍历：前序 / 中序 / 后序
 *  3. 迭代遍历（用栈模拟递归调用过程）：前序 / 中序 / 后序
 *  4. 层序遍历（用队列，广度优先）
 *  5. 树的高度 / 节点数
 *  6. 二叉搜索树（BST）操作：put / get / remove / min / max / floor / ceiling / rank
 *
 * 说明：BST 相关方法要求节点数据可比较，故泛型约束为 T extends Comparable<T>。
 * 中序遍历一棵 BST 得到的序列必然按升序排列，测试中正是利用这一性质做校验。
 *
 * @param <T> 节点中存储数据的类型（须可比较，Comparable）
 */
public class BinaryTree<T extends Comparable<T>> {

    // ======================== 节点定义 ========================

    /**
     * 树节点：数据域 + 左右孩子指针。
     * 声明为 static 嵌套类，不隐式持有外部 BinaryTree 实例，便于独立构造节点。
     */
    public static class Node<T> {
        public T data;          // 数据域
        public Node<T> left;    // 左孩子
        public Node<T> right;   // 右孩子

        /** 构造叶子节点（无左右孩子） */
        public Node(T data) {
            this(data, null, null);
        }

        /** 构造带左右孩子的节点 */
        public Node(T data, Node<T> left, Node<T> right) {
            this.data = data;
            this.left = left;
            this.right = right;
        }
    }

    // ======================== 字段与构造 ========================

    private Node<T> root;   // 根节点

    /** 构造一棵空树 */
    public BinaryTree() {
        this.root = null;
    }

    /** 用给定根节点构造一棵树 */
    public BinaryTree(Node<T> root) {
        this.root = root;
    }

    /** 返回根节点 */
    public Node<T> getRoot() {
        return root;
    }

    // ======================== 递归遍历 ========================
    // 递归的本质：系统用「调用栈」隐式保存每一层的上下文。
    // 三种遍历的区别仅在于「访问根节点」的时机。

    /** 前序遍历（递归）：根 → 左 → 右 */
    public List<T> preorderRecursive() {
        List<T> result = new ArrayList<>();
        preorderRecursive(root, result);
        return result;
    }

    private void preorderRecursive(Node<T> node, List<T> result) {
        if (node == null) {
            return;                                    // 递归终止：越过叶子
        }
        result.add(node.data);                         // 1. 访问根
        preorderRecursive(node.left, result);          // 2. 遍历左子树
        preorderRecursive(node.right, result);         // 3. 遍历右子树
    }

    /** 中序遍历（递归）：左 → 根 → 右 */
    public List<T> inorderRecursive() {
        List<T> result = new ArrayList<>();
        inorderRecursive(root, result);
        return result;
    }

    private void inorderRecursive(Node<T> node, List<T> result) {
        if (node == null) {
            return;
        }
        inorderRecursive(node.left, result);           // 1. 遍历左子树
        result.add(node.data);                         // 2. 访问根
        inorderRecursive(node.right, result);          // 3. 遍历右子树
    }

    /** 后序遍历（递归）：左 → 右 → 根 */
    public List<T> postorderRecursive() {
        List<T> result = new ArrayList<>();
        postorderRecursive(root, result);
        return result;
    }

    private void postorderRecursive(Node<T> node, List<T> result) {
        if (node == null) {
            return;
        }
        postorderRecursive(node.left, result);         // 1. 遍历左子树
        postorderRecursive(node.right, result);        // 2. 遍历右子树
        result.add(node.data);                         // 3. 访问根
    }

    // ======================== 迭代遍历（用栈模拟） ========================
    // 用显式栈替代系统调用栈，手动管理「待处理的节点」。

    /** 前序遍历（迭代）：根 → 左 → 右 */
    public List<T> preorderIterative() {
        List<T> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<Node<T>> stack = new ArrayDeque<>();
        stack.push(root);                              // 根先入栈
        while (!stack.isEmpty()) {
            Node<T> node = stack.pop();                // 弹出即访问（根）
            result.add(node.data);
            // 先压右、再压左 —— 栈是 LIFO，左孩子会先被弹出处理
            if (node.right != null) {
                stack.push(node.right);
            }
            if (node.left != null) {
                stack.push(node.left);
            }
        }
        return result;
    }

    /** 中序遍历（迭代）：一路向左压栈，弹出访问后转向右子树 */
    public List<T> inorderIterative() {
        List<T> result = new ArrayList<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        Node<T> cur = root;
        while (cur != null || !stack.isEmpty()) {
            // 1. 沿左链一路向下，把沿途节点全部压栈
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            // 2. 此时 cur == null，栈顶节点的左子树已处理完 → 弹出并访问
            cur = stack.pop();
            result.add(cur.data);
            // 3. 转向右子树，重复上面的过程
            cur = cur.right;
        }
        return result;
    }

    /**
     * 后序遍历（迭代）：单栈 + 记录上次访问的节点。
     * 关键：只有当「右子树为空」或「右子树刚被访问过」时，才能访问当前节点。
     */
    public List<T> postorderIterative() {
        List<T> result = new ArrayList<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        Node<T> cur = root;
        Node<T> lastVisited = null;                    // 上一次访问的节点
        while (cur != null || !stack.isEmpty()) {
            // 1. 沿左链一路压栈
            while (cur != null) {
                stack.push(cur);
                cur = cur.left;
            }
            // 2. 查看栈顶，暂不弹出
            Node<T> peek = stack.peek();
            // 右子树为空，或右子树刚被访问过 → 左右都已处理，可以访问根
            if (peek.right == null || peek.right == lastVisited) {
                result.add(peek.data);
                lastVisited = stack.pop();
            } else {
                // 3. 否则先处理右子树
                cur = peek.right;
            }
        }
        return result;
    }

    // ======================== 层序遍历（用队列） ========================

    /**
     * 层序遍历（广度优先）：自上而下、自左向右逐层访问。
     * 队列保证「先入队的节点先被访问」，即同一层从左到右、上层先于下层。
     */
    public List<T> levelOrder() {
        List<T> result = new ArrayList<>();
        if (root == null) {
            return result;
        }
        Deque<Node<T>> queue = new ArrayDeque<>();
        queue.offer(root);                             // 根先入队
        while (!queue.isEmpty()) {
            Node<T> node = queue.poll();               // 出队即访问
            result.add(node.data);
            if (node.left != null) {
                queue.offer(node.left);                // 左孩子入队
            }
            if (node.right != null) {
                queue.offer(node.right);               // 右孩子入队
            }
        }
        return result;
    }

    // ======================== 高度 / 节点数 ========================

    /**
     * 树的高度（递归）。
     * 约定：空树高度为 0，单节点高度为 1 —— 即「从根到最远叶子的节点个数」。
     */
    public int height() {
        return height(root);
    }

    private int height(Node<T> node) {
        if (node == null) {
            return 0;                                  // 空树 / 越过叶子的高度
        }
        // 当前节点高度 = 1 + 左右子树高度的较大者
        return 1 + Math.max(height(node.left), height(node.right));
    }

    /** 节点总数（递归） */
    public int size() {
        return size(root);
    }

    private int size(Node<T> node) {
        if (node == null) {
            return 0;
        }
        // 节点总数 = 1（自身） + 左子树节点数 + 右子树节点数
        return 1 + size(node.left) + size(node.right);
    }

    // ======================== 二叉搜索树（BST）操作 ========================
    // BST 性质：对任意节点，左子树所有键 < 该节点键 < 右子树所有键。
    // 基于该性质，查找 / 插入 / 删除都可沿一条路径递归向下，平均 O(log N)。

    /**
     * 插入键（递归）。
     * 若键已存在则不重复插入；否则落到合适位置作为新叶子。
     */
    public void put(T key) {
        root = put(root, key);
    }

    private Node<T> put(Node<T> node, T key) {
        if (node == null) {
            return new Node<>(key);                       // 找到空位，新建叶子
        }
        int cmp = key.compareTo(node.data);
        if (cmp < 0) {
            node.left = put(node.left, key);              // 比当前小 → 进左子树
        } else if (cmp > 0) {
            node.right = put(node.right, key);            // 比当前大 → 进右子树
        }
        // cmp == 0：键已存在，不做任何事
        return node;
    }

    /**
     * 查找键（递归）。
     * @return 命中的键，找不到返回 null
     */
    public T get(T key) {
        Node<T> node = get(root, key);
        return node == null ? null : node.data;
    }

    private Node<T> get(Node<T> node, T key) {
        if (node == null) {
            return null;                                  // 递归到底未命中
        }
        int cmp = key.compareTo(node.data);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node;                                  // 命中
        }
    }

    /**
     * 删除键（递归）—— 覆盖三种情况：
     *  1. 叶子节点：直接删除（返回 null）
     *  2. 只有一个孩子：用孩子顶替被删节点
     *  3. 有两个孩子：用右子树最小节点（后继）顶替，再递归删除该后继
     */
    public void remove(T key) {
        root = remove(root, key);
    }

    private Node<T> remove(Node<T> node, T key) {
        if (node == null) {
            return null;                                  // 未找到，不做任何事
        }
        int cmp = key.compareTo(node.data);
        if (cmp < 0) {
            node.left = remove(node.left, key);
        } else if (cmp > 0) {
            node.right = remove(node.right, key);
        } else {
            // 找到待删节点
            if (node.left == null && node.right == null) {
                return null;                              // 情况1：叶子，直接删
            }
            if (node.left == null) {
                return node.right;                        // 情况2：只有右孩子
            }
            if (node.right == null) {
                return node.left;                         // 情况2：只有左孩子
            }
            // 情况3：左右都有孩子 —— 用后继（右子树最小节点）替换
            Node<T> successor = min(node.right);
            node.data = successor.data;                   // 后继的值上移，只是key变化，左右子树指针不变
            node.right = remove(node.right, successor.data); // 删掉原后继节点
        }
        return node;
    }

    /**
     * 最小值（递归）—— 一直沿左孩子走到头。
     * @return 最小键，空树返回 null
     */
    public T min() {
        if (root == null) {
            return null;
        }
        return min(root).data;
    }

    private Node<T> min(Node<T> node) {
        if (node.left == null) {
            return node;                                  // 没有更小的了
        }
        return min(node.left);
    }

    /**
     * 最大值（递归）—— 一直沿右孩子走到头。
     * @return 最大键，空树返回 null
     */
    public T max() {
        if (root == null) {
            return null;
        }
        return max(root).data;
    }

    private Node<T> max(Node<T> node) {
        if (node.right == null) {
            return node;                                  // 没有更大的了
        }
        return max(node.right);
    }

    /**
     * 下界 floor（递归）：返回 <= key 的最大键。
     * 若 key 小于当前节点 → 只能在左子树；否则当前节点是候选，再看右子树有没有更接近的。
     * @return 满足条件的键，不存在返回 null
     */
    public T floor(T key) {
        Node<T> node = floor(root, key);
        return node == null ? null : node.data;
    }

    private Node<T> floor(Node<T> node, T key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.data);
        if (cmp == 0) {
            return node;                                  // 恰好相等
        }
        if (cmp < 0) {
            return floor(node.left, key);                 // key 更小，只能去左子树找
        }
        // cmp > 0：当前节点可作为候选，但右子树可能有更接近 key 的更大值
        Node<T> t = floor(node.right, key);
        return t != null ? t : node;
    }

    /**
     * 上界 ceiling（递归）：返回 >= key 的最小键，与 floor 对称。
     * @return 满足条件的键，不存在返回 null
     */
    public T ceiling(T key) {
        Node<T> node = ceiling(root, key);
        return node == null ? null : node.data;
    }

    private Node<T> ceiling(Node<T> node, T key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.data);
        if (cmp == 0) {
            return node;
        }
        if (cmp > 0) {
            return ceiling(node.right, key);              // key 更大，只能去右子树找
        }
        // cmp < 0：当前节点可作为候选，但左子树可能有更接近 key 的较小值
        Node<T> t = ceiling(node.left, key);
        return t != null ? t : node;
    }

    /**
     * 排名 rank（递归）：返回严格小于 key 的键的个数（即 key 在有序序列中的下标）。
     * 比较到当前节点时：
     *  - key 更小 → 只统计左子树
     *  - key 更大 → 左子树 + 当前节点 + 右子树中小于 key 的部分
     *  - 相等     → 只统计左子树
     */
    public int rank(T key) {
        return rank(root, key);
    }

    private int rank(Node<T> node, T key) {
        if (node == null) {
            return 0;
        }
        int cmp = key.compareTo(node.data);
        if (cmp < 0) {
            return rank(node.left, key);
        } else if (cmp > 0) {
            return 1 + size(node.left) + rank(node.right, key);
        } else {
            return size(node.left);
        }
    }

    // ======================== 主方法（演示用） ========================

    /** 演示四种遍历及高度/节点数 */
    public static void main(String[] args) {
        // 构造示例树：
        //         1
        //        / \
        //       2   3
        //      / \   \
        //     4   5   6
        Node<Integer> n4 = new Node<>(4);
        Node<Integer> n5 = new Node<>(5);
        Node<Integer> n6 = new Node<>(6);
        Node<Integer> n2 = new Node<>(2, n4, n5);
        Node<Integer> n3 = new Node<>(3, null, n6);
        Node<Integer> n1 = new Node<>(1, n2, n3);

        BinaryTree<Integer> tree = new BinaryTree<>(n1);

        System.out.println("前序（递归）：" + tree.preorderRecursive());
        System.out.println("前序（迭代）：" + tree.preorderIterative());
        System.out.println("中序（递归）：" + tree.inorderRecursive());
        System.out.println("中序（迭代）：" + tree.inorderIterative());
        System.out.println("后序（递归）：" + tree.postorderRecursive());
        System.out.println("后序（迭代）：" + tree.postorderIterative());
        System.out.println("层序（队列）：" + tree.levelOrder());
        System.out.println("树的高度：" + tree.height());
        System.out.println("节点数：" + tree.size());

        // ---- BST 演示 ----
        BinaryTree<Integer> bst = new BinaryTree<>();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80}) {
            bst.put(v);
        }
        System.out.println("\n[BST 演示] 中序：" + bst.inorderRecursive());
        System.out.println("[BST 演示] min=" + bst.min() + ", max=" + bst.max());
        System.out.println("[BST 演示] floor(45)=" + bst.floor(45) + ", ceiling(45)=" + bst.ceiling(45));
        System.out.println("[BST 演示] rank(50)=" + bst.rank(50));
        bst.remove(70);                                   // 删除「有两个孩子」的节点
        System.out.println("[BST 演示] 删除 70 后中序：" + bst.inorderRecursive());
    }
}
