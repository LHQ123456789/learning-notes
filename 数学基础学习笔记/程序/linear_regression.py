import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import make_regression
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


class LinearRegressionFromScratch:
    """从零实现的线性回归（NumPy only）"""

    def __init__(self, method='normal', learning_rate=0.01, n_iterations=1000,
                 batch_size=None, tolerance=1e-6):
        """
        Parameters:
        - method: 'normal' 或 'gradient' 或 'sgd'
        - learning_rate: 学习率（仅梯度下降）
        - n_iterations: 迭代次数（仅梯度下降）
        - batch_size: 批量大小（None表示全批量，1表示SGD）
        - tolerance: 收敛阈值
        """
        self.method = method
        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.batch_size = batch_size
        self.tolerance = tolerance
        self.theta = None
        self.loss_history = []

    def _add_bias_column(self, X):
        """添加偏置列（全1列）"""
        if X.ndim == 1:
            X = X.reshape(-1, 1)
        return np.hstack([np.ones((X.shape[0], 1)), X])

    def _compute_loss(self, X, y, theta):
        """计算MSE损失"""
        n = len(y)
        y_pred = X @ theta
        return np.mean((y - y_pred) ** 2) / 2  # 除以2便于梯度计算

    def _compute_gradient(self, X, y, theta, indices=None):
        """计算梯度"""
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
        """正规方程求解"""
        # 添加偏置列
        X_with_bias = self._add_bias_column(X)

        # 正规方程: θ = (XᵀX)⁻¹Xᵀy
        try:
            self.theta = np.linalg.inv(X_with_bias.T @ X_with_bias) @ X_with_bias.T @ y
        except np.linalg.LinAlgError:
            # 如果矩阵奇异，使用伪逆
            print("矩阵奇异，使用伪逆求解")
            self.theta = np.linalg.pinv(X_with_bias.T @ X_with_bias) @ X_with_bias.T @ y

        # 计算最终损失
        self.loss_history = [self._compute_loss(X_with_bias, y, self.theta)]
        return self

    def fit_gradient(self, X, y):
        """梯度下降求解（支持批量/小批量/SGD）"""
        X_with_bias = self._add_bias_column(X)
        n_samples = X_with_bias.shape[0]
        n_features = X_with_bias.shape[1]

        # 初始化参数
        self.theta = np.random.randn(n_features) * 0.01
        self.loss_history = []

        # 确定batch size
        if self.batch_size is None:
            # 全批量梯度下降
            batch_size = n_samples
        elif self.batch_size == 1:
            # SGD
            batch_size = 1
        else:
            batch_size = min(self.batch_size, n_samples)

        for iteration in range(self.n_iterations):
            # 随机打乱数据（用于SGD/小批量）
            if batch_size < n_samples:
                indices = np.random.permutation(n_samples)
            else:
                indices = np.arange(n_samples)

            # 计算梯度
            if batch_size == n_samples:
                # 全批量
                gradient = self._compute_gradient(X_with_bias, y, self.theta)
            else:
                # 小批量或SGD
                batch_indices = indices[:batch_size]
                gradient = self._compute_gradient(X_with_bias, y, self.theta, batch_indices)

            # 更新参数
            self.theta -= self.learning_rate * gradient

            # 记录损失
            loss = self._compute_loss(X_with_bias, y, self.theta)
            self.loss_history.append(loss)

            # 检查收敛
            if iteration > 0 and abs(self.loss_history[-1] - self.loss_history[-2]) < self.tolerance:
                print(f"在第 {iteration} 次迭代收敛")
                break

        return self

    def fit(self, X, y):
        """统一的fit接口"""
        if self.method == 'normal':
            return self.fit_normal(X, y)
        elif self.method in ['gradient', 'sgd']:
            return self.fit_gradient(X, y)
        else:
            raise ValueError(f"未知方法: {self.method}")

    def predict(self, X):
        """预测"""
        if self.theta is None:
            raise ValueError("请先训练模型！")
        X_with_bias = self._add_bias_column(X)
        return X_with_bias @ self.theta

    def score(self, X, y):
        """计算R²分数"""
        y_pred = self.predict(X)
        ss_total = np.sum((y - np.mean(y)) ** 2)
        ss_residual = np.sum((y - y_pred) ** 2)
        return 1 - (ss_residual / ss_total)


