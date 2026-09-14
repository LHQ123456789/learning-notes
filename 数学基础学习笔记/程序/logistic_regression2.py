import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import make_blobs, make_moons
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from matplotlib.colors import ListedColormap


# ============================================
# 从零实现的逻辑回归（复用之前代码，添加了一些可视化支持）
# ============================================

class LogisticRegressionFromScratch:
    def __init__(self, learning_rate=0.01, n_iterations=1000, add_bias=True):
        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.add_bias = add_bias
        self.weights = None
        self.loss_history = []
        self.param_history = []  # 记录参数变化，用于可视化

    def _sigmoid(self, z):
        z = np.clip(z, -500, 500)
        return 1 / (1 + np.exp(-z))

    def _compute_loss(self, y_true, y_pred):
        eps = 1e-15
        y_pred = np.clip(y_pred, eps, 1 - eps)
        return -np.mean(y_true * np.log(y_pred) + (1 - y_true) * np.log(1 - y_pred))

    def fit(self, X, y, verbose=False):
        n_samples, n_features = X.shape

        if self.add_bias:
            X = np.hstack([np.ones((n_samples, 1)), X])
            n_features += 1

        self.weights = np.zeros(n_features)
        self.loss_history = []
        self.param_history = []

        for iteration in range(self.n_iterations):
            # 前向传播
            z = np.dot(X, self.weights)
            y_pred = self._sigmoid(z)

            # 计算损失
            loss = self._compute_loss(y, y_pred)
            self.loss_history.append(loss)

            # 记录参数（每10次记录一次，节省内存）
            if iteration % 10 == 0:
                self.param_history.append(self.weights.copy())

            # 计算梯度
            gradient = (1 / n_samples) * np.dot(X.T, (y_pred - y))

            # 更新权重
            self.weights -= self.learning_rate * gradient

            if verbose and (iteration % 100 == 0):
                print(f"Iteration {iteration:4d}, Loss: {loss:.6f}")

        return self

    def predict_proba(self, X):
        if self.add_bias:
            X = np.hstack([np.ones((X.shape[0], 1)), X])
        z = np.dot(X, self.weights)
        return self._sigmoid(z)

    def predict(self, X, threshold=0.5):
        proba = self.predict_proba(X)
        return (proba >= threshold).astype(int)

    def score(self, X, y):
        y_pred = self.predict(X)
        return np.mean(y_pred == y)


# ============================================
# 可视化工具函数
# ============================================

def plot_decision_boundary(model, X, y, title="Decision Boundary", ax=None):
    """
    绘制决策边界和训练数据
    """
    if ax is None:
        fig, ax = plt.subplots(figsize=(8, 6))

    # 创建网格
    x_min, x_max = X[:, 0].min() - 0.5, X[:, 0].max() + 0.5
    y_min, y_max = X[:, 1].min() - 0.5, X[:, 1].max() + 0.5
    h = 0.02  # 步长
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))

    # 预测网格点的类别
    grid_points = np.c_[xx.ravel(), yy.ravel()]

    # 标准化（使用训练数据的均值和标准差）
    # 注意：这里假设数据已经标准化，或者我们传入原始数据
    Z = model.predict(grid_points)
    Z = Z.reshape(xx.shape)

    # 绘制决策边界
    custom_cmap = ListedColormap(['#FFAAAA', '#AAAAFF'])
    ax.contourf(xx, yy, Z, alpha=0.3, cmap=custom_cmap)

    # 绘制数据点
    scatter = ax.scatter(X[:, 0], X[:, 1], c=y,
                         cmap=ListedColormap(['#FF0000', '#0000FF']),
                         edgecolors='black', linewidth=0.5, alpha=0.8)

    ax.set_xlabel('Feature 1')
    ax.set_ylabel('Feature 2')
    ax.set_title(title)
    ax.legend(*scatter.legend_elements(), title="Classes")

    return ax


