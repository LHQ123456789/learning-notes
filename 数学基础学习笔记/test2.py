import numpy as np
"""
    求解矩阵的秩：np.linalg.matrix_rank()，
    这个函数可以传递两个参数，第二个参数tol默认为None，他的参数值可以用于处理数据本身的噪声与误差
    只有满秩矩阵才可逆
"""
A = np.array([[1, 2, 3], [4, 5, 6]])
rank = np.linalg.matrix_rank(A)
print(f"Rank of A is {rank}")