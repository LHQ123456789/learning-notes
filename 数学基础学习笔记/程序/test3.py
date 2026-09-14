import numpy as np
'''
    将矩阵分为U，s， vt三个矩阵，u与vt都是正交矩阵，s为奇异值对角矩阵
    第二个参数主要针对是否返回完整的u和t
'''

A = np.array([[1, 2, 3], [4, 5, 6]])

U, s, Vt = np.linalg.svd(A, full_matrices=False)

print(f"U:\n{U}\n\ns:\n{s}\n\nVt:\n{Vt}")

U, s, Vt = np.linalg.svd(A, full_matrices=True)
print(f"U:\n{U}\n\ns:\n{s}\n\nVt:\n{Vt}")