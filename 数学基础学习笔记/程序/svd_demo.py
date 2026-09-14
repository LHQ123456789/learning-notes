"""
NumPy SVD 分解实现 + 图像压缩 Demo
====================================
1. 手动实现 SVD（通过 A^T A 的特征值分解）
2. 与 np.linalg.svd 对比验证
3. 用 SVD 对灰度图做低秩近似压缩，展示不同 k 的重建效果


代码结构：
    - svd_manual(A) — 手写 SVD：通过 np.linalg.eigh(A.T @ A) 求特征值/特征向量得到 V 和 Σ，再由 u_i = A v_i / σ_i 得到 U，零空间用投影+QR补齐
    - reconstruct(U, S, Vt, k) — 低秩近似：A_k = U[:,:k] @ diag(S[:k]) @ Vt[:k,:]
    - generate_test_image() — 生成含正弦波纹、矩形块、渐变背景的合成测试图
    - main() — 验证手动 SVD → SVD 分解图像 → 绘制奇异值衰减 + 累积能量 + 10 种 k 值重建对比
"""

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.gridspec as gridspec


# ============================================================
# Part 1: 手动实现 SVD
# ============================================================

def svd_manual(A):
    """
    通过特征值分解 A^T A 手动实现 SVD 分解。

    数学原理:
      A = U Σ V^T, 其中
        A^T A = V Σ^2 V^T  →  V 是 A^T A 的特征向量矩阵
        σ_i = √λ_i
        u_i = (1/σ_i) A v_i  (σ_i > 0 时)

    输入:  A — shape (m, n) 的实数矩阵
    输出:  U, S, Vt — 满足 A = U @ diag(S) @ Vt (在机器精度内)
           U  (m, m)  左奇异向量矩阵（正交）
           S  (k,)    奇异值（降序，k = min(m,n)）
           Vt (n, n)  右奇异向量矩阵的转置（正交）
    """
    m, n = A.shape
    A = A.astype(np.float64)

    if m >= n:
        # ================================================================
        # "瘦" 或方阵 (m ≥ n)：对 A^T A (n×n) 特征分解
        # ================================================================
        ATA = A.T @ A                                    # (n, n) 对称半正定
        eigenvalues, V = np.linalg.eigh(ATA)             # eigh 返回升序
        # 翻转为降序
        idx = np.argsort(eigenvalues)[::-1]
        eigenvalues = eigenvalues[idx]
        V = V[:, idx]

        # 奇异值 σ_i = √λ_i（处理数值负零）
        S_all = np.sqrt(np.maximum(eigenvalues, 0))

        # 非零奇异值个数（即矩阵的秩）
        r = np.sum(S_all > 1e-12)

        # ---------- 构造 U ----------
        U = np.zeros((m, m))
        # 对于 σ_i > 0: u_i = (1/σ_i) A v_i
        for i in range(r):
            U[:, i] = (A @ V[:, i]) / S_all[i]

        # 补齐 U[:, r:] 为 U[:, :r] 列空间的正交补
        if r < m:
            # 方法: 随机向量 → 减去在已知列上的投影 → QR 正交化
            Z = np.random.randn(m, m - r)
            Z = Z - U[:, :r] @ (U[:, :r].T @ Z)          # 投影减法
            Q, _ = np.linalg.qr(Z)                       # QR 正交化
            U[:, r:] = Q

        S = S_all[:n]

    else:
        # ================================================================
        # "宽" 矩阵 (m < n)：对 A A^T (m×m) 特征分解，节省计算
        # ================================================================
        AAT = A @ A.T                                    # (m, m)
        eigenvalues, U = np.linalg.eigh(AAT)
        idx = np.argsort(eigenvalues)[::-1]
        eigenvalues = eigenvalues[idx]
        U = U[:, idx]

        S_all = np.sqrt(np.maximum(eigenvalues, 0))
        r = np.sum(S_all > 1e-12)

        # 构造 V
        V = np.zeros((n, n))
        for i in range(r):
            V[:, i] = (A.T @ U[:, i]) / S_all[i]

        # 补齐 V[:, r:] 为 V[:, :r] 列空间的正交补
        if r < n:
            Z = np.random.randn(n, n - r)
            Z = Z - V[:, :r] @ (V[:, :r].T @ Z)
            Q, _ = np.linalg.qr(Z)
            V[:, r:] = Q

        S = S_all

    # 取 min(m, n) 个奇异值，Vt = V^T
    k = min(m, n)
    return U, S[:k], V.T


