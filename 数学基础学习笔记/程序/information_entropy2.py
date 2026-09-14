
import numpy as np
import matplotlib.pyplot as plt

# 设置中文字体（解决Mac/Win中文显示问题，若报错可删掉下面两行）
plt.rcParams['font.sans-serif'] = ['Arial Unicode MS', 'SimHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False
# 生成预测概率 p 从 0.01 到 0.99
p_vals = np.linspace(0.01, 0.99, 200)

# 二分类交叉熵：y=1 时为 -log(p)，y=0 时为 -log(1-p)
loss_y1 = -np.log(p_vals)
loss_y0 = -np.log(1 - p_vals)

plt.figure(figsize=(10, 6))
plt.plot(p_vals, loss_y1, label='真实标签 y = 1', linewidth=2.5, color='red')
plt.plot(p_vals, loss_y0, label='真实标签 y = 0', linewidth=2.5, color='blue')
plt.xlabel('预测概率 P(y=1 | x)', fontsize=12)
plt.ylabel('交叉熵损失值', fontsize=12)
plt.title('二分类交叉熵损失函数曲线', fontsize=14)
plt.ylim(0, 5)  # 限制 y 轴范围，方便观察，但要知道右边理论上是无穷大
plt.axvline(x=0.5, color='gray', linestyle='--', alpha=0.6, label='随机猜测边界 (p=0.5)')
plt.grid(True, alpha=0.3)
plt.legend()
plt.show()

# 打印极端示例
print("当 y=1, p=0.01 时，损失 = {:.2f}".format(-np.log(0.01)))
print("当 y=0, p=0.99 时，损失 = {:.2f}".format(-np.log(0.01)))