# ========== 生成实验数据 ==========
def generate_comparison_data():
    """生成对比数据"""
    np.random.seed(42)

    # 1. 简单线性数据（无噪声，易拟合）
    X_simple = np.random.randn(100, 1)
    y_simple = 3 * X_simple.squeeze() + 2 + np.random.randn(100) * 0.1

    # 2. 多维特征数据（修正：只返回X和y）
    X_multi, y_multi = make_regression(
        n_samples=200,
        n_features=5,
        n_informative=3,
        noise=10.0,
        random_state=42
    )

    # 3. 病态数据（接近奇异）
    X_ill = np.random.randn(100, 3)
    X_ill[:, 2] = X_ill[:, 0] + X_ill[:, 1] + np.random.randn(100) * 0.01  # 几乎线性相关
    y_ill = 2 * X_ill[:, 0] + 3 * X_ill[:, 1] + 1 * X_ill[:, 2] + np.random.randn(100) * 0.1

    return {
        'simple': (X_simple, y_simple),
        'multi': (X_multi, y_multi),
        'ill_conditioned': (X_ill, y_ill)
    }


# ========== 对比实验 ==========
def compare_methods(X, y, dataset_name):
    """对比正规方程和梯度下降"""
    print(f"\n{'=' * 60}")
    print(f"数据集: {dataset_name}")
    print(f"样本数: {X.shape[0]}, 特征数: {X.shape[1]}")
    print('=' * 60)

    # 数据标准化（对梯度下降很重要）
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 分割数据
    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled, y, test_size=0.2, random_state=42
    )

    results = {}

    # 1. 正规方程
    print("\n【正规方程求解】")
    model_normal = LinearRegressionFromScratch(method='normal')
    model_normal.fit(X_train, y_train)
    y_pred_normal = model_normal.predict(X_test)

    print(f"参数: {model_normal.theta}")
    print(f"训练损失: {model_normal.loss_history[-1]:.6f}")
    print(f"R²分数: {model_normal.score(X_test, y_test):.6f}")
    results['normal'] = model_normal

    # 2. 批量梯度下降
    print("\n【批量梯度下降】")
    model_gd = LinearRegressionFromScratch(
        method='gradient',
        learning_rate=0.1,
        n_iterations=1000,
        batch_size=None  # 全批量
    )
    model_gd.fit(X_train, y_train)
    y_pred_gd = model_gd.predict(X_test)

    print(f"参数: {model_gd.theta}")
    print(f"训练损失: {model_gd.loss_history[-1]:.6f}")
    print(f"R²分数: {model_gd.score(X_test, y_test):.6f}")
    print(f"收敛迭代次数: {len(model_gd.loss_history)}")
    results['gd'] = model_gd

    # 3. SGD（随机梯度下降）
    print("\n【SGD (随机梯度下降)】")
    model_sgd = LinearRegressionFromScratch(
        method='gradient',
        learning_rate=0.01,
        n_iterations=2000,
        batch_size=1
    )
    model_sgd.fit(X_train, y_train)
    y_pred_sgd = model_sgd.predict(X_test)

    print(f"参数: {model_sgd.theta}")
    print(f"训练损失: {model_sgd.loss_history[-1]:.6f}")
    print(f"R²分数: {model_sgd.score(X_test, y_test):.6f}")
    print(f"收敛迭代次数: {len(model_sgd.loss_history)}")
    results['sgd'] = model_sgd

    # 4. 小批量梯度下降
    print("\n【小批量梯度下降 (batch_size=32)】")
    model_minibatch = LinearRegressionFromScratch(
        method='gradient',
        learning_rate=0.05,
        n_iterations=1000,
        batch_size=32
    )
    model_minibatch.fit(X_train, y_train)
    y_pred_minibatch = model_minibatch.predict(X_test)

    print(f"参数: {model_minibatch.theta}")
    print(f"训练损失: {model_minibatch.loss_history[-1]:.6f}")
    print(f"R²分数: {model_minibatch.score(X_test, y_test):.6f}")
    print(f"收敛迭代次数: {len(model_minibatch.loss_history)}")
    results['minibatch'] = model_minibatch

    # ========== 参数对比 ==========
    print("\n【参数差异对比】")
    print(f"正规方程 vs 批量GD: {np.linalg.norm(model_normal.theta - model_gd.theta):.6f}")
    print(f"正规方程 vs SGD: {np.linalg.norm(model_normal.theta - model_sgd.theta):.6f}")
    print(f"正规方程 vs 小批量GD: {np.linalg.norm(model_normal.theta - model_minibatch.theta):.6f}")

    # 检查参数相对差异
    theta_norm = np.linalg.norm(model_normal.theta)
    if theta_norm > 1e-6:
        rel_diff_gd = np.linalg.norm(model_normal.theta - model_gd.theta) / theta_norm
        rel_diff_sgd = np.linalg.norm(model_normal.theta - model_sgd.theta) / theta_norm
        rel_diff_minibatch = np.linalg.norm(model_normal.theta - model_minibatch.theta) / theta_norm
        print(f"批量GD相对差异: {rel_diff_gd:.6f}")
        print(f"SGD相对差异: {rel_diff_sgd:.6f}")
        print(f"小批量GD相对差异: {rel_diff_minibatch:.6f}")

    return results


