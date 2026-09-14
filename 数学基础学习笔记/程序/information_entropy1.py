import numpy as np
import matplotlib.pyplot as plt

# 设置中文字体（解决Mac/Win中文显示问题，若报错可删掉下面两行）
plt.rcParams['font.sans-serif'] = ['Arial Unicode MS', 'SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

def entropy(p, base=np.e):
    """
    计算离散分布的熵
    处理 p=0 的情况（0*log(0)=0）
    """
    p = np.array(p)
    p = p[p > 0]  # 忽略概率为0的项
    return -np.sum(p * np.log(p)) / np.log(base)

# 定义三个典型分布
distributions = {
    '公平硬币 (均匀)': [0.5, 0.5],
    '偏斜硬币': [0.9, 0.1],
    '确定性事件': [1.0, 0.0],
    '三分类均匀': [1/3, 1/3, 1/3],
    '三分类集中': [0.8, 0.15, 0.05]
}

print("="*40)
print("离散分布熵值计算 (单位: 纳特, 自然对数底)")
print("="*40)
for name, prob in distributions.items():
    H = entropy(prob)
    print(f"{name:12} | 分布 {prob} | 熵 H = {H:.4f} nats")