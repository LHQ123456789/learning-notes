import pandas as pd
import numpy as np
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.impute import SimpleImputer
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    classification_report, confusion_matrix, ConfusionMatrixDisplay,
    roc_curve, roc_auc_score, auc
)
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.preprocessing import label_binarize

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei']
plt.rcParams['axes.unicode_minus'] = False

# ======================== 1. 使用鸢尾花数据集 ========================
iris = load_iris()
X = pd.DataFrame(iris.data, columns=iris.feature_names)
y = pd.Series(iris.target, name='label')
y_classes = iris.target_names  # ['setosa', 'versicolor', 'virginica']

print(f"数据集大小: {len(X)} 样本")
print(f"特征: {list(X.columns)}")
print(f"类别分布:\n{pd.Series(y).value_counts().to_dict()}")

# ======================== 2. 预处理（数值特征直接标准化）=======================
preprocessor = ColumnTransformer([
    ('scaler', StandardScaler(), X.columns.tolist())
], remainder='drop')

# ======================== 3. 划分数据 ========================
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.3, random_state=42, stratify=y
)

print(f"\n训练集: {len(X_train)} 样本")
print(f"测试集: {len(X_test)} 样本")

# ======================== 4. 训练模型 ========================
pipeline = Pipeline([
    ('preprocessor', preprocessor),
    ('classifier', RandomForestClassifier(
        n_estimators=50,
        max_depth=5,
        min_samples_split=5,
        min_samples_leaf=2,
        random_state=42
    ))
])

pipeline.fit(X_train, y_train)
y_pred = pipeline.predict(X_test)
y_pred_proba = pipeline.predict_proba(X_test)

# ======================== 5. 评估指标 ========================
print("\n" + "=" * 60)
print("模型评估报告（鸢尾花数据集）")
print("=" * 60)

accuracy = accuracy_score(y_test, y_pred)
precision_macro = precision_score(y_test, y_pred, average='macro', zero_division=0)
recall_macro = recall_score(y_test, y_pred, average='macro', zero_division=0)
f1_macro = f1_score(y_test, y_pred, average='macro', zero_division=0)

print(f"\n【基础指标】")
print(f"准确率 (Accuracy):        {accuracy:.4f}")
print(f"宏平均精确率 (Macro Precision): {precision_macro:.4f}")
print(f"宏平均召回率 (Macro Recall):    {recall_macro:.4f}")
print(f"宏平均F1 (Macro F1):            {f1_macro:.4f}")

print(f"\n【分类报告】")
print(classification_report(y_test, y_pred, target_names=y_classes, zero_division=0))

# ======================== 6. 混淆矩阵 ========================
fig, axes = plt.subplots(1, 2, figsize=(12, 5))

# sklearn 内置
cm = confusion_matrix(y_test, y_pred)
disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=y_classes)
disp.plot(cmap='Blues', ax=axes[0], values_format='d')
axes[0].set_title('混淆矩阵 (sklearn)', fontsize=12)

# seaborn 热力图
sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
            xticklabels=y_classes, yticklabels=y_classes, ax=axes[1])
axes[1].set_xlabel('预测类别')
axes[1].set_ylabel('真实类别')
axes[1].set_title('混淆矩阵 (热力图)', fontsize=12)

plt.tight_layout()
plt.show()

# ======================== 7. ROC-AUC（多分类）=======================
y_test_bin = label_binarize(y_test, classes=[0, 1, 2])

fig, ax = plt.subplots(figsize=(10, 8))

for i, cls in enumerate(y_classes):
    fpr, tpr, _ = roc_curve(y_test_bin[:, i], y_pred_proba[:, i])
    roc_auc = auc(fpr, tpr)
    ax.plot(fpr, tpr, linewidth=2, label=f'{cls} (AUC = {roc_auc:.4f})')

ax.plot([0, 1], [0, 1], 'k--', linewidth=1, label='随机猜测')
ax.set_xlim([0.0, 1.0])
ax.set_ylim([0.0, 1.05])
ax.set_xlabel('假正率 (FPR)')
ax.set_ylabel('真正率 (TPR)')
ax.set_title('鸢尾花 - 多分类 ROC 曲线')
ax.legend(loc='lower right')
ax.grid(True, alpha=0.3)
plt.tight_layout()
plt.show()

# AUC 汇总
auc_macro = roc_auc_score(y_test_bin, y_pred_proba, average='macro', multi_class='ovr')
auc_weighted = roc_auc_score(y_test_bin, y_pred_proba, average='weighted', multi_class='ovr')

print(f"\n【ROC-AUC 汇总】")
print(f"宏平均 AUC: {auc_macro:.4f}")
print(f"加权平均 AUC: {auc_weighted:.4f}")

# ======================== 8. 详细类别指标 ========================
results = []
for i, cls in enumerate(y_classes):
    y_true_bin = (y_test == i).astype(int)
    y_pred_bin = (y_pred == i).astype(int)

    precision = precision_score(y_true_bin, y_pred_bin, zero_division=0)
    recall = recall_score(y_true_bin, y_pred_bin, zero_division=0)
    f1 = f1_score(y_true_bin, y_pred_bin, zero_division=0)
    auc_score = roc_auc_score(y_true_bin, y_pred_proba[:, i])

    results.append({
        '类别': cls,
        '样本数': np.sum(y_test == i),
        '精确率': precision,
        '召回率': recall,
        'F1': f1,
        'AUC': auc_score
    })

print(f"\n【各类别详细指标】")
print(pd.DataFrame(results).to_string(index=False, float_format='%.4f'))

print("\n" + "=" * 60)
print("评估完成！")
print("=" * 60)