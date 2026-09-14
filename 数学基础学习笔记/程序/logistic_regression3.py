import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import make_blobs, make_moons
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import seaborn as sns
from matplotlib.colors import ListedColormap


# ============================================
# 从零实现的逻辑回归（简化版，用于对比）
# ============================================

class LogisticRegressionFromScratch:
    def __init__(self, learning_rate=0.1, n_iterations=1000):
        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.weights = None
        self.bias = None
        self.loss_history = []

    def _sigmoid(self, z):
        z = np.clip(z, -500, 500)
        return 1 / (1 + np.exp(-z))

    def fit(self, X, y):
        n_samples, n_features = X.shape
        self.weights = np.zeros(n_features)
        self.bias = 0
        self.loss_history = []

        for _ in range(self.n_iterations):
            # 线性组合
            z = np.dot(X, self.weights) + self.bias
            y_pred = self._sigmoid(z)

            # 损失
            eps = 1e-15
            y_pred_clipped = np.clip(y_pred, eps, 1 - eps)
            loss = -np.mean(y * np.log(y_pred_clipped) + (1 - y) * np.log(1 - y_pred_clipped))
            self.loss_history.append(loss)

            # 梯度
            dw = (1 / n_samples) * np.dot(X.T, (y_pred - y))
            db = (1 / n_samples) * np.sum(y_pred - y)

            # 更新
            self.weights -= self.learning_rate * dw
            self.bias -= self.learning_rate * db

        return self

    def decision_function(self, X):
        """返回线性组合 z = w·x + b"""
        return np.dot(X, self.weights) + self.bias

    def predict_proba(self, X):
        """返回概率 P(y=1|x)"""
        z = self.decision_function(X)
        return self._sigmoid(z)

    def predict(self, X, threshold=0.5):
        proba = self.predict_proba(X)
        return (proba >= threshold).astype(int)


# ============================================
# 对比实验和可视化函数
# ============================================

