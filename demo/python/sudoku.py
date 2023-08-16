import cv2
import numpy as np

import time

sudoku = [
    [[0, 0], [0, 0], [4, 1], [3, 1], [0, 0], [0, 0], [0, 0], [5, 1], [0, 0]],
    [[0, 0], [6, 1], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0]],
    [[0, 0], [0, 0], [8, 1], [0, 0], [0, 0], [2, 1], [3, 1], [0, 0], [4, 1]],
    [[5, 1], [0, 0], [0, 0], [0, 0], [4, 1], [8, 1], [0, 0], [2, 1], [0, 0]],
    [[0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [9, 1], [0, 0], [0, 0]],
    [[0, 0], [8, 1], [0, 0], [2, 1], [5, 1], [0, 0], [0, 0], [0, 0], [0, 0]],
    [[0, 0], [9, 1], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0], [0, 0]],
    [[2, 1], [5, 1], [1, 1], [0, 0], [7, 1], [0, 0], [0, 0], [0, 0], [0, 0]],
    [[8, 1], [0, 0], [6, 1], [0, 0], [9, 1], [0, 0], [0, 0], [1, 1], [3, 1]]
]

image = cv2.imread("sudoku.png")
imageText = image.copy()

d = int(image.shape[0] / 9)

def isValidSolution(row, col, n):
    for i in range(0, 9):
        if sudoku[row][i][0] == n or sudoku[i][col][0] == n:
            return False
        
    si = int(row / 3)
    sc = int(col / 3)
    for i in range(si * 3, si * 3 + 3):
        for j in range(sc * 3, sc * 3 + 3):
            if sudoku[i][j][0] == n and i != row and j != col:
                return False
            
    return True


def solve(start = 0):
    for i in range(start, 81):
        r = int(i / 9)
        c = int(i % 9)
        if sudoku[r][c][0] == 0:
            for n in range(1, 10):
                if isValidSolution(r, c, n):
                    sudoku[r][c][0] = n
                    
                    if solve(start + 1):
                        return True
                    
                    sudoku[r][c][0] = 0
            
            return False
        
    return True


def overlaySolution():
    imageText = image.copy()
    for i in range(0, 9):
        for j in range(0, 9):
            if sudoku[i][j][1] == 0:
                cv2.putText(
                    imageText, 
                    str(sudoku[i][j][0]), 
                    (int(j * d + 8), int((i + 1) * d - 5)), 
                    cv2.FONT_HERSHEY_COMPLEX, 
                    1, 
                    (0, 255, 0), 
                    1, 
                    cv2.LINE_AA
                )
    cv2.imshow("sudoku", imageText)
    cv2.waitKey(10000)


solve()
overlaySolution()