<img src="imgs/Banner.png" width=100%></img>


## Table of Contents
- [Introduction](#introduction)
- [How it works](#how-it-works)
- [Getting Started](#getting-started)

## Introduction

## How it works

### Sudoku board detection

To identify the sudoku board the input image is processed as follows with [OpenCV](https://github.com/opencv/opencv):
- Using the Canny algorithm to detect all contours in the image.
- The biggest contour corresponds to the whole sudoku puzzle.
- Use the found contour to extract and warp the sudoku grid .

The numbers are detected using [MLKit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/v2).

<br>

<p align="center">
  <img src="demo/demo1.gif"></img>
</p>

<br>

### Solving
For solving the puzzle I'm using a fairly simple, brute-force algorithm that relies on backtracking to generate the valid solution. 
<br>
It goes through the whole 2D array and for each number that needs to be found it tries all possibilities and continues with the following numbers. 
<br> 
<br>
For each cell there are 9 possible numbers which means the time complexity of this algorithm is O(9<sup>N</sup>).

```
private fun solve(index: Int = 0): Boolean {
    for (i in index until 81) {
        val r = i / 9
        val c = i % 9
        if (sudokuBoard[r][c].number == 0) {
            for (n in 1..9) {
                if (isValidSolution(r, c, n)) {
                    sudokuBoard[r][c].number = n
                    if (solve(i + 1)) {
                        return true
                    }
                    sudokuBoard[r][c].number = 0
                }
            }
            return false
        }
    }
    return true
}
```
<br>
<p align="center">
  <img src="demo/demo2.gif"></img>
</p>
<p align="center">
  <span>Solve algorithm demo</span>
</p>

## Getting Started