def compare_models(X, y, dataset_name):
    """
    对比 sklearn 和手写逻辑回归
    """
    print(f"\n{'=' * 70}")
    print(f"数据集: {dataset_name}")
    print(f"{'=' * 70}")

    # 标准化
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # 划分数据
    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled, y, test_size=0.3, random_state=42
    )

    # ===== 1. sklearn 逻辑回归 =====
    print("\n1. sklearn LogisticRegression")
    print("-" * 40)
    sk_model = LogisticRegression(
        C=1.0,  # 正则化强度的倒数
        max_iter=1000,
        random_state=42
    )
    sk_model.fit(X_train, y_train)

    # 预测
    y_pred_sk = sk_model.predict(X_test)
    y_proba_sk = sk_model.predict_proba(X_test)
    y_decision_sk = sk_model.decision_function(X_test)

    # 评估
    acc_sk = accuracy_score(y_test, y_pred_sk)
    print(f"  测试准确率: {acc_sk:.4f}")
    print(f"  权重: {sk_model.coef_[0]}")
    print(f"  偏置: {sk_model.intercept_[0]:.4f}")

    # ===== 2. 手写逻辑回归 =====
    print("\n2. 手写 LogisticRegression")
    print("-" * 40)
    my_model = LogisticRegressionFromScratch(
        learning_rate=0.1,
        n_iterations=1000
    )
    my_model.fit(X_train, y_train)

    # 预测
    y_pred_my = my_model.predict(X_test)
    y_proba_my = my_model.predict_proba(X_test)
    y_decision_my = my_model.decision_function(X_test)

    # 评估
    acc_my = accuracy_score(y_test, y_pred_my)
    print(f"  测试准确率: {acc_my:.4f}")
    print(f"  权重: {my_model.weights}")
    print(f"  偏置: {my_model.bias:.4f}")
    print(f"  最终损失: {my_model.loss_history[-1]:.6f}")

    # ===== 3. 详细对比 =====
    print("\n3. 详细对比分析")
    print("-" * 40)

    # 选择前5个测试样本
    n_samples = min(5, len(X_test))
    print(f"\n前 {n_samples} 个测试样本的预测详情:")
    print(f"{'样本':<8} {'真实':<6} {'sklearn预测':<12} {'手写预测':<12} "
          f"{'sklearn概率':<16} {'手写概率':<16} {'decision_func':<14}")
    print("-" * 90)

    for i in range(n_samples):
        print(f"{i + 1:<8} {y_test[i]:<6} {y_pred_sk[i]:<12} {y_pred_my[i]:<12} "
              f"{y_proba_sk[i, 1]:<16.6f} {y_proba_my[i]:<16.6f} {y_decision_sk[i]:<14.4f}")

    # ===== 4. 可视化对比 =====
    fig, axes = plt.subplots(2, 3, figsize=(15, 10))

    # 4.1 决策边界 - sklearn
    plot_decision_boundary_sklearn(sk_model, X_scaled, y,
                                   ax=axes[0, 0],
                                   title=f'sklearn - Acc={acc_sk:.3f}')

    # 4.2 决策边界 - 手写
    plot_decision_boundary_custom(my_model, X_scaled, y,
                                  ax=axes[0, 1],
                                  title=f'手写 - Acc={acc_my:.3f}')

    # 4.3 损失曲线
    axes[0, 2].plot(my_model.loss_history)
    axes[0, 2].set_xlabel('Iteration')
    axes[0, 2].set_ylabel('Loss')
    axes[0, 2].set_title('Training Loss Curve (手写)')
    axes[0, 2].grid(True, alpha=0.3)

    # 4.4 概率分布对比
    axes[1, 0].scatter(y_decision_sk, y_proba_sk[:, 1], alpha=0.5, label='sklearn')
    axes[1, 0].scatter(y_decision_my, y_proba_my, alpha=0.5, label='手写', marker='x')
    axes[1, 0].set_xlabel('decision_function (z)')
    axes[1, 0].set_ylabel('predict_proba (sigmoid)')
    axes[1, 0].set_title('决策函数 vs 概率')
    axes[1, 0].legend()
    axes[1, 0].grid(True, alpha=0.3)

    # 4.5 混淆矩阵 - sklearn
    cm_sk = confusion_matrix(y_test, y_pred_sk)
    sns.heatmap(cm_sk, annot=True, fmt='d', cmap='Blues', ax=axes[1, 1])
    axes[1, 1].set_title(f'sklearn 混淆矩阵')
    axes[1, 1].set_xlabel('预测')
    axes[1, 1].set_ylabel('真实')

    # 4.6 混淆矩阵 - 手写
    cm_my = confusion_matrix(y_test, y_pred_my)
    sns.heatmap(cm_my, annot=True, fmt='d', cmap='Greens', ax=axes[1, 2])
    axes[1, 2].set_title(f'手写 混淆矩阵')
    axes[1, 2].set_xlabel('预测')
    axes[1, 2].set_ylabel('真实')

    plt.tight_layout()
    plt.show()

    # ===== 5. 深入理解 decision_function 和 predict_proba =====
    print("\n4. 深入理解 decision_function vs predict_proba")
    print("-" * 40)

    # 生成一个决策函数的范围
    z_vals = np.linspace(-10, 10, 100)
    proba_vals = 1 / (1 + np.exp(-z_vals))

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.plot(z_vals, proba_vals, 'b-', linewidth=2, label='sigmoid: σ(z)')
    ax.axhline(y=0.5, color='r', linestyle='--', alpha=0.5, label='阈值 0.5')
    ax.axvline(x=0, color='g', linestyle='--', alpha=0.5, label='z=0')
    ax.scatter(y_decision_sk[:20], y_proba_sk[:20, 1],
               c='red', alpha=0.6, s=50, label='sklearn 样本点')
    ax.set_xlabel('decision_function (z = w·x + b)')
    ax.set_ylabel('predict_proba (σ(z))')
    ax.set_title('Sigmoid 函数: decision_function → predict_proba')
    ax.legend()
    ax.grid(True, alpha=0.3)
    plt.show()

    return {
        'sklearn': {'model': sk_model, 'acc': acc_sk, 'proba': y_proba_sk},
        'custom': {'model': my_model, 'acc': acc_my, 'proba': y_proba_my}
    }


