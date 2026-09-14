import pandas as pd
import numpy as np
from sklearn.preprocessing import StandardScaler

# =========================== 1. 读取 CSV ===========================
print("正在加载数据...")
train = pd.read_csv('train.csv')
test = pd.read_csv('test.csv')

# 保存测试集的 PassengerId（用于最终提交），并暂存训练集的 Survived
test_ids = test['PassengerId'].copy()
train_labels = train['Survived'].copy()

print(f"训练集形状: {train.shape}, 测试集形状: {test.shape}")

# =========================== 2. 查看缺失值 ===========================
print("\n【训练集缺失值统计】")
print(train.isnull().sum())
print("\n【测试集缺失值统计】")
print(test.isnull().sum())

# =========================== 3. 删除列 & 填充缺失值 ===========================
# 3.1 删除无法利用的列：Name, Ticket, Cabin（缺失太多）, PassengerId(训练集)
drop_cols = ['Name', 'Ticket', 'Cabin']
train.drop(columns=drop_cols, axis=1, inplace=True)
test.drop(columns=drop_cols, axis=1, inplace=True)

# 训练集丢掉 PassengerId，测试集保留（但先不丢，用于索引对齐）
train.drop(columns=['PassengerId'], axis=1, inplace=True)

# 3.2 填充缺失值（均使用训练集的统计量，防止数据泄漏）
# 年龄（Age）：用训练集中位数填充
age_median = train['Age'].median()
train['Age'].fillna(age_median, inplace=True)
test['Age'].fillna(age_median, inplace=True)

# 登船港口（Embarked）：用训练集众数填充
embark_mode = train['Embarked'].mode()[0]
train['Embarked'].fillna(embark_mode, inplace=True)
test['Embarked'].fillna(embark_mode, inplace=True)

# 票价（Fare）：测试集可能有1个缺失，用训练集中位数填充
fare_median = train['Fare'].median()
test['Fare'].fillna(fare_median, inplace=True)

# 再次检查是否还有缺失值（保险起见）
print("\n【处理后缺失值检查】")
print(f"训练集剩余缺失: {train.isnull().sum().sum()}")
print(f"测试集剩余缺失: {test.isnull().sum().sum()}")

# =========================== 4. 特征编码 (One-Hot / Label) ===========================
# 4.1 Label Encoding: Sex (男=0, 女=1)
train['Sex'] = train['Sex'].map({'male': 0, 'female': 1})
test['Sex'] = test['Sex'].map({'male': 0, 'female': 1})

# 4.2 One-Hot Encoding: Embarked (注意 drop_first=True 避免虚拟变量陷阱)
train = pd.get_dummies(train, columns=['Embarked'], prefix='Emb', drop_first=True)
test = pd.get_dummies(test, columns=['Embarked'], prefix='Emb', drop_first=True)

# 【关键步骤】确保训练集和测试集的特征列完全对齐
# 测试集可能缺少某些 one-hot 列（例如没出现某个港口），需要补0
# 同时训练集独有的列（如只有 Emb_Q）也要在测试集出现
for col in train.columns:
    if col not in test.columns:
        test[col] = 0  # 测试集缺少该列，补0

# 反过来，测试集独有的列（几乎不会发生，但为了防止万一）在训练集中补0
for col in test.columns:
    if col not in train.columns:
        train[col] = 0

# 确保两边的列顺序一致（按列名排序保证对齐）
train = train.reindex(sorted(train.columns), axis=1)
test = test.reindex(sorted(test.columns), axis=1)

# 此时训练集包含 Survived，测试集没有，我们先把 Survived 拿出来单独存
# 注意：训练集之前丢掉了 PassengerId，现在列顺序变了，但 Survived 依然在第一列（因为 reindex 排序了）
# 稳妥起见，我们用之前备份的 train_labels 重新赋值
train['Survived'] = train_labels.values

print(f"\n编码后训练集特征列: {train.columns.tolist()}")
print(f"编码后测试集特征列: {test.columns.tolist()}")

# =========================== 5. 特征缩放 (Standardization) ===========================
# 分离特征和目标（缩放只针对特征）
X_train = train.drop(columns=['Survived'])
y_train = train['Survived']
X_test = test  # 测试集没有目标值

# 标准化连续变量：Age 和 Fare
scaler = StandardScaler()
X_train[['Age', 'Fare']] = scaler.fit_transform(X_train[['Age', 'Fare']])
X_test[['Age', 'Fare']] = scaler.transform(X_test[['Age', 'Fare']])

# 重新组合训练集（用于保存完整数据）
train_processed = pd.concat([X_train, y_train], axis=1)
test_processed = X_test  # 测试集已经是特征矩阵

# =========================== 6. 保存处理后的数据 ===========================
train_processed.to_csv('processed_train.csv', index=False)
test_processed.to_csv('processed_test.csv', index=False)

print("\n✅ 数据处理完成！")
print(f"训练集已保存为 'processed_train.csv'，形状: {train_processed.shape}")
print(f"测试集已保存为 'processed_test.csv'，形状: {test_processed.shape}")
print(f"最终特征数量: {X_train.shape[1]}")

# 显示前5行预览
print("\n【处理后的训练集预览】")
print(train_processed.head())