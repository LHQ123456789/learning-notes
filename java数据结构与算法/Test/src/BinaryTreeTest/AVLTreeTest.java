package BinaryTreeTest;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * AVL 树的 JUnit 测试用例。
 *
 * 覆盖：
 *  1. 四种旋转各自的失衡场景（LL / RR / LR / RL）
 *  2. 递增序列（BST 的最坏退化场景）下 AVL 仍保持平衡且高度为 O(log N)
 *  3. 随机插入 1000 个键：全树平衡、中序有序、查找正确
 *  4. 重复插入（更新值）不改变节点数
 */
public class AVLTreeTest {

    /** 断言一棵 AVL 树满足全部性质：平衡 + 中序有序 + 节点数正确 */
    private void assertAVLValid(AVLTree<Integer, String> tree, int expectedSize) {
        assertTrue("树应始终保持平衡（任意节点 |bf| <= 1）", tree.isBalanced());
        assertEquals("节点数不符", expectedSize, tree.size());

        List<Integer> keys = tree.inorderKeys();
        assertEquals("中序元素个数不符", expectedSize, keys.size());
        for (int i = 1; i < keys.size(); i++) {
            assertTrue("中序遍历应升序，但 " + keys.get(i - 1) + " >= " + keys.get(i),
                    keys.get(i - 1).compareTo(keys.get(i)) < 0);
        }
    }

    // ---- 1. LL 单旋（右旋）：左左失衡 ----

    @Test
    public void testLLRotation() {
        // 依次插入 3,2,1 → 3 的左孩子 2 的左孩子 1，触发 LL 单旋
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.put(3, "c");
        tree.put(2, "b");
        tree.put(1, "a");

        // 旋转后 2 应为根，左右各一个孩子
        assertEquals("LL 旋转后根应为 2", Integer.valueOf(2), tree.getRoot().key);
        assertEquals("左孩子应为 1", Integer.valueOf(1), tree.getRoot().left.key);
        assertEquals("右孩子应为 3", Integer.valueOf(3), tree.getRoot().right.key);
        assertAVLValid(tree, 3);
    }

    // ---- 2. RR 单旋（左旋）：右右失衡 ----

    @Test
    public void testRRRotation() {
        // 依次插入 1,2,3 → 1 的右孩子 2 的右孩子 3，触发 RR 单旋
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.put(1, "a");
        tree.put(2, "b");
        tree.put(3, "c");

        // 旋转后 2 应为根
        assertEquals("RR 旋转后根应为 2", Integer.valueOf(2), tree.getRoot().key);
        assertEquals("左孩子应为 1", Integer.valueOf(1), tree.getRoot().left.key);
        assertEquals("右孩子应为 3", Integer.valueOf(3), tree.getRoot().right.key);
        assertAVLValid(tree, 3);
    }

    // ---- 3. LR 双旋：左右失衡 ----

    @Test
    public void testLRRotation() {
        // 依次插入 3,1,2 → 3 的左孩子 1 的右孩子 2，触发 LR 双旋
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.put(3, "c");
        tree.put(1, "a");
        tree.put(2, "b");

        // 双旋后 2 应为根
        assertEquals("LR 旋转后根应为 2", Integer.valueOf(2), tree.getRoot().key);
        assertEquals("左孩子应为 1", Integer.valueOf(1), tree.getRoot().left.key);
        assertEquals("右孩子应为 3", Integer.valueOf(3), tree.getRoot().right.key);
        assertAVLValid(tree, 3);
    }

    // ---- 4. RL 双旋：右左失衡 ----

    @Test
    public void testRLRotation() {
        // 依次插入 1,3,2 → 1 的右孩子 3 的左孩子 2，触发 RL 双旋
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.put(1, "a");
        tree.put(3, "c");
        tree.put(2, "b");

        // 双旋后 2 应为根
        assertEquals("RL 旋转后根应为 2", Integer.valueOf(2), tree.getRoot().key);
        assertEquals("左孩子应为 1", Integer.valueOf(1), tree.getRoot().left.key);
        assertEquals("右孩子应为 3", Integer.valueOf(3), tree.getRoot().right.key);
        assertAVLValid(tree, 3);
    }

    // ---- 5. 递增序列（BST 最坏退化场景）下仍平衡 ----

    @Test
    public void testAscendingInsertKeepsBalanced() {
        int n = 1000;
        AVLTree<Integer, String> tree = new AVLTree<>();
        for (int i = 1; i <= n; i++) {
            tree.put(i, "v" + i);
        }

        assertAVLValid(tree, n);

        // AVL 高度上界 ≈ 1.44 * log2(N + 2)，1000 个节点高度绝不可能超过 20；
        // 而普通 BST 插入同样的递增序列会退化成链，高度 = 1000。
        assertTrue("AVL 高度应远小于节点数（退化 BST 会是 1000）",
                tree.height() <= 20);
    }

    // ---- 6. 随机插入大量键 ----

    @Test
    public void testRandomInsert() {
        java.util.Random random = new java.util.Random(42);
        AVLTree<Integer, String> tree = new AVLTree<>();
        java.util.Set<Integer> keys = new java.util.HashSet<>();
        while (keys.size() < 1000) {
            keys.add(random.nextInt(100000));
        }
        for (int key : keys) {
            tree.put(key, "v" + key);
        }

        assertAVLValid(tree, 1000);
        assertTrue("随机 1000 个节点的 AVL 高度应不超过 20", tree.height() <= 20);

        // 抽查若干键的 get
        for (int key : keys) {
            assertEquals("get(" + key + ") 应命中", "v" + key, tree.get(key));
        }
    }

    // ---- 7. 重复插入更新值 ----

    @Test
    public void testPutExistingKeyUpdatesValue() {
        AVLTree<Integer, String> tree = new AVLTree<>();
        tree.put(10, "old");
        tree.put(10, "new");

        assertEquals("键已存在时应更新值", "new", tree.get(10));
        assertEquals("重复插入不应增加节点数", 1, tree.size());
        assertAVLValid(tree, 1);
    }

    // ---- 8. get 与空树 ----

    @Test
    public void testGet() {
        AVLTree<Integer, String> tree = new AVLTree<>();
        assertNull("空树查找应返回 null", tree.get(1));
        assertNull("空树根应为 null", tree.getRoot());

        for (int i = 1; i <= 10; i++) {
            tree.put(i, "v" + i);
        }
        assertEquals("v5", tree.get(5));
        assertNull("不存在的键应返回 null", tree.get(99));
        assertEquals("空树高度应为 0", 0, new AVLTree<Integer, String>().height());
    }
}
