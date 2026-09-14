package BinaryTreeTest;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * 二叉树遍历的 JUnit 测试用例。
 *
 * 统一使用一棵固定的示例树：
 *
 *          1
 *         / \
 *        2   3
 *       / \   \
 *      4   5   6
 *
 * 四种遍历的期望序列：
 *   前序：1 2 4 5 3 6
 *   中序：4 2 5 1 3 6
 *   后序：4 5 2 6 3 1
 *   层序：1 2 3 4 5 6
 *
 * 每个遍历测试同时验证「递归版本」与「迭代版本」，并各自与期望序列比对，
 * 从而间接保证递归与迭代实现结果一致。
 */
public class BinaryTreeTest {

    // ---- 期望序列（四种遍历 + 高度/节点数） ----

    private static final List<Integer> PREORDER   = Arrays.asList(1, 2, 4, 5, 3, 6);
    private static final List<Integer> INORDER    = Arrays.asList(4, 2, 5, 1, 3, 6);
    private static final List<Integer> POSTORDER  = Arrays.asList(4, 5, 2, 6, 3, 1);
    private static final List<Integer> LEVEL_ORDER = Arrays.asList(1, 2, 3, 4, 5, 6);

    // ---- 辅助方法：构造示例树 ----

    /**
     * 构造示例树：
     *          1
     *         / \
     *        2   3
     *       / \   \
     *      4   5   6
     */
    private BinaryTree<Integer> buildSampleTree() {
        BinaryTree.Node<Integer> n4 = new BinaryTree.Node<>(4);
        BinaryTree.Node<Integer> n5 = new BinaryTree.Node<>(5);
        BinaryTree.Node<Integer> n6 = new BinaryTree.Node<>(6);
        BinaryTree.Node<Integer> n2 = new BinaryTree.Node<>(2, n4, n5);
        BinaryTree.Node<Integer> n3 = new BinaryTree.Node<>(3, null, n6);
        BinaryTree.Node<Integer> n1 = new BinaryTree.Node<>(1, n2, n3);
        return new BinaryTree<>(n1);
    }

    // ---- 1. 前序遍历 ----

    @Test
    public void testPreorderTraversal() {
        BinaryTree<Integer> tree = buildSampleTree();

        assertEquals("前序（递归）序列错误", PREORDER, tree.preorderRecursive());
        assertEquals("前序（迭代）序列错误", PREORDER, tree.preorderIterative());
    }

    // ---- 2. 中序遍历 ----

    @Test
    public void testInorderTraversal() {
        BinaryTree<Integer> tree = buildSampleTree();

        assertEquals("中序（递归）序列错误", INORDER, tree.inorderRecursive());
        assertEquals("中序（迭代）序列错误", INORDER, tree.inorderIterative());
    }

    // ---- 3. 后序遍历 ----

    @Test
    public void testPostorderTraversal() {
        BinaryTree<Integer> tree = buildSampleTree();

        assertEquals("后序（递归）序列错误", POSTORDER, tree.postorderRecursive());
        assertEquals("后序（迭代）序列错误", POSTORDER, tree.postorderIterative());
    }

    // ---- 4. 层序遍历 ----

    @Test
    public void testLevelOrderTraversal() {
        BinaryTree<Integer> tree = buildSampleTree();

        assertEquals("层序（队列）序列错误", LEVEL_ORDER, tree.levelOrder());
    }

    // ---- 5. 高度 / 节点数 ----

    @Test
    public void testHeightAndSize() {
        BinaryTree<Integer> tree = buildSampleTree();

        // 最长路径 1→2→4（或 1→3→6），共 3 个节点 → 高度 3
        assertEquals("树的高度错误", 3, tree.height());
        // 共 6 个节点
        assertEquals("节点数错误", 6, tree.size());
    }

    // ======================== 二叉搜索树（BST）测试 ========================