def plot_loss_curves_comparison(loss_histories, learning_rates, title="Loss Curves Comparison"):
    """
    对比不同学习率的损失曲线
    """
    fig, ax = plt.subplots(figsize=(10, 6))

    colors = ['blue', 'green', 'red', 'orange', 'purple']
    for i, (lr, loss_hist) in enumerate(zip(learning_rates, loss_histories)):
        ax.plot(loss_hist, label=f'LR = {lr}', color=colors[i % len(colors)], linewidth=2)

    ax.set_xlabel('Iteration')
    ax.set_ylabel('Cross-Entropy Loss')
    ax.set_title(title)
    ax.legend()
    ax.grid(True, alpha=0.3)
    ax.set_yscale('log')  # 对数尺度，更清晰

    return ax


def visualize_training_progress(model, X, y, title="Training Progress",
                                save_frames=False):
    """
    可视化训练过程中决策边界的变化
    """
    n_params = len(model.param_history)
    n_cols = 5
    n_rows = (n_params + n_cols - 1) // n_cols

    fig, axes = plt.subplots(n_rows, n_cols, figsize=(15, 3 * n_rows))
    axes = axes.flatten()

    # 保存当前权重
    original_weights = model.weights.copy()

    for idx, weights in enumerate(model.param_history[::max(1, n_params // 20)]):
        # 临时设置权重
        model.weights = weights
        plot_decision_boundary(model, X, y,
                               title=f"Iter {idx * 10}",
                               ax=axes[idx])

    # 隐藏多余的子图
    for idx in range(len(model.param_history[::max(1, n_params // 20)]), len(axes)):
        axes[idx].axis('off')

    # 恢复权重
    model.weights = original_weights

    plt.tight_layout()
    return fig


# ============================================
# 主实验函数
# ============================================

def experiment_on_dataset(X, y, dataset_name, learning_rates=[0.01, 0.05, 0.1, 0.5, 1.0]):
    """
    在给定数据集上运行实验
    """
    print(f"\n{'=' * 60}")
    print(f"数据集: {dataset_name}")
    print(f"{'=' * 60}")
    print(f"样本数: {X.shape[0]}, 特征数: {X.shape[1]}")
    print(f"类别分布: {np.bincount(y)}")

    # 标准化
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled, y, test_size=0.3, random_state=42
    )

    # 存储结果
    models = []
    loss_histories = []
    train_accs = []
    test_accs = []

    # 对不同学习率进行训练
    for lr in learning_rates:
        print(f"\n训练学习率 = {lr}...")

        model = LogisticRegressionFromScratch(
            learning_rate=lr,
            n_iterations=500
        )
        model.fit(X_train, y_train, verbose=False)

        train_acc = model.score(X_train, y_train)
        test_acc = model.score(X_test, y_test)

        models.append(model)
        loss_histories.append(model.loss_history)
        train_accs.append(train_acc)
        test_accs.append(test_acc)

        print(f"  训练准确率: {train_acc:.4f}, 测试准确率: {test_acc:.4f}")

    # ========== 可视化 ==========

    # 1. 绘制损失曲线对比
    fig1 = plot_loss_curves_comparison(
        loss_histories,
        learning_rates,
        title=f"{dataset_name} - Loss Curves for Different Learning Rates"
    )
    plt.show()

    # 2. 绘制最终决策边界（最佳学习率）
    best_idx = np.argmax(test_accs)
    best_model = models[best_idx]
    best_lr = learning_rates[best_idx]

    fig2, ax = plt.subplots(figsize=(8, 6))
    plot_decision_boundary(
        best_model,
        X_scaled,
        y,
        title=f"{dataset_name} - Best Decision Boundary (LR={best_lr}, Acc={test_accs[best_idx]:.4f})"
    )
    plt.show()

    # 3. 绘制训练过程（决策边界变化）
    if len(models) > 0:
        print("\n生成训练过程动画（静态帧）...")
        fig3 = visualize_training_progress(
            models[0],  # 使用第一个模型展示训练过程
            X_scaled,
            y,
            title=f"{dataset_name} - Decision Boundary During Training"
        )
        plt.show()

    # 4. 绘制准确率对比条形图
    fig4, ax = plt.subplots(figsize=(10, 6))
    x_pos = np.arange(len(learning_rates))
    width = 0.35

    ax.bar(x_pos - width / 2, train_accs, width, label='Train Accuracy', alpha=0.8)
    ax.bar(x_pos + width / 2, test_accs, width, label='Test Accuracy', alpha=0.8)

    ax.set_xlabel('Learning Rate')
    ax.set_ylabel('Accuracy')
    ax.set_title(f'{dataset_name} - Accuracy vs Learning Rate')
    ax.set_xticks(x_pos)
    ax.set_xticklabels([f'{lr}' for lr in learning_rates])
    ax.legend()
    ax.grid(True, alpha=0.3, axis='y')

    # 在柱子上显示数值
    for i, (train, test) in enumerate(zip(train_accs, test_accs)):
        ax.text(i - width / 2, train + 0.01, f'{train:.3f}', ha='center', va='bottom', fontsize=9)
        ax.text(i + width / 2, test + 0.01, f'{test:.3f}', ha='center', va='bottom', fontsize=9)

    plt.tight_layout()
    plt.show()

    # 返回结果
    return {
        'models': models,
        'loss_histories': loss_histories,
        'train_accs': train_accs,
        'test_accs': test_accs,
        'best_model': best_model,
        'best_lr': best_lr
    }


# ============================================
# 主程序：运行所有实验
# ============================================

def main():
    print("=" * 70)
    print("逻辑回归实验：不同数据集 × 不同学习率")
    print("=" * 70)

    # 1. 生成 make_blobs 数据集（线性可分）
    print("\n生成 make_blobs 数据集...")
    X_blobs, y_blobs = make_blobs(
        n_samples=500,
        n_features=2,
        centers=2,
        cluster_std=1.5,
        random_state=42
    )

    # 2. 生成 make_moons 数据集（非线性可分）
    print("\n生成 make_moons 数据集...")
    X_moons, y_moons = make_moons(
        n_samples=500,
        noise=0.2,
        random_state=42
    )

    # 3. 在 blobs 数据集上实验
    results_blobs = experiment_on_dataset(
        X_blobs, y_blobs,
        dataset_name="make_blobs (Linearly Separable)",
        learning_rates=[0.01, 0.05, 0.1, 0.5, 1.0]
    )

    # 4. 在 moons 数据集上实验
    results_moons = experiment_on_dataset(
        X_moons, y_moons,
        dataset_name="make_moons (Non-linearly Separable)",
        learning_rates=[0.01, 0.05, 0.1, 0.5, 1.0]
    )

    # 5. 总结对比
    print("\n" + "=" * 70)
    print("实验总结")
    print("=" * 70)

    print("\nmake_blobs 数据集最佳结果:")
    best_idx = np.argmax(results_blobs['test_accs'])
    print(f"  最佳学习率: {results_blobs['best_lr']}")
    print(f"  测试准确率: {results_blobs['test_accs'][best_idx]:.4f}")
    print(f"  训练准确率: {results_blobs['train_accs'][best_idx]:.4f}")

    print("\nmake_moons 数据集最佳结果:")
    best_idx = np.argmax(results_moons['test_accs'])
    print(f"  最佳学习率: {results_moons['best_lr']}")
    print(f"  测试准确率: {results_moons['test_accs'][best_idx]:.4f}")
    print(f"  训练准确率: {results_moons['train_accs'][best_idx]:.4f}")

    print("\n关键观察:")
    print("1. make_blobs（线性可分）: 逻辑回归可以完美或接近完美分类")
    print("2. make_moons（非线性）: 逻辑回归只能学习线性决策边界，效果有限")
    print("3. 学习率影响: 过小收敛慢，过大会震荡甚至发散")
    print("4. 最佳学习率通常介于 0.01-0.1 之间")
    print("\n实验完成！")


if __name__ == "__main__":
    main()