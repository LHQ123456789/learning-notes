# 整理「sklearn 建模流程模板」（从数据加载到模型评估的 7 步标准流程）

```python
"""
================================================================================
sklearn 机器学习建模标准流程模板
适用场景：表格数据分类/回归任务
版本：v2.0
================================================================================
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.model_selection import (
    train_test_split,
    cross_val_score,
    GridSearchCV,
    StratifiedKFold,
    learning_curve
)
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import (
    StandardScaler,
    MinMaxScaler,
    OneHotEncoder,
    LabelEncoder,
    OrdinalEncoder
)
from sklearn.impute import SimpleImputer
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
    classification_report,
    confusion_matrix,
    ConfusionMatrixDisplay,
    roc_curve,
    roc_auc_score
)
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.svm import SVC
import warnings
warnings.filterwarnings('ignore')

# 设置中文显示
plt.rcParams['font.sans-serif'] = ['Microsoft YaHei', 'SimHei']
plt.rcParams['axes.unicode_minus'] = False

# ====================================================================
# 第1步：数据加载与探索
# ====================================================================
print("="*70)
print("第1步：数据加载与探索")
print("="*70)

def load_data():
    """加载数据（支持多种数据源）"""
    # ===== 方式1：从 CSV 加载 =====
    # df = pd.read_csv('data.csv')
    
    # ===== 方式2：从 Excel 加载 =====
    # df = pd.read_excel('data.xlsx')
    
    # ===== 方式3：从 sklearn 内置数据集加载 =====
    from sklearn.datasets import load_iris
    iris = load_iris()
    df = pd.DataFrame(iris.data, columns=iris.feature_names)
    df['target'] = iris.target
    target_names = iris.target_names
    
    # ===== 方式4：自定义数据 =====
    # np.random.seed(42)
    # df = pd.DataFrame({
    #     'feature1': np.random.randn(100),
    #     'feature2': np.random.randn(100),
    #     'target': np.random.randint(0, 2, 100)
    # })
    # target_names = ['类0', '类1']
    
    return df, target_names

df, target_names = load_data()
print(f"数据形状: {df.shape}")
print(f"列名: {list(df.columns)}")
print(f"\n前5行数据:")
print(df.head())
print(f"\n数据统计描述:")
print(df.describe())
print(f"\n目标列分布:")
print(df['target'].value_counts())

# ====================================================================
# 第2步：数据预处理配置
# ====================================================================
print("\n" + "="*70)
print("第2步：数据预处理配置")
print("="*70)

def configure_preprocessor(df, target_col='target'):
    """
    配置预处理流程
    自动识别数值列和类别列，分别配置预处理方案
    """
    # 分离特征和目标
    X = df.drop(target_col, axis=1)
    y = df[target_col]
    
    # 识别特征类型
    numeric_cols = X.select_dtypes(include=['int64', 'float64']).columns.tolist()
    categorical_cols = X.select_dtypes(include=['object', 'category']).columns.tolist()
    
    print(f"数值特征 ({len(numeric_cols)}): {numeric_cols}")
    print(f"类别特征 ({len(categorical_cols)}): {categorical_cols}")
    
    # 构建预处理器
    transformers = []
    
    if numeric_cols:
        transformers.append((
            'num',
            Pipeline([
                ('imputer', SimpleImputer(strategy='mean')),
                ('scaler', StandardScaler())  # 或 MinMaxScaler()
            ]),
            numeric_cols
        ))
    
    if categorical_cols:
        transformers.append((
            'cat',
            Pipeline([
                ('imputer', SimpleImputer(strategy='most_frequent')),
                ('onehot', OneHotEncoder(handle_unknown='ignore', sparse_output=False))
            ]),
            categorical_cols
        ))
    
    preprocessor = ColumnTransformer(
        transformers=transformers,
        remainder='drop'  # 未指定的列丢弃
    )
    
    return preprocessor, X, y, numeric_cols, categorical_cols

preprocessor, X, y, numeric_cols, categorical_cols = configure_preprocessor(df)

# ====================================================================
# 第3步：数据划分
# ====================================================================
print("\n" + "="*70)
print("第3步：数据划分")
print("="*70)

def split_data(X, y, test_size=0.2, random_state=42):
    """划分训练集和测试集"""
    # 检查是否需要分层（分类任务）
    use_stratify = len(np.unique(y)) < 20  # 类别数少于20使用分层
    
    X_train, X_test, y_train, y_test = train_test_split(
        X, y,
        test_size=test_size,
        random_state=random_state,
        stratify=y if use_stratify else None
    )
    
    print(f"训练集大小: {len(X_train)} ({len(X_train)/len(X):.1%})")
    print(f"测试集大小:  {len(X_test)} ({len(X_test)/len(X):.1%})")
    
    if use_stratify:
        print(f"训练集类别分布: {pd.Series(y_train).value_counts().to_dict()}")
        print(f"测试集类别分布:  {pd.Series(y_test).value_counts().to_dict()}")
    
    return X_train, X_test, y_train, y_test

X_train, X_test, y_train, y_test = split_data(X, y)

# 编码标签（如果标签是字符串）
if y_train.dtype == 'object':
    le = LabelEncoder()
    y_train_encoded = le.fit_transform(y_train)
    y_test_encoded = le.transform(y_test)
    class_names = le.classes_
else:
    y_train_encoded = y_train.values
    y_test_encoded = y_test.values
    class_names = [str(i) for i in np.unique(y_train)]

# ====================================================================
# 第4步：模型定义与训练
# ====================================================================
print("\n" + "="*70)
print("第4步：模型定义与训练")
print("="*70)

def create_model(model_type='random_forest', **kwargs):
    """
    创建模型（支持多种算法）
    可选: 'random_forest', 'logistic', 'svm', 'xgboost', 'lightgbm'
    """
    models = {
        'random_forest': RandomForestClassifier(
            n_estimators=kwargs.get('n_estimators', 100),
            max_depth=kwargs.get('max_depth', 10),
            min_samples_split=kwargs.get('min_samples_split', 2),
            min_samples_leaf=kwargs.get('min_samples_leaf', 1),
            random_state=42
        ),
        'logistic': LogisticRegression(
            C=kwargs.get('C', 1.0),
            max_iter=1000,
            random_state=42
        ),
        'svm': SVC(
            C=kwargs.get('C', 1.0),
            kernel=kwargs.get('kernel', 'rbf'),
            probability=True,
            random_state=42
        )
    }
    return models.get(model_type, models['random_forest'])

# 创建模型
model = create_model('random_forest', n_estimators=50, max_depth=5)

# 构建 Pipeline
pipeline = Pipeline([
    ('preprocessor', preprocessor),
    ('classifier', model)
])

print(f"模型类型: {model.__class__.__name__}")
print(f"Pipeline 结构:")
print(pipeline)

# 训练模型
print("\n开始训练...")
pipeline.fit(X_train, y_train_encoded)
print("训练完成！")

# ====================================================================
# 第5步：交叉验证
# ====================================================================
print("\n" + "="*70)
print("第5步：交叉验证")
print("="*70)

def cross_validate_model(pipeline, X_train, y_train_encoded, cv=5):
    """执行交叉验证"""
    # 选择交叉验证策略
    if len(np.unique(y_train_encoded)) < cv:
        from sklearn.model_selection import LeaveOneOut
        cv_strategy = LeaveOneOut()
        print(f"使用 LeaveOneOut 交叉验证（类别数 < {cv}）")
    else:
        cv_strategy = StratifiedKFold(n_splits=cv, shuffle=True, random_state=42)
        print(f"使用 {cv} 折分层交叉验证")
    
    # 执行交叉验证
    cv_scores = cross_val_score(
        pipeline, X_train, y_train_encoded,
        cv=cv_strategy,
        scoring='accuracy'
    )
    
    print(f"各折准确率: {cv_scores}")
    print(f"平均准确率: {cv_scores.mean():.4f} (±{cv_scores.std():.4f})")
    
    return cv_scores

cv_scores = cross_validate_model(pipeline, X_train, y_train_encoded)

# ====================================================================
# 第6步：模型调优（GridSearchCV）
# ====================================================================
print("\n" + "="*70)
print("第6步：模型调优")
print("="*70)

def tune_model(pipeline, X_train, y_train_encoded, param_grid=None, cv=5):
    """使用 GridSearchCV 调优模型参数"""
    if param_grid is None:
        # 默认参数网格
        param_grid = {
            'classifier__n_estimators': [50, 100],
            'classifier__max_depth': [3, 5, 10],
            'classifier__min_samples_split': [2, 5]
        }
    
    # 选择 CV 策略
    if len(np.unique(y_train_encoded)) < cv:
        from sklearn.model_selection import LeaveOneOut
        cv_strategy = LeaveOneOut()
    else:
        cv_strategy = StratifiedKFold(n_splits=cv, shuffle=True, random_state=42)
    
    grid_search = GridSearchCV(
        pipeline,
        param_grid,
        cv=cv_strategy,
        scoring='accuracy',
        n_jobs=-1,
        verbose=0,
        return_train_score=True
    )
    
    print("开始 GridSearchCV 调参...")
    grid_search.fit(X_train, y_train_encoded)
    print("调参完成！")
    
    print(f"\n最佳参数: {grid_search.best_params_}")
    print(f"最佳交叉验证得分: {grid_search.best_score_:.4f}")
    
    # 显示 Top 5 结果
    results = pd.DataFrame(grid_search.cv_results_)
    print(f"\nTop 5 参数组合:")
    print(results[['params', 'mean_test_score', 'std_test_score']]
          .sort_values('mean_test_score', ascending=False).head(5))
    
    return grid_search

# 执行调优（可选：如果不需要调优，注释掉）
# grid_search = tune_model(pipeline, X_train, y_train_encoded)
# best_pipeline = grid_search.best_estimator_
best_pipeline = pipeline  # 如果不调优，直接使用原始 pipeline

# ====================================================================
# 第7步：模型评估与可视化
# ====================================================================
print("\n" + "="*70)
print("第7步：模型评估与可视化")
print("="*70)

# 7.1 预测
y_pred = best_pipeline.predict(X_test)
y_pred_proba = best_pipeline.predict_proba(X_test)

# 7.2 计算指标
def calculate_metrics(y_true, y_pred, y_pred_proba, class_names):
    """计算所有评估指标"""
    # 基础指标
    accuracy = accuracy_score(y_true, y_pred)
    precision_macro = precision_score(y_true, y_pred, average='macro', zero_division=0)
    recall_macro = recall_score(y_true, y_pred, average='macro', zero_division=0)
    f1_macro = f1_score(y_true, y_pred, average='macro', zero_division=0)
    
    # 加权指标
    precision_weighted = precision_score(y_true, y_pred, average='weighted', zero_division=0)
    recall_weighted = recall_score(y_true, y_pred, average='weighted', zero_division=0)
    f1_weighted = f1_score(y_true, y_pred, average='weighted', zero_division=0)
    
    # 分类报告
    report = classification_report(y_true, y_pred, target_names=class_names, 
                                   zero_division=0, output_dict=True)
    
    # ROC-AUC（多分类）
    n_classes = len(np.unique(y_true))
    if n_classes == 2:
        auc_score = roc_auc_score(y_true, y_pred_proba[:, 1])
    else:
        from sklearn.preprocessing import label_binarize
        y_true_bin = label_binarize(y_true, classes=range(n_classes))
        auc_score = roc_auc_score(y_true_bin, y_pred_proba, average='macro', multi_class='ovr')
    
    metrics = {
        'accuracy': accuracy,
        'precision_macro': precision_macro,
        'recall_macro': recall_macro,
        'f1_macro': f1_macro,
        'precision_weighted': precision_weighted,
        'recall_weighted': recall_weighted,
        'f1_weighted': f1_weighted,
        'auc_macro': auc_score,
        'classification_report': report
    }
    
    return metrics

metrics = calculate_metrics(y_test_encoded, y_pred, y_pred_proba, class_names)

# 7.3 打印结果
print(f"\n【模型评估结果】")
print(f"准确率:                {metrics['accuracy']:.4f}")
print(f"宏平均精确率:          {metrics['precision_macro']:.4f}")
print(f"宏平均召回率:          {metrics['recall_macro']:.4f}")
print(f"宏平均F1:              {metrics['f1_macro']:.4f}")
print(f"加权平均精确率:        {metrics['precision_weighted']:.4f}")
print(f"加权平均召回率:        {metrics['recall_weighted']:.4f}")
print(f"加权平均F1:            {metrics['f1_weighted']:.4f}")
print(f"宏平均AUC:             {metrics['auc_macro']:.4f}")

print(f"\n【分类报告】")
print(classification_report(y_test_encoded, y_pred, target_names=class_names, zero_division=0))

# 7.4 可视化
def plot_all_results(y_true, y_pred, y_pred_proba, class_names, X, feature_names=None):
    """绘制所有评估图表"""
    fig = plt.figure(figsize=(18, 10))
    
    n_classes = len(class_names)
    
    # 1. 混淆矩阵
    ax1 = plt.subplot(2, 3, 1)
    cm = confusion_matrix(y_true, y_pred)
    ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=class_names).plot(
        ax=ax1, cmap='Blues', values_format='d'
    )
    ax1.set_title('混淆矩阵', fontsize=12)
    
    # 2. 归一化混淆矩阵
    ax2 = plt.subplot(2, 3, 2)
    cm_norm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
    sns.heatmap(cm_norm, annot=True, fmt='.2f', cmap='Blues',
                xticklabels=class_names, yticklabels=class_names, ax=ax2)
    ax2.set_title('归一化混淆矩阵', fontsize=12)
    ax2.set_xlabel('预测类别')
    ax2.set_ylabel('真实类别')
    
    # 3. 特征重要性（仅当模型是树模型且有特征名）
    ax3 = plt.subplot(2, 3, 3)
    if hasattr(best_pipeline.named_steps['classifier'], 'feature_importances_'):
        # 获取特征名
        if feature_names is None:
            try:
                # 获取预处理后的特征名
                feature_names = preprocessor.get_feature_names_out()
            except:
                feature_names = [f'特征_{i}' for i in range(len(feature_importances))]
        
        feature_importances = best_pipeline.named_steps['classifier'].feature_importances_
        imp_df = pd.DataFrame({'特征': feature_names, '重要性': feature_importances})
        imp_df = imp_df.sort_values('重要性', ascending=True).tail(10)
        ax3.barh(imp_df['特征'], imp_df['重要性'], color='steelblue')
        ax3.set_title('Top 10 特征重要性', fontsize=12)
        ax3.set_xlabel('重要性分数')
    
    # 4. ROC 曲线
    ax4 = plt.subplot(2, 3, 4)
    if n_classes == 2:
        fpr, tpr, _ = roc_curve(y_true, y_pred_proba[:, 1])
        ax4.plot(fpr, tpr, linewidth=2, label=f'AUC = {roc_auc_score(y_true, y_pred_proba[:, 1]):.3f}')
    else:
        from sklearn.preprocessing import label_binarize
        y_true_bin = label_binarize(y_true, classes=range(n_classes))
        for i, cls in enumerate(class_names):
            fpr, tpr, _ = roc_curve(y_true_bin[:, i], y_pred_proba[:, i])
            ax4.plot(fpr, tpr, linewidth=2, label=f'{cls} (AUC={roc_auc_score(y_true_bin[:, i], y_pred_proba[:, i]):.3f})')
    ax4.plot([0, 1], [0, 1], 'k--', linewidth=1, label='随机猜测')
    ax4.set_xlabel('假正率 (FPR)')
    ax4.set_ylabel('真正率 (TPR)')
    ax4.set_title('ROC 曲线', fontsize=12)
    ax4.legend(loc='lower right')
    ax4.grid(True, alpha=0.3)
    
    # 5. 分类报告表格
    ax5 = plt.subplot(2, 3, 5)
    ax5.axis('off')
    report = classification_report(y_true, y_pred, target_names=class_names, 
                                   zero_division=0, output_dict=True)
    report_df = pd.DataFrame(report).transpose()
    # 保留需要的行
    report_df = report_df[['precision', 'recall', 'f1-score', 'support']]
    report_df = report_df.round(3)
    ax5.table(cellText=report_df.values,
              rowLabels=report_df.index,
              colLabels=report_df.columns,
              loc='center',
              cellLoc='center')
    ax5.set_title('分类报告', fontsize=12)
    
    # 6. 模型概览
    ax6 = plt.subplot(2, 3, 6)
    ax6.axis('off')
    metrics_text = f"""
    模型评估概览
    ────────────────────
    数据集大小:      {len(X)} 样本
    训练集:          {len(X_train)} 样本
    测试集:          {len(X_test)} 样本
    特征数:          {X.shape[1]}
    类别数:          {n_classes}
    
    准确率:          {metrics['accuracy']:.3f}
    宏平均 F1:       {metrics['f1_macro']:.3f}
    宏平均 AUC:      {metrics['auc_macro']:.3f}
    交叉验证均值:    {cv_scores.mean():.3f}
    """
    ax6.text(0.1, 0.5, metrics_text, fontsize=12, va='center', fontfamily='monospace')
    ax6.set_title('模型性能概览', fontsize=12)
    
    plt.tight_layout()
    plt.show()

# 执行可视化
plot_all_results(y_test_encoded, y_pred, y_pred_proba, class_names, X)

# ====================================================================
# 总结输出
# ====================================================================
print("\n" + "="*70)
print("建模流程完成！")
print("="*70)
print("\n【流程总结】")
print(f"1. 数据加载: {df.shape[0]} 行, {df.shape[1]} 列")
print(f"2. 预处理: 数值特征 {len(numeric_cols)} 个, 类别特征 {len(categorical_cols)} 个")
print(f"3. 数据划分: 训练集 {len(X_train)} 个, 测试集 {len(X_test)} 个")
print(f"4. 模型: {model.__class__.__name__}")
print(f"5. 交叉验证: {cv_scores.mean():.3f} (±{cv_scores.std():.3f})")
print(f"6. 测试集准确率: {metrics['accuracy']:.4f}")
print(f"7. 宏平均 F1: {metrics['f1_macro']:.4f}")
print("="*70)

# ====================================================================
# 模型保存（可选）
# ====================================================================
def save_model(pipeline, filename='model.pkl'):
    """保存模型"""
    import joblib
    joblib.dump(pipeline, filename)
    print(f"\n模型已保存到: {filename}")

def load_model(filename='model.pkl'):
    """加载模型"""
    import joblib
    return joblib.load(filename)

# save_model(best_pipeline)
```