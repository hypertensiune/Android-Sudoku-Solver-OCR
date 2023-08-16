package com.example.sudokusolver2

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

class Solver(_context: Context) {

    private lateinit var sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>

    private val context = _context.applicationContext

    companion object {

        private lateinit var instance: Solver

        fun init(_context: Context) {
            instance = Solver(_context)
        }

        fun getInstance() = instance
    }

    fun solveSudoku(_sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>): Boolean {
        sudokuBoard = _sudokuBoard


        // If solve() returns false then there is no solution for the puzzle
        if(!solve()) {
            Toast.makeText(
                context,
                "Unsolvable!",
                Toast.LENGTH_SHORT
            ).show()
            SudokuUtils.set0(sudokuBoard)
            return false
        }
        return true
    }

    /**
     * Check if we can place a number at the given row and column.
     */
    private fun isValidSolution(row: Int, col: Int, n: Int): Boolean {
        // Check on row and column
        for (i in 0 until 9) {
            if (sudokuBoard[row][i].number == n || sudokuBoard[i][col].number == n)
                return false
        }

        // Check in the 3x3 subgrid
        // Get the top left row and column indexes of the subgrid and iterate the subgrid
        val subgridTLi = row / 3
        val subgridTlj = col / 3
        for (i in subgridTLi * 3 until subgridTLi * 3 + 3) {
            for (j in subgridTlj * 3 until subgridTlj * 3 + 3) {
                if (sudokuBoard[i][j].number == n && i != row && j != col)
                    return false
            }
        }

        return true
    }

    /**
     * Function to solve the sudoku
     *
     * Find all numbers that must be completed and for each one try every number from 1 to 9.
     * If a solution is valid place the number and try solving further, otherwise try another solution
     */
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
}