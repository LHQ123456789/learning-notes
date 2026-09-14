import numpy as np
import matplotlib.pyplot as plt

# 设置中文字体（解决Mac/Win中文显示问题，若报错可删掉下面两行）
plt.rcParams['font.sans-serif'] = ['Arial Unicode MS', 'SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def kl_divergence(p, q, eps=1e-12):
    """
    计算 KL(P || Q)
    添加极小值 eps 防止除以 0 或 log(0)
    """
    p = np.array(p)
    q = np.array(q)
    # 为了保证数学严谨，我们过滤掉 p=0 的项（因为 0*log(0/q)=0）
    # 但若 q=0 且 p>0，则 KL 应为无穷大，这里用 eps 近似极大值
    q = np.maximum(q, eps)  # 避免分母为0
    p = np.maximum(p, eps)  # 避免 log(0)（但乘以p后极限为0，这里为了计算方便）
    # 注意：标准的 KL 若 p>0 且 q=0 应为 inf，这里用eps表示数值上极大
    return np.sum(p * np.log(p / q))

# 定义两个不同的分布
P = np.array([0.7, 0.2, 0.1])  # 真实分布（倾向于第一类）
Q = np.array([0.4, 0.4, 0.2])  # 预测分布（比较平滑）

kl_PQ = kl_divergence(P, Q)
kl_QP = kl_divergence(Q, P)

print("="*50)
print("KL 散度非对称性验证")
print("="*50)
print(f"分布 P: {P}")
print(f"分布 Q: {Q}")
print(f"\nD_KL(P || Q) = {kl_PQ:.4f}")
print(f"D_KL(Q || P) = {kl_QP:.4f}")
print(f"\n不对称差值: {kl_PQ - kl_QP:.4f} (非零，证明不对称)")

# 可视化对比
plt.figure(figsize=(12, 5))

plt.subplot(1, 2, 1)
labels = ['类别1', '类别2', '类别3']
x = np.arange(len(labels))
width = 0.35
plt.bar(x - width/2, P, width, label='分布 P (真实)', color='steelblue')
plt.bar(x + width/2, Q, width, label='分布 Q (预测)', color='orange')
plt.xticks(x, labels)
plt.ylim(0, 1)
plt.title('原始分布对比')
plt.legend()

plt.subplot(1, 2, 2)
kl_values = [kl_PQ, kl_QP]
plt.bar(['D_KL(P || Q)', 'D_KL(Q || P)'], kl_values, color=['coral', 'lightseagreen'])
plt.ylabel('KL 散度值')
plt.title(f'非对称性：差值 = {kl_PQ - kl_QP:.2f}')
plt.grid(axis='y', alpha=0.3)

plt.tight_layout()
plt.show()

# ---------- 极端非对称演示（惩罚差异） ----------
print("\n" + "="*50)
print("极端非对称演示（为什么前向 KL 惩罚不同？）")
print("="*50)
P_extreme = np.array([0.98, 0.01, 0.01])
Q_extreme = np.array([0.33, 0.33, 0.34])

kl1 = kl_divergence(P_extreme, Q_extreme)  # P有质量的地方，Q必须也有
kl2 = kl_divergence(Q_extreme, P_extreme)  # Q分散，P很集中

print(f"P (极集中): {P_extreme}")
print(f"Q (极均匀): {Q_extreme}")
print(f"D_KL(P||Q) = {kl1:.4f}  (非常大，因为 Q 没有覆盖 P 的高概率区域)")
print(f"D_KL(Q||P) = {kl2:.4f}  (相对较小，因为 P 覆盖了 Q 的支撑集)")
print(">>> 机器学习启示：最小化 D_KL(P_data || P_model) 会迫使模型覆盖所有真实数据的模式（Mean-Seeking）；")
print("    而最小化 D_KL(P_model || P_data) 会迫使模型集中在数据分布的最高峰（Mode-Seeking）。")