# ========== 可视化 ==========
def plot_comparison(models, dataset_name):
    """可视化对比结果"""
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    fig.suptitle(f'线性回归方法对比 - {dataset_name}', fontsize=16)

    # 1. 损失收敛曲线
    ax1 = axes[0, 0]
    for name, model in models.items():
        if name == 'normal' or len(model.loss_history) == 0:
            continue
        ax1.plot(model.loss_history, label=name.upper(), alpha=0.7)
    ax1.set_xlabel('迭代次数')
    ax1.set_ylabel('损失 (MSE/2)')
    ax1.set_title('损失收敛曲线')
    ax1.legend()
    ax1.grid(True, alpha=0.3)

    # 2. 参数直方图对比
    ax2 = axes[0, 1]
    n_params = min(5, len(models['normal'].theta))
    x = np.arange(n_params)
    width = 0.2

    colors = {'normal': 'red', 'gd': 'blue', 'sgd': 'green', 'minibatch': 'orange'}
    labels = {'normal': '正规方程', 'gd': '批量GD', 'sgd': 'SGD', 'minibatch': '小批量GD'}

    for i, (name, model) in enumerate(models.items()):
        if name not in labels:
            continue
        ax2.bar(x + i * width, model.theta[:n_params], width,
                label=labels[name], alpha=0.7, color=colors[name])

    ax2.set_xlabel('参数索引')
    ax2.set_ylabel('参数值')
    ax2.set_title(f'参数对比 (前{n_params}个)')
    ax2.legend()
    ax2.grid(True, alpha=0.3)

    # 3. 参数稳定性（显示最终参数对比）
    ax3 = axes[1, 0]
    ax3.axis('off')

    text = "最终参数对比:\n\n"
    for name, model in models.items():
        if name not in labels:
            continue
        text += f"{labels[name]}: \n  {model.theta[:5]}\n\n"

    ax3.text(0.1, 0.5, text, fontsize=10, transform=ax3.transAxes,
             verticalalignment='center', fontfamily='monospace')
    ax3.set_title('参数值对比')

    # 4. 参数差异条形图
    ax4 = axes[1, 1]
    param_diffs = []
    param_labels = []

    for name in ['gd', 'sgd', 'minibatch']:
        if name in models:
            diff = np.linalg.norm(models['normal'].theta - models[name].theta)
            param_diffs.append(diff)
            param_labels.append(labels[name])

    if param_diffs:
        bars = ax4.bar(param_labels, param_diffs, color=['blue', 'green', 'orange'])
        ax4.set_ylabel('参数差异 (L2范数)')
        ax4.set_title('与正规方程的参数差异')
        ax4.grid(True, alpha=0.3)

        # 在柱状图上显示数值
        for bar, value in zip(bars, param_diffs):
            height = bar.get_height()
            ax4.text(bar.get_x() + bar.get_width() / 2., height,
                     f'{value:.4f}', ha='center', va='bottom')

    plt.tight_layout()
    plt.show()


# ========== 运行实验 ==========
if __name__ == "__main__":
    # 生成数据
    print("生成数据...")
    data = generate_comparison_data()

    # 对每个数据集进行对比
    results = {}
    for name, (X, y) in data.items():
        models = compare_methods(X, y, name)
        results[name] = models
        # 可视化（可以注释掉避免弹窗过多）
        # plot_comparison(models, name)

    # ========== 关键观察 ==========
    print("\n" + "=" * 60)
    print("关键观察总结")
    print("=" * 60)
    print("""
    1. 正规方程:
       - 优点: 精确解，无需调参，一次计算
       - 缺点: O(n³)复杂度，大数据集无法计算，数值不稳定

    2. 批量梯度下降:
       - 优点: 稳定收敛，每步使用全部数据
       - 缺点: 计算量大，可能陷入局部最优（对凸问题没问题）

    3. SGD:
       - 优点: 快，适合大数据，逃离局部最优
       - 缺点: 噪声大，收敛不稳定

    4. 小批量梯度下降:
       - 优点: 折中方案，利用矩阵运算加速
       - 缺点: 需要调batch size

    参数差异原因:
    - 梯度下降可能不收敛到精确解（学习率/迭代次数限制）
    - 数据标准化对梯度下降至关重要
    - 病态矩阵会让正规方程不稳定，而梯度下降可能更鲁棒
    """)