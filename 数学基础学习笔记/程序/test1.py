import numpy as np


"""
    新建矩阵： np.array()
    求逆矩阵：np.linalg.inv()
    矩阵乘法，点积： np.linalg.solve()
    对奇异矩阵求逆报错
    大型矩阵求逆将花费很大，一般用于求解线性方程组（np.lg.solve）
"""
A = np.array([[1, 2, 3], [4, 5, 6], [7, 8, 10]])

A_inv = np.linalg.inv(A)

result = np.dot(A, A_inv)

print(A_inv)

print(result)