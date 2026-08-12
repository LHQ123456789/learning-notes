import numpy as np
import matplotlib.pyplot as plt


# 解决中文显示问题
plt.rcParams['font.sans-serif'] = ['SimHei']  # 使用黑体
plt.rcParams['axes.unicode_minus'] = False    # 解决负号显示问题


def gradient_descent_complete(start_x, start_y, learning_rate=0.1,
                              n_iterations=50, tol=1e-6, verbose=True):
    """
    完整的梯度下降实现（含所有组件）
    """
    # 初始化
    x, y = start_x, start_y
    history = [(x, y)]
    losses = [x ** 2 + y ** 2]

    for i in range(n_iterations):
        # 计算梯度
        grad_x, grad_y = 2 * x, 2 * y

        # 更新参数
        x -= learning_rate * grad_x
        y -= learning_rate * grad_y

        # 记录
        history.append((x, y))
        loss = x ** 2 + y ** 2
        losses.append(loss)

        # 打印
        if verbose and i % 5 == 0:
            print(f"Iter {i:3d}: ({x:.6f}, {y:.6f}), loss={loss:.8f}")

        # 收敛判断
        if loss < tol:
            if verbose:
                print(f"✓ 在第 {i + 1} 次迭代收敛")
            break

    return np.array(history), np.array(losses)


# 运行
history, losses = gradient_descent_complete(3.0, 4.0, learning_rate=0.1)

# 可视化
fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

# 轨迹
ax1.plot(history[:, 0], history[:, 1], 'r.-', linewidth=2)
ax1.plot(history[0, 0], history[0, 1], 'go', markersize=10, label='起点')
ax1.plot(history[-1, 0], history[-1, 1], 'ro', markersize=10, label='终点')
ax1.set_xlabel('x')
ax1.set_ylabel('y')
ax1.set_title('下降轨迹')
ax1.grid(True)
ax1.legend()
ax1.axis('equal')

# 损失曲线
ax2.plot(losses, 'b-', linewidth=2)
ax2.set_xlabel('迭代次数')
ax2.set_ylabel('损失值')
ax2.set_title('损失下降')
ax2.grid(True)

plt.tight_layout()
plt.show()

print(f"\n最终结果: ({history[-1][0]:.8f}, {history[-1][1]:.8f})")
print(f"理论最优: (0, 0)")
print(f"最终损失: {losses[-1]:.10f}")