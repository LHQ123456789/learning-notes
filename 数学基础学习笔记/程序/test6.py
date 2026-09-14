import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import StandardScaler


# 设置中文字体（解决Mac/Win中文显示问题，若报错可删掉下面两行）
plt.rcParams['font.sans-serif'] = ['Arial Unicode MS', 'SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

class LinearRegressionFromScratch:
    """从零实现的线性回归（NumPy only）"""

    def __init__(self, method='normal', learning_rate=0.01, n_iterations=1000,
                 batch_size=None, tolerance=1e-6):
        self.method = method
        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.batch_size = batch_size
        self.tolerance = tolerance
        self.theta = None
        self.loss_history = []
        self.theta_history = []  # 记录参数变化

    def _add_bias_column(self, X):
        if X.ndim == 1:
            X = X.reshape(-1, 1)
        return np.hstack([np.ones((X.shape[0], 1)), X])

    def _compute_loss(self, X, y, theta):
        n = len(y)
        y_pred = X @ theta
        return np.mean((y - y_pred) ** 2) / 2

    def _compute_gradient(self, X, y, theta, indices=None):
        if indices is not None:
            X_batch = X[indices]
            y_batch = y[indices]
        else:
            X_batch = X
            y_batch = y

        n = len(y_batch)
        y_pred = X_batch @ theta
        gradient = -(1 / n) * X_batch.T @ (y_batch - y_pred)
        return gradient

    def fit_normal(self, X, y):
        X_with_bias = self._add_bias_column(X)

        try:
            self.theta = np.linalg.inv(X_with_bias.T @ X_with_bias) @ X_with_bias.T @ y
        except np.linalg.LinAlgError:
            print("矩阵奇异，使用伪逆求解")
            self.theta = np.linalg.pinv(X_with_bias.T @ X_with_bias) @ X_with_bias.T @ y

        self.loss_history = [self._compute_loss(X_with_bias, y, self.theta)]
        self.theta_history = [self.theta.copy()]
        return self

    def fit_gradient(self, X, y):
        X_with_bias = self._add_bias_column(X)
        n_samples = X_with_bias.shape[0]
        n_features = X_with_bias.shape[1]

        # 初始化参数
        self.theta = np.random.randn(n_features) * 0.1
        self.loss_history = []
        self.theta_history = [self.theta.copy()]

        if self.batch_size is None:
            batch_size = n_samples
        elif self.batch_size == 1:
            batch_size = 1
        else:
            batch_size = min(self.batch_size, n_samples)

        for iteration in range(self.n_iterations):
            if batch_size < n_samples:
                indices = np.random.permutation(n_samples)
            else:
                indices = np.arange(n_samples)

            if batch_size == n_samples:
                gradient = self._compute_gradient(X_with_bias, y, self.theta)
            else:
                batch_indices = indices[:batch_size]
                gradient = self._compute_gradient(X_with_bias, y, self.theta, batch_indices)

            self.theta -= self.learning_rate * gradient

            loss = self._compute_loss(X_with_bias, y, self.theta)
            self.loss_history.append(loss)
            self.theta_history.append(self.theta.copy())

            if iteration > 0 and abs(self.loss_history[-1] - self.loss_history[-2]) < self.tolerance:
                print(f"在第 {iteration} 次迭代收敛")
                break

        return self

    def fit(self, X, y):
        if self.method == 'normal':
            return self.fit_normal(X, y)
        elif self.method in ['gradient', 'sgd']:
            return self.fit_gradient(X, y)
        else:
            raise ValueError(f"未知方法: {self.method}")

    def predict(self, X):
        if self.theta is None:
            raise ValueError("请先训练模型！")
        X_with_bias = self._add_bias_column(X)
        return X_with_bias @ self.theta

    def score(self, X, y):
        y_pred = self.predict(X)
        ss_total = np.sum((y - np.mean(y)) ** 2)
        ss_residual = np.sum((y - y_pred) ** 2)
        return 1 - (ss_residual / ss_total)


def generate_synthetic_data(n_samples=100, noise_std=0.3, random_seed=42):
    """
    生成合成回归数据

    Parameters:
    - n_samples: 样本数
    - noise_std: 噪声标准差
    - random_seed: 随机种子

    Returns:
    - X, y, true_theta: 特征、目标、真实参数
    """
    np.random.seed(random_seed)

    # 生成特征（一维，方便可视化）
    X = np.random.randn(n_samples, 1)

    # 真实参数: y = 2.5*x + 1.8 + noise
    true_theta = np.array([1.8, 2.5])  # [截距, 斜率]

    # 添加偏置列
    X_with_bias = np.hstack([np.ones((n_samples, 1)), X])

    # 生成目标值（加入噪声）
    y = X_with_bias @ true_theta + np.random.randn(n_samples) * noise_std

    return X, y, true_theta


def plot_fitting_comparison(X, y, true_theta, models_dict, title="拟合曲线对比"):
    """可视化不同方法的拟合曲线"""
    fig, axes = plt.subplots(1, 2, figsize=(14, 5))

    # 生成用于绘图的点
    X_plot = np.linspace(X.min() - 0.5, X.max() + 0.5, 100).reshape(-1, 1)
    X_plot_with_bias = np.hstack([np.ones((100, 1)), X_plot])

    # 左图：拟合曲线对比
    ax1 = axes[0]

    # 绘制真实数据点
    ax1.scatter(X, y, alpha=0.5, s=20, color='gray', label='训练数据')

    # 绘制真实曲线（如果提供了真实参数）
    if true_theta is not None:
        y_true = X_plot_with_bias @ true_theta
        ax1.plot(X_plot, y_true, 'k--', linewidth=2, label='真实曲线', alpha=0.7)

    # 绘制各方法的拟合曲线
    colors = {'normal': 'red', 'gd': 'blue', 'sgd': 'green', 'minibatch': 'orange'}
    for name, model in models_dict.items():
        if model is not None and model.theta is not None:
            y_pred = X_plot_with_bias @ model.theta
            ax1.plot(X_plot, y_pred, color=colors.get(name, 'purple'),
                     linewidth=2, label=name, alpha=0.8)

    ax1.set_xlabel('X')
    ax1.set_ylabel('y')
    ax1.set_title(title)
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # 右图：损失下降曲线（只显示梯度下降方法）
    ax2 = axes[1]

    for name, model in models_dict.items():
        if model is not None and name in ['gd', 'sgd', 'minibatch']:
            if len(model.loss_history) > 0:
                ax2.plot(model.loss_history, label=name, alpha=0.7)

    ax2.set_xlabel('迭代次数')
    ax2.set_ylabel('损失 (MSE/2)')
    ax2.set_title('损失下降曲线')
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.show()


def experiment_learning_rates(X, y, learning_rates, n_iterations=100):
    """
    实验不同学习率对梯度下降收敛的影响

    Parameters:
    - X, y: 训练数据
    - learning_rates: 学习率列表
    - n_iterations: 迭代次数
    """
    print("\n" + "=" * 70)
    print("实验：学习率对梯度下降收敛的影响")
    print("=" * 70)

    # 数据标准化（对梯度下降很重要）
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 存储结果
    results = {}

    # 可视化设置
    fig, axes = plt.subplots(2, 3, figsize=(15, 10))
    fig.suptitle('不同学习率对梯度下降收敛的影响', fontsize=16)

    # 为每个学习率训练模型
    for idx, lr in enumerate(learning_rates):
        row = idx // 3
        col = idx % 3

        print(f"\n学习率: {lr}")

        # 训练模型
        model = LinearRegressionFromScratch(
            method='gradient',
            learning_rate=lr,
            n_iterations=n_iterations,
            batch_size=None  # 批量梯度下降
        )
        model.fit(X_scaled, y)

        results[lr] = model

        # 计算最终损失和参数
        final_loss = model.loss_history[-1] if model.loss_history else None
        print(f"  最终损失: {final_loss:.6f}")
        print(f"  最终参数: {model.theta}")

        # 绘制损失下降曲线
        ax = axes[row, col]
        ax.plot(model.loss_history, linewidth=2)
        ax.set_xlabel('迭代次数')
        ax.set_ylabel('损失')
        ax.set_title(f'学习率 = {lr}')
        ax.grid(True, alpha=0.3)

        # 标注最终损失
        if model.loss_history:
            ax.axhline(y=model.loss_history[-1], color='r', linestyle='--', alpha=0.5)
            ax.text(0.5, 0.95, f'最终损失: {model.loss_history[-1]:.4f}',
                    transform=ax.transAxes, ha='center', va='top')

        # 检查收敛情况
        if len(model.loss_history) < n_iterations:
            print(f"  ✅ 提前收敛于第 {len(model.loss_history)} 次迭代")
        elif len(model.loss_history) == n_iterations:
            # 检查是否仍在下降
            if len(model.loss_history) > 10:
                recent_improvement = (model.loss_history[-10] - model.loss_history[-1]) / model.loss_history[-10]
                if recent_improvement > 1e-4:
                    print(f"  ⚠️  未收敛，仍在缓慢下降")
                else:
                    print(f"  ✅ 基本收敛")

        # 检查是否发散
        if model.loss_history and np.isnan(model.loss_history[-1]):
            print(f"  ❌ 发散！学习率过大")
        elif model.loss_history and model.loss_history[-1] > 1e10:
            print(f"  ⚠️  损失过大，可能即将发散")

    plt.tight_layout()
    plt.show()

    return results


def visualize_parameter_path(X, y, learning_rates, n_iterations=50):
    """
    可视化参数在等高线图上的优化路径
    """
    # 数据标准化
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    X_with_bias = np.hstack([np.ones((len(X_scaled), 1)), X_scaled])

    # 计算损失函数的等高线
    theta0_vals = np.linspace(-2, 4, 50)
    theta1_vals = np.linspace(-2, 4, 50)
    T0, T1 = np.meshgrid(theta0_vals, theta1_vals)
    Z = np.zeros_like(T0)

    for i in range(T0.shape[0]):
        for j in range(T0.shape[1]):
            theta = np.array([T0[i, j], T1[i, j]])
            Z[i, j] = np.mean((X_with_bias @ theta - y) ** 2) / 2

    fig, axes = plt.subplots(1, len(learning_rates), figsize=(5 * len(learning_rates), 4))
    if len(learning_rates) == 1:
        axes = [axes]

    for idx, lr in enumerate(learning_rates):
        # 训练模型并记录参数路径
        model = LinearRegressionFromScratch(
            method='gradient',
            learning_rate=lr,
            n_iterations=n_iterations,
            batch_size=None
        )
        model.fit(X_scaled, y)

        # 获取参数历史
        theta_history = np.array(model.theta_history)

        # 绘制等高线
        ax = axes[idx]
        contour = ax.contour(T0, T1, Z, levels=20, cmap='viridis', alpha=0.7)
        ax.clabel(contour, inline=True, fontsize=8)

        # 绘制参数路径
        ax.plot(theta_history[:, 0], theta_history[:, 1], 'r-', linewidth=2, alpha=0.7)
        ax.scatter(theta_history[0, 0], theta_history[0, 1], c='green', s=100,
                   marker='o', label='起点', edgecolors='black')
        ax.scatter(theta_history[-1, 0], theta_history[-1, 1], c='red', s=100,
                   marker='*', label='终点', edgecolors='black')

        # 绘制路径上的点
        step = max(1, len(theta_history) // 10)
        ax.scatter(theta_history[::step, 0], theta_history[::step, 1],
                   c='blue', s=20, alpha=0.5)

        ax.set_xlabel(r'$\theta_0$ (截距)')
        ax.set_ylabel(r'$\theta_1$ (斜率)')
        ax.set_title(f'学习率 = {lr}')
        ax.legend()
        ax.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.show()


# ========== 主实验 ==========
if __name__ == "__main__":
    # 1. 生成合成数据
    print("=" * 70)
    print("生成合成回归数据")
    print("=" * 70)
    X, y, true_theta = generate_synthetic_data(n_samples=100, noise_std=0.3)
    print(f"真实参数: 截距={true_theta[0]:.3f}, 斜率={true_theta[1]:.3f}")
    print(f"数据形状: X={X.shape}, y={y.shape}")

    # 2. 正规方程 vs 梯度下降对比
    print("\n" + "=" * 70)
    print("方法对比：正规方程 vs 梯度下降")
    print("=" * 70)

    # 数据标准化
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 正规方程
    model_normal = LinearRegressionFromScratch(method='normal')
    model_normal.fit(X_scaled, y)
    print(f"\n正规方程:")
    print(f"  参数: {model_normal.theta}")
    print(f"  损失: {model_normal.loss_history[-1]:.6f}")

    # 批量梯度下降
    model_gd = LinearRegressionFromScratch(
        method='gradient',
        learning_rate=0.1,
        n_iterations=1000,
        batch_size=None
    )
    model_gd.fit(X_scaled, y)
    print(f"\n批量梯度下降 (lr=0.1):")
    print(f"  参数: {model_gd.theta}")
    print(f"  损失: {model_gd.loss_history[-1]:.6f}")
    print(f"  迭代次数: {len(model_gd.loss_history)}")

    # 小批量梯度下降
    model_minibatch = LinearRegressionFromScratch(
        method='gradient',
        learning_rate=0.05,
        n_iterations=1000,
        batch_size=32
    )
    model_minibatch.fit(X_scaled, y)
    print(f"\n小批量梯度下降 (lr=0.05, batch=32):")
    print(f"  参数: {model_minibatch.theta}")
    print(f"  损失: {model_minibatch.loss_history[-1]:.6f}")
    print(f"  迭代次数: {len(model_minibatch.loss_history)}")

    # 可视化拟合结果
    models_dict = {
        'normal': model_normal,
        'gd': model_gd,
        'minibatch': model_minibatch
    }

    # 注意：数据已被标准化，我们使用标准化后的数据绘图
    # 但真实参数是在原始尺度上的，所以这里不显示真实曲线
    plot_fitting_comparison(X_scaled, y, None, models_dict,
                            "拟合曲线对比 (标准化数据)")

    # 3. 学习率实验
    print("\n" + "=" * 70)
    print("学习率实验")
    print("=" * 70)

    learning_rates = [0.001, 0.01, 0.05, 0.1, 0.5, 1.0]
    lr_results = experiment_learning_rates(X, y, learning_rates, n_iterations=100)

    # 4. 参数路径可视化（选择几个学习率）
    print("\n" + "=" * 70)
    print("参数优化路径可视化")
    print("=" * 70)

    selected_lrs = [0.01, 0.1, 0.5]
    visualize_parameter_path(X, y, selected_lrs, n_iterations=50)

    # 5. 总结
    print("\n" + "=" * 70)
    print("实验总结")
    print("=" * 70)
    print("""
    学习率选择指南:

    1. 学习率太小 (如 0.001):
       - ✅ 稳定收敛
       - ❌ 收敛速度极慢，需要大量迭代
       - 适用: 精细调优，确保稳定

    2. 学习率适中 (如 0.01-0.1):
       - ✅ 收敛速度快
       - ✅ 通常能达到最优解
       - 推荐: 首选范围

    3. 学习率较大 (如 0.5):
       - ⚠️ 可能震荡但最终收敛
       - ❌ 可能越过最优解
       - 适用: 需要小心监控

    4. 学习率过大 (如 1.0):
       - ❌ 发散，损失爆炸
       - 避免: 导致数值不稳定

    关键观察:
    - 正规方程给出精确解（如果有逆矩阵）
    - 梯度下降需要调参，但适合大规模数据
    - 数据标准化能显著改善梯度下降的收敛
    """)