def reconstruct(U, S, Vt, k):
    """用前 k 个奇异值重建矩阵（低秩近似）。"""
    return U[:, :k] @ np.diag(S[:k]) @ Vt[:k, :]


# ============================================================
# Part 2: 生成测试灰度图
# ============================================================

def generate_test_image(size=256):
    """
    生成一张合成灰度图，包含多种结构：
    - 正弦波纹
    - 矩形块
    - 渐变背景
    """
    x = np.linspace(-3, 3, size)
    y = np.linspace(-3, 3, size)
    X, Y = np.meshgrid(x, y)

    # 背景渐变
    img = 0.3 + 0.2 * (X / 3) + 0.2 * (Y / 3)

    # 同心正弦波纹
    R = np.sqrt(X**2 + Y**2)
    img += 0.25 * np.sin(4 * R) / (1 + R * 0.3)

    # 倾斜正弦波纹
    img += 0.2 * np.sin(5 * X) * np.cos(5 * Y)

    # 中心矩形亮块
    mask = (np.abs(X) < 0.8) & (np.abs(Y) < 0.6)
    img[mask] += 0.4

    # 角落暗块
    mask_corner = (np.abs(X) > 2) & (np.abs(Y) > 2)
    img[mask_corner] -= 0.2

    # 圆形亮斑
    circle1 = R < 0.3
    img[circle1] += 0.5

    # 归一化到 [0, 1]
    img = (img - img.min()) / (img.max() - img.min())
    return img


# ============================================================
# Part 3: 主流程
# ============================================================

