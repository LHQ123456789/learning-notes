import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import make_classification
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler


class LogisticRegressionFromScratch:
    def __init__(self, learning_rate=0.01, n_iterations=1000, add_bias=True):
        """
        从零实现的逻辑回归

        Parameters:
        -----------
        learning_rate : float, 学习率
        n_iterations : int, 迭代次数
        add_bias : bool, 是否添加偏置项（截距）
        """
        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.add_bias = add_bias
        self.weights = None
        self.bias = None
        self.loss_history = []

    def _sigmoid(self, z):
        """
        Sigmoid 函数: σ(z) = 1/(1+e^(-z))
        数值稳定性处理：对 z 的极端值做裁剪
        """
        # 防止溢出：对大于 500 或小于 -500 的值做裁剪
        z = np.clip(z, -500, 500)
        return 1 / (1 + np.exp(-z))

    def _compute_loss(self, y_true, y_pred):
        """
        计算交叉熵损失（带数值稳定性）
        L = -[y*log(ŷ) + (1-y)*log(1-ŷ)]
        """
        # 防止 log(0) 的情况
        eps = 1e-15
        y_pred = np.clip(y_pred, eps, 1 - eps)

        # 批量计算损失
        loss = -np.mean(y_true * np.log(y_pred) + (1 - y_true) * np.log(1 - y_pred))
        return loss

    def fit(self, X, y, verbose=True):
        """
        使用梯度下降训练模型

        Parameters:
        -----------
        X : array-like, shape (n_samples, n_features)
            训练数据
        y : array-like, shape (n_samples,)
            目标值（0 或 1）
        verbose : bool, 是否打印训练进度
        """
        n_samples, n_features = X.shape

        # 添加偏置项（在 X 前加一列 1）
        if self.add_bias:
            X = np.hstack([np.ones((n_samples, 1)), X])
            n_features += 1

        # 初始化权重
        self.weights = np.zeros(n_features)

        # 梯度下降
        for iteration in range(self.n_iterations):
            # 前向传播
            z = np.dot(X, self.weights)
            y_pred = self._sigmoid(z)

            # 计算损失
            loss = self._compute_loss(y, y_pred)
            self.loss_history.append(loss)

            # 计算梯度: ∂L/∂θ = (ŷ - y) * x
            # 对于批量的梯度: (1/m) * X^T @ (ŷ - y)
            gradient = (1 / n_samples) * np.dot(X.T, (y_pred - y))

            # 更新权重
            self.weights -= self.learning_rate * gradient

            # 打印进度
            if verbose and (iteration % 100 == 0 or iteration == self.n_iterations - 1):
                print(f"Iteration {iteration:4d}/{self.n_iterations}, Loss: {loss:.6f}")

        return self

    def predict_proba(self, X):
        """
        预测概率 P(y=1|x)
        """
        if self.add_bias:
            X = np.hstack([np.ones((X.shape[0], 1)), X])

        z = np.dot(X, self.weights)
        return self._sigmoid(z)

    def predict(self, X, threshold=0.5):
        """
        预测类别（0 或 1）
        """
        proba = self.predict_proba(X)
        return (proba >= threshold).astype(int)

    def score(self, X, y):
        """
        计算准确率
        """
        y_pred = self.predict(X)
        return np.mean(y_pred == y)

    def plot_loss_curve(self):
        """
        绘制损失下降曲线
        """
        plt.figure(figsize=(8, 5))
        plt.plot(self.loss_history)
        plt.xlabel('Iteration')
        plt.ylabel('Cross-Entropy Loss')
        plt.title('Training Loss over Iterations')
        plt.grid(True, alpha=0.3)
        plt.show()


# ============================================
# 测试代码：使用生成的数据集
# ============================================

def test_logistic_regression():
    """测试从零实现的逻辑回归"""

    print("=" * 60)
    print("测试从零实现的逻辑回归")
    print("=" * 60)

    # 1. 生成数据集
    print("\n1. 生成二分类数据集...")
    X, y = make_classification(
        n_samples=1000,
        n_features=20,
        n_informative=15,
        n_redundant=5,
        n_classes=2,
        random_state=42
    )
    print(f"   样本数: {X.shape[0]}, 特征数: {X.shape[1]}")
    print(f"   类别分布: 类别0={np.sum(y == 0)}, 类别1={np.sum(y == 1)}")

    # 2. 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42
    )
    print(f"\n2. 数据集划分: 训练集 {len(X_train)} 样本, 测试集 {len(X_test)} 样本")

    # 3. 标准化（逻辑回归对特征尺度敏感）
    print("\n3. 标准化特征...")
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # 4. 训练模型
    print("\n4. 训练模型...")
    print("-" * 40)
    model = LogisticRegressionFromScratch(
        learning_rate=0.1,
        n_iterations=1000
    )
    model.fit(X_train_scaled, y_train, verbose=True)

    # 5. 评估模型
    print("\n" + "-" * 40)
    print("5. 模型评估:")
    train_acc = model.score(X_train_scaled, y_train)
    test_acc = model.score(X_test_scaled, y_test)
    print(f"   训练集准确率: {train_acc:.4f}")
    print(f"   测试集准确率:  {test_acc:.4f}")

    # 6. 预测概率示例
    print("\n6. 预测示例（前5个测试样本）:")
    probas = model.predict_proba(X_test_scaled[:5])
    preds = model.predict(X_test_scaled[:5])
    for i in range(5):
        print(f"   样本{i + 1}: 真实={y_test[i]}, 预测={preds[i]}, 概率={probas[i]:.4f}")

    # 7. 绘制损失曲线
    print("\n7. 绘制损失曲线...")
    model.plot_loss_curve()

    # 8. 与 sklearn 对比
    print("\n8. 与 sklearn 的逻辑回归对比:")
    from sklearn.linear_model import LogisticRegression as SKLR
    sk_model = SKLR(max_iter=1000, random_state=42)
    sk_model.fit(X_train_scaled, y_train)
    sk_test_acc = sk_model.score(X_test_scaled, y_test)
    print(f"   sklearn 测试集准确率: {sk_test_acc:.4f}")
    print(f"   我们自己实现的准确率: {test_acc:.4f}")
    print(f"   差距: {abs(sk_test_acc - test_acc):.4f}")

    return model


if __name__ == "__main__":
    model = test_logistic_regression()