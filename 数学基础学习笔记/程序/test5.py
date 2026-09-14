import numpy as np
import matplotlib.pyplot as plt

plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'Arial Unicode MS']
plt.rcParams['axes.unicode_minus'] = False  # 解决负号显示问题

# 设置随机种子
np.random.seed(42)

# 真实参数
p_true = 0.6

# 样本量列表
sample_sizes = [10, 50, 100, 500, 1000, 5000]

# 每个样本量重复实验次数
n_experiments = 500

# 存储结果
results = {}

for N in sample_sizes:
    # 进行 n_experiments 次实验，每次实验独立估计 p
    estimates = []
    for _ in range(n_experiments):
        # 1) 投硬币实验：生成 N 次伯努利结果（0/1）
        samples = np.random.binomial(n=1, p=p_true, size=N)

        # 2) MLE 估计：p_hat = 出现正面的次数 / 总次数
        p_hat = np.mean(samples)
        estimates.append(p_hat)

    # 转为数组便于计算
    estimates = np.array(estimates)

    # 保存统计量
    results[N] = {
        'estimates': estimates,
        'mean': np.mean(estimates),
        'std': np.std(estimates),
        'bias': np.mean(estimates) - p_true,
        'mse': np.mean((estimates - p_true) ** 2)
    }

# --- 打印统计结果 ---
print("样本量 N | 估计均值 | 标准差 | 偏差 | MSE")
print("---------|---------|--------|------|-----")
for N in sample_sizes:
    stats = results[N]
    print(f"{N:>8} | {stats['mean']:.4f} | {stats['std']:.4f} | "
          f"{stats['bias']:+.4f} | {stats['mse']:.6f}")

# --- 可视化：估计值分布随样本量变化 ---
fig, axes = plt.subplots(2, 3, figsize=(15, 8))
axes = axes.flatten()

for idx, N in enumerate(sample_sizes):
    ax = axes[idx]
    estimates = results[N]['estimates']

    # 直方图
    ax.hist(estimates, bins=20, density=True, alpha=0.7, edgecolor='black')

    # 真实值竖线
    ax.axvline(p_true, color='red', linestyle='--', label=f'True p={p_true}')

    # 估计均值竖线
    ax.axvline(results[N]['mean'], color='blue', linestyle='-', label=f'Mean={results[N]["mean"]:.3f}')

    ax.set_title(f'N = {N}')
    ax.set_xlabel(r'$\hat{p}$')
    ax.set_ylabel('Density')
    ax.legend()
    ax.grid(alpha=0.3)

plt.tight_layout()
plt.show()

# --- 额外：标准差随样本量变化（收敛性） ---
std_values = [results[N]['std'] for N in sample_sizes]

plt.figure(figsize=(8, 5))
plt.plot(sample_sizes, std_values, 'o-', linewidth=2, markersize=8)
plt.xlabel('Sample Size (N)')
plt.ylabel('Standard Deviation of MLE')
plt.title('MLE 估计的标准差随样本量增加而下降（$\\propto 1/\\sqrt{N}$）')
plt.grid(True, alpha=0.3)
plt.xscale('log')  # 对数坐标更明显
plt.show()