def plot_decision_boundary_sklearn(model, X, y, ax=None, title="Decision Boundary"):
    """绘制 sklearn 模型的决策边界"""
    if ax is None:
        fig, ax = plt.subplots(figsize=(8, 6))

    x_min, x_max = X[:, 0].min() - 0.5, X[:, 0].max() + 0.5
    y_min, y_max = X[:, 1].min() - 0.5, X[:, 1].max() + 0.5
    h = 0.02
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))

    Z = model.predict(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)

    custom_cmap = ListedColormap(['#FFAAAA', '#AAAAFF'])
    ax.contourf(xx, yy, Z, alpha=0.3, cmap=custom_cmap)
    ax.scatter(X[:, 0], X[:, 1], c=y,
               cmap=ListedColormap(['#FF0000', '#0000FF']),
               edgecolors='black', linewidth=0.5, alpha=0.8, s=20)
    ax.set_xlabel('Feature 1')
    ax.set_ylabel('Feature 2')
    ax.set_title(title)
    return ax


def plot_decision_boundary_custom(model, X, y, ax=None, title="Decision Boundary"):
    """绘制手写模型的决策边界"""
    if ax is None:
        fig, ax = plt.subplots(figsize=(8, 6))

    x_min, x_max = X[:, 0].min() - 0.5, X[:, 0].max() + 0.5
    y_min, y_max = X[:, 1].min() - 0.5, X[:, 1].max() + 0.5
    h = 0.02
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))

    Z = model.predict(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)

    custom_cmap = ListedColormap(['#FFAAAA', '#AAAAFF'])
    ax.contourf(xx, yy, Z, alpha=0.3, cmap=custom_cmap)
    ax.scatter(X[:, 0], X[:, 1], c=y,
               cmap=ListedColormap(['#FF0000', '#0000FF']),
               edgecolors='black', linewidth=0.5, alpha=0.8, s=20)
    ax.set_xlabel('Feature 1')
    ax.set_ylabel('Feature 2')
    ax.set_title(title)
    return ax


# ============================================
# 主程序
# ============================================

def main():
    print("=" * 70)
    print("sklearn LogisticRegression 对比实验")
    print("深入理解 predict_proba 和 decision_function")
    print("=" * 70)

    # 1. 生成数据集
    print("\n生成数据集...")

    # make_blobs - 线性可分
    X_blobs, y_blobs = make_blobs(
        n_samples=300,
        n_features=2,
        centers=2,
        cluster_std=1.5,
        random_state=42
    )

    # make_moons - 非线性
    X_moons, y_moons = make_moons(
        n_samples=300,
        noise=0.2,
        random_state=42
    )

    # 2. 在 blobs 上对比
    results_blobs = compare_models(X_blobs, y_blobs, "make_blobs")

    # 3. 在 moons 上对比
    results_moons = compare_models(X_moons, y_moons, "make_moons")

    # 4. 总结
    print("\n" + "=" * 70)
    print("总结和关键发现")
    print("=" * 70)

    print("\n1. decision_function 返回的是 z = w·x + b (线性组合)")
    print("   - 范围: (-∞, +∞)")
    print("   - 正负表示属于正类的信心程度")
    print("   - 值越大，模型越确信样本属于正类")

    print("\n2. predict_proba 返回的是 σ(z) = 1/(1+e^(-z))")
    print("   - 范围: (0, 1)")
    print("   - 是 decision_function 经过 sigmoid 变换")
    print("   - 直接解释为概率")

    print("\n3. 关系: predict_proba = sigmoid(decision_function)")
    print("   - z = 0  → p = 0.5 (决策边界)")
    print("   - z > 0  → p > 0.5 (预测为正类)")
    print("   - z < 0  → p < 0.5 (预测为负类)")

    print("\n4. sklearn vs 手写:")
    print("   - 两者原理完全一致")
    print("   - sklearn 有更多优化（正则化、收敛判据等）")
    print("   - 手写版本帮助理解底层机制")

    print("\n5. 适用场景:")
    print("   - decision_function: 需要原始得分时（如排序、ROC曲线）")
    print("   - predict_proba: 需要概率解释时（如风险评估）")
    print("   - predict: 只需要最终类别时")

    print("\n实验完成！")


if __name__ == "__main__":
    main()