    /**
     * 构造一棵固定的 BST：
     *          50
     *         /  \
     *        30   70
     *       / \   / \
     *      20 40 60 80
     *           \
     *            65
     * 便于精确测试删除的三种情况：叶子（20）、单孩子（60）、双子（70 或 50）。
     */
    private BinaryTree<Integer> buildSampleBst() {
        BinaryTree<Integer> bst = new BinaryTree<>();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80, 65}) {
            bst.put(v);
        }
        return bst;
    }

    /** 断言中序遍历升序（BST 的核心不变量） */
    private void assertInorderSorted(BinaryTree<Integer> bst) {
        List<Integer> inorder = bst.inorderRecursive();
        for (int i = 1; i < inorder.size(); i++) {
            assertTrue("中序遍历应升序，但 " + inorder.get(i - 1) + " >= " + inorder.get(i),
                    inorder.get(i - 1).compareTo(inorder.get(i)) < 0);
        }
    }

    // ---- 1. 插入 100 个随机数，验证中序有序 ----

    @Test
    public void testBstInsertAndInorderSorted() {
        BinaryTree<Integer> bst = new BinaryTree<>();
        Random random = new Random(42);               // 固定种子，保证可复现
        Set<Integer> keys = new HashSet<>();
        while (keys.size() < 100) {                   // 生成 100 个不重复的随机数
            keys.add(random.nextInt(10000));
        }
        for (int key : keys) {
            bst.put(key);
        }

        assertEquals("插入 100 个不同随机数后节点数应为 100", 100, bst.size());

        List<Integer> inorder = bst.inorderRecursive();
        assertEquals("中序遍历元素个数应为 100", 100, inorder.size());
        assertInorderSorted(bst);
    }

    // ---- 2. get ----

    @Test
    public void testBstGet() {
        BinaryTree<Integer> bst = buildSampleBst();

        assertEquals(Integer.valueOf(50), bst.get(50));
        assertEquals(Integer.valueOf(65), bst.get(65));
        assertEquals(Integer.valueOf(20), bst.get(20));
        assertNull("不存在的键应返回 null", bst.get(99));
        assertNull("空树查找应返回 null", new BinaryTree<Integer>().get(1));
    }

    // ---- 3. min / max ----

    @Test
    public void testBstMinMax() {
        BinaryTree<Integer> bst = buildSampleBst();

        assertEquals(Integer.valueOf(20), bst.min());
        assertEquals(Integer.valueOf(80), bst.max());
        assertNull("空树 min 应为 null", new BinaryTree<Integer>().min());
        assertNull("空树 max 应为 null", new BinaryTree<Integer>().max());
    }

    // ---- 4. floor / ceiling ----

    @Test
    public void testBstFloorCeiling() {
        BinaryTree<Integer> bst = buildSampleBst();

        // 键存在于树中：floor/ceiling 均返回其本身
        assertEquals(Integer.valueOf(50), bst.floor(50));
        assertEquals(Integer.valueOf(50), bst.ceiling(50));
        // 键不存在：找前后最近的
        assertEquals(Integer.valueOf(40), bst.floor(45));
        assertEquals(Integer.valueOf(50), bst.ceiling(45));
        // 越界：floor 比最小还小 / ceiling 比最大还大
        assertNull("floor(10) 应为 null", bst.floor(10));
        assertNull("ceiling(90) 应为 null", bst.ceiling(90));
    }

    // ---- 5. rank ----

    @Test
    public void testBstRank() {
        BinaryTree<Integer> bst = buildSampleBst();
        // 键集合 {20,30,40,50,60,65,70,80}
        assertEquals(0, bst.rank(20));   // 没有比 20 更小的
        assertEquals(3, bst.rank(50));   // 20,30,40 三个小于 50
        assertEquals(7, bst.rank(80));   // 除 80 外全部小于它
        assertEquals(0, bst.rank(10));   // 比最小还小
        assertEquals(8, bst.rank(99));   // 比最大还大
    }

    // ---- 6. 删除的三种情况 ----

    /** 情况1：删除叶子节点（20） */
    @Test
    public void testRemoveLeafNode() {
        BinaryTree<Integer> bst = buildSampleBst();

        bst.remove(20);

        assertNull("删除后 20 应不存在", bst.get(20));
        assertEquals("删除叶子后节点数应减 1", 7, bst.size());
        assertInorderSorted(bst);
    }

    /** 情况2：删除只有一个孩子的节点（60，仅右孩子 65） */
    @Test
    public void testRemoveSingleChildNode() {
        BinaryTree<Integer> bst = buildSampleBst();

        bst.remove(60);

        assertNull("删除后 60 应不存在", bst.get(60));
        assertNotNull("孩子 65 应被顶替上来", bst.get(65));
        assertEquals("删除单孩子节点后节点数应减 1", 7, bst.size());
        assertInorderSorted(bst);
    }

    /** 情况3：删除有两个孩子的节点（根 50，用后继 60 顶替） */
    @Test
    public void testRemoveTwoChildrenNode() {
        BinaryTree<Integer> bst = buildSampleBst();

        bst.remove(50);

        assertNull("删除后 50 应不存在", bst.get(50));
        assertNotNull("后继 60 应顶替到根", bst.get(60));
        assertEquals("删除双子节点后节点数应减 1", 7, bst.size());
        assertInorderSorted(bst);
    }
}
