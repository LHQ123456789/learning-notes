package BinaryTreeTest;

import java.util.ArrayList;
import java.util.List;

/**
 * AVL 树（平衡二叉搜索树）—— 在 BST 的基础上，通过「旋转」保证任意节点
 * 左右子树高度差不大于 1，从而把最坏情况的高度从 O(N) 拉回到 O(log N)。
 *
 * 本类包含：
 *  1. 节点定义（Node 静态内部类）：key + value + 左右孩子 + 高度 height
 *  2. 高度 height / 平衡因子 balanceFactor 的计算
 *  3. 四种旋转：LL 单旋（右旋）/ RR 单旋（左旋）/ LR 双旋 / RL 双旋
 *  4. 插入 put：插入后自底向上重算高度并自动平衡
 *  5. 查找 get / 中序遍历 inorder / 树高 height / 节点数 size
 *
 * 平衡因子约定：balanceFactor = height(left) - height(right)。
 *   - bf ∈ {-1, 0, 1}  平衡
 *   - bf =  2          左重（需 LL 或 LR）
 *   - bf = -2          右重（需 RR 或 RL）
 *
 * 高度约定：与 {@link BinaryTree#height()} 保持一致——空树高度为 0，单节点高度为 1，
 * 即「从根到最远叶子的节点个数」。这样 AVL 与 BST 的高度可以直接比较。
 *
 * @param <K> 键类型（须可比较，Comparable）
 * @param <V> 值类型
 */
public class AVLTree<K extends Comparable<K>, V> {

    //这里extends Comparable是为了约束Key为可比较对象

    // ======================== 节点定义 ========================

    /**
     * AVL 树节点：键值对 + 左右孩子 + 高度。
     * 与 BST 节点相比多了一个 height 字段，用于 O(1) 计算平衡因子。
     */
    public static class Node<K, V> {
        public K key;             // 键
        public V value;           // 值
        public Node<K, V> left;   // 左孩子
        public Node<K, V> right;  // 右孩子
        public int height;        // 以该节点为根的子树高度