def main():
    print("=" * 60)
    print("NumPy SVD 分解 + 图像压缩 Demo")
    print("=" * 60)

    # ---------- 3.1 验证手动 SVD ----------
    print("\n[1] 验证手动 SVD 实现...")
    np.random.seed(42)
    A_test = np.random.randn(6, 4)  # 6×4 矩阵
    U_m, S_m, Vt_m = svd_manual(A_test)

    # NumPy 内置 SVD
    U_np, S_np, Vt_np = np.linalg.svd(A_test, full_matrices=True)

    print(f"  手动 SVD:  U={U_m.shape}, S={S_m.shape}, Vt={Vt_m.shape}")
    print(f"  NumPy SVD: U={U_np.shape}, S={S_np.shape}, Vt={Vt_np.shape}")

    # 重建误差
    A_recon_m = reconstruct(U_m, S_m, Vt_m, len(S_m))
    A_recon_np = U_np[:, :4] @ np.diag(S_np) @ Vt_np

    err_manual = np.linalg.norm(A_test - A_recon_m)
    err_np = np.linalg.norm(A_test - A_recon_np)
    print(f"  手动 SVD 重建误差: {err_manual:.2e}")
    print(f"  NumPy SVD 重建误差: {err_np:.2e}")

    # 奇异值一致性
    print(f"\n  手动 SVD 前 4 个奇异值: {np.round(S_m, 4)}")
    print(f"  NumPy SVD 前 4 个奇异值: {np.round(S_np, 4)}")

    # ---------- 3.2 图像压缩 ----------
    print("\n[2] 生成测试图像 & SVD 压缩...")
    img = generate_test_image(256)
    m, n = img.shape
    print(f"  图像尺寸: {m} × {n} = {m * n} 像素")

    # 对图像矩阵做 SVD
    U, S, Vt = np.linalg.svd(img, full_matrices=False)
    print(f"  奇异值数量: {len(S)}, 前10个: {np.round(S[:10], 3)}")

    # 不同 k 值
    ks = [1, 2, 3, 5, 10, 20, 30, 50, 100, 256]
    # 每个 k 的实际上限不超过奇异值总数
    ks = [k for k in ks if k <= len(S)]

    # ---------- 3.3 可视化 ----------
    print("\n[3] 绘制结果...")
    fig = plt.figure(figsize=(18, 10))
    gs = gridspec.GridSpec(3, 5, figure=fig,
                           hspace=0.35, wspace=0.25)

    # --- 第一行：奇异值衰减 ---
    ax_sv = fig.add_subplot(gs[0, :2])
    ax_sv.semilogy(S, 'b-', linewidth=0.8, alpha=0.7)
    ax_sv.semilogy(S, 'r.', markersize=3)
    # 标注 k 值位置
    for k in ks[:7]:
        if k <= len(S):
            ax_sv.axvline(x=k - 1, color='gray', linestyle='--', alpha=0.5)
            ax_sv.text(k - 1, S[k - 1] * 1.5, f'k={k}',
                       fontsize=7, ha='center', color='gray')
    ax_sv.set_xlabel('Index')
    ax_sv.set_ylabel('Singular Value (log scale)')
    ax_sv.set_title('Singular Value Decay')
    ax_sv.grid(True, alpha=0.3)

    # --- 累积能量占比 ---
    ax_energy = fig.add_subplot(gs[0, 2:])
    cum_energy = np.cumsum(S**2) / np.sum(S**2)
    ax_energy.plot(cum_energy, 'b-', linewidth=1.2)
    # 标注关键 k 对应的能量占比
    for k in [1, 3, 5, 10, 20, 50, 100]:
        if k <= len(cum_energy):
            ax_energy.axvline(x=k - 1, color='gray', linestyle='--', alpha=0.4)
            ax_energy.text(k - 1, cum_energy[k - 1] + 0.03,
                           f'k={k}\n{cum_energy[k-1]*100:.1f}%',
                           fontsize=6.5, ha='center', color='darkred')
    ax_energy.axhline(y=0.95, color='orange', linestyle=':', alpha=0.8)
    ax_energy.axhline(y=0.99, color='red', linestyle=':', alpha=0.8)
    ax_energy.text(len(S) * 0.6, 0.955, '95% energy', fontsize=7, color='orange')
    ax_energy.text(len(S) * 0.6, 0.995, '99% energy', fontsize=7, color='red')
    ax_energy.set_xlabel('k (number of singular values kept)')
    ax_energy.set_ylabel('Cumulative Energy Ratio')
    ax_energy.set_title('Cumulative Energy vs. k')
    ax_energy.grid(True, alpha=0.3)

    # --- 第二、三行：不同 k 的重建图像 ---
    plot_ks = [1, 2, 3, 5, 10, 20, 30, 50, 100, 256]
    plot_ks = [k for k in plot_ks if k <= len(S)]

    for i, k in enumerate(plot_ks):
        row = 1 + i // 5
        col = i % 5
        ax = fig.add_subplot(gs[row, col])

        recon = reconstruct(U, S, Vt, k)
        ax.imshow(recon, cmap='gray', vmin=0, vmax=1)
        ax.set_title(f'k = {k}', fontsize=9)

        # 压缩比
        original_params = m * n
        svd_params = k * (m + n + 1)
        ratio = original_params / svd_params
        ax.set_xlabel(f'CR: {ratio:.1f}×', fontsize=7)
        ax.set_xticks([])
        ax.set_yticks([])

    fig.suptitle('SVD Image Compression Demo — Low-Rank Approximation',
                 fontsize=14, fontweight='bold', y=1.01)

    # 保存
    output_path = 'svd_compression_demo.png'
    plt.savefig(output_path, dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    print(f"\n  图表已保存至: {output_path}")

    # ---------- 3.4 输出统计 ----------
    print("\n[4] 压缩统计:")
    print(f"  {'k':>5}  {'累积能量':>10}  {'压缩比':>8}  {'存储参数':>10}")
    print("  " + "-" * 40)
    for k in plot_ks:
        energy = cum_energy[k - 1] * 100
        svd_params = k * (m + n + 1)
        ratio = m * n / svd_params
        print(f"  {k:>5}  {energy:>9.2f}%  {ratio:>7.1f}×  {svd_params:>8}")

    # ---------- 3.5 找到 95% / 99% 能量所需的最小 k ----------
    k95 = np.searchsorted(cum_energy, 0.95) + 1
    k99 = np.searchsorted(cum_energy, 0.99) + 1
    print(f"\n  95% 能量需要 k ≥ {k95}")
    print(f"  99% 能量需要 k ≥ {k99}")
    print(f"  总奇异值数: {len(S)}")

    plt.show()


if __name__ == '__main__':
    main()