        /** 构造叶子节点（高度 1，无左右孩子） */
        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
            this.height = 1;                          // 新叶子高度恒为 1
        }
    }

    // ======================== 字段 ========================

    private Node<K, V> root;   // 根节点
    //根节点私有化，防止外部破坏树结构

    /** 构造一棵空的 AVL 树 */
    public AVLTree() {
        this.root = null;
    }

    /** 返回根节点 */
    public Node<K, V> getRoot() {
        return root;
    }

    // ======================== 高度 / 平衡因子 ========================

    /**
     * 取某节点的高度，空节点视为 0（与 BST 的 height 约定一致）。
     * 用 height 字段 O(1) 读取，避免每次都递归计算。
     */
    private int height(Node<K, V> node) {
        return node == null ? 0 : node.height;
    }

    /** 整棵树的高度（等价于根节点高度，空树为 0） */
    public int height() {
        return height(root);
    }

    /**
     * 计算节点的平衡因子：height(left) - height(right)。
     * @return bf ∈ [-2, 2]，超过 ±1 表示失衡需要旋转
     */
    public int balanceFactor(Node<K, V> node) {
        if (node == null) {
            return 0;
        }
        return height(node.left) - height(node.right);
    }

    /** 由左右子树高度更新当前节点高度：1 + max(左, 右) */
    private void updateHeight(Node<K, V> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    // ======================== 四种旋转 ========================
    // 旋转的本质：在不破坏「中序有序」的前提下，改变局部根节点，降低子树高度。
    // 单旋一次即可修正 LL / RR；双旋 = 先转成单旋情形，再单旋一次。

    /**
     * LL 单旋（右旋）：左孩子的左子树过高。
     *
     *        y             x
     *       / \           / \
     *      x   T3  ==>  T1   y
     *     / \               / \
     *   T1  T2            T2  T3
     */
    private Node<K, V> rotateLL(Node<K, V> y) {
        Node<K, V> x = y.left;
        y.left = x.right;       // x 的右子树挂到 y 的左孩子
        x.right = y;            // y 成为 x 的右孩子
        updateHeight(y);        // 先更新下层的 y
        updateHeight(x);        // 再更新上层的 x
        return x;               // x 成为新的局部根
    }

    /**
     * RR 单旋（左旋）：右孩子的右子树过高（与 LL 镜像对称）。
     *
     *        x                 y
     *       / \               / \
     *     T1   y     ==>    x   T3
     *         / \          / \
     *       T2  T3       T1  T2
     */
    private Node<K, V> rotateRR(Node<K, V> x) {
        Node<K, V> y = x.right;
        x.right = y.left;
        y.left = x;
        updateHeight(x);
        updateHeight(y);
        return y;
    }

    /**
     * LR 双旋（左孩子的右子树过高）：先对左孩子做 RR 单旋（左旋），
     * 把它扭成 LL 情形，再对当前节点做 LL 单旋（右旋）。
     */
    private Node<K, V> rotateLR(Node<K, V> node) {
        node.left = rotateRR(node.left);   // 第一步：左孩子左旋 → 变成 LL
        return rotateLL(node);             // 第二步：当前节点右旋
    }

    /**
     * RL 双旋（右孩子的左子树过高）：先对右孩子做 LL 单旋（右旋），
     * 把它扭成 RR 情形，再对当前节点做 RR 单旋（左旋）。
     */
    private Node<K, V> rotateRL(Node<K, V> node) {
        node.right = rotateLL(node.right); // 第一步：右孩子右旋 → 变成 RR
        return rotateRR(node);             // 第二步：当前节点左旋
    }

    // ======================== 插入（自动平衡） ========================

    /**
     * 插入键值对。若键已存在则更新其值，否则作为新叶子插入。
     * 插入后自底向上重算高度，一旦某个祖先失衡就旋转修正。
     */
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    private Node<K, V> put(Node<K, V> node, K key, V value) {
        if (node == null) {
            return new Node<>(key, value);        // 找到空位，新建叶子（height=1）
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            node.value = value;                   // 键已存在：只更新值，结构不变
            return node;
        }
        return rebalance(node);                   // 回溯时逐层重算高度并修复平衡
    }

    /**
     * 重平衡：先刷新高度，再根据平衡因子选择对应的旋转。
     *   bf =  2 → 左重：左子树 bf < 0 是 LR，否则 LL
     *   bf = -2 → 右重：右子树 bf > 0 是 RL，否则 RR
     */
    private Node<K, V> rebalance(Node<K, V> node) {
        updateHeight(node);
        int bf = balanceFactor(node);
        if (bf > 1) {                             // 左重
            if (balanceFactor(node.left) < 0) {
                return rotateLR(node);            // 左孩子的右子树高 → LR 双旋
            }
            return rotateLL(node);                // 左孩子的左子树高 → LL 单旋
        }
        if (bf < -1) {                            // 右重
            if (balanceFactor(node.right) > 0) {
                return rotateRL(node);            // 右孩子的左子树高 → RL 双旋
            }
            return rotateRR(node);                // 右孩子的右子树高 → RR 单旋
        }
        return node;                              // 已平衡，无需旋转
    }

    // ======================== 查找 / 遍历 / 大小 ========================

    /** 查找键对应的值，找不到返回 null */
    public V get(K key) {
        Node<K, V> node = get(root, key);
        return node == null ? null : node.value;
    }

    private Node<K, V> get(Node<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        } else if (cmp > 0) {
            return get(node.right, key);
        } else {
            return node;
        }
    }

    /** 中序遍历：返回升序的键序列（BST 的核心不变量，用于校验） */
    public List<K> inorderKeys() {
        List<K> result = new ArrayList<>();
        inorderKeys(root, result);
        return result;
    }

    private void inorderKeys(Node<K, V> node, List<K> result) {
        if (node == null) {
            return;
        }
        inorderKeys(node.left, result);
        result.add(node.key);
        inorderKeys(node.right, result);
    }

    /** 节点总数（递归） */
    public int size() {
        return size(root);
    }

    private int size(Node<K, V> node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left) + size(node.right);
    }

    /**
     * 校验整棵树是否满足 AVL 性质：每个节点的 |平衡因子| <= 1。
     * @return 全树平衡返回 true
     */
    public boolean isBalanced() {
        return isBalanced(root);
    }

    private boolean isBalanced(Node<K, V> node) {
        if (node == null) {
            return true;
        }
        if (Math.abs(balanceFactor(node)) > 1) {
            return false;
        }
        return isBalanced(node.left) && isBalanced(node.right);
    }

    // ======================== 主方法（对比 BST vs AVL） ========================

    /**
     * 演示：把同一条「严格递增」的插入序列分别喂给普通 BST 和 AVL。
     *
     * 递增序列对 BST 是最坏的退化场景——每次都往最右插，树变成一条链，高度 = N；
     * 而 AVL 会在插入过程中自动旋转保持平衡，高度始终维持在 O(log N)。
     */
    public static void main(String[] args) {
        int n = 31;                                   // 2^5 - 1，AVL 应正好高度 5

        // 1. 普通 BST（复用现有 BinaryTree）插入递增序列 → 退化成链
        BinaryTree<Integer> bst = new BinaryTree<>();
        for (int i = 1; i <= n; i++) {
            bst.put(i);
        }

        // 2. AVL 插入同样的递增序列 → 自动旋转保持平衡
        AVLTree<Integer, String> avl = new AVLTree<>();
        for (int i = 1; i <= n; i++) {
            avl.put(i, "v" + i);
        }

        // 3. 对比高度
        System.out.println("插入 1.." + n + " 的递增序列后：");
        System.out.println("  普通 BST 高度 = " + bst.height() + "  (退化成链)");
        System.out.println("  AVL    高度 = " + avl.height() + "  (始终保持平衡)");
        System.out.println("  AVL 仍平衡？ " + avl.isBalanced()
                + "，节点数 = " + avl.size()
                + "，中序有序？ " + isSorted(avl.inorderKeys()));
    }

    /** 辅助：判断序列是否严格升序 */
    private static <T extends Comparable<T>> boolean isSorted(List<T> list) {
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i - 1).compareTo(list.get(i)) >= 0) {
                return false;
            }
        }
        return true;
    }
}
