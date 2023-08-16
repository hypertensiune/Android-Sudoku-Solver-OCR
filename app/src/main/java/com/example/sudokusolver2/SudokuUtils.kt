package com.example.sudokusolver2

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.Serializable

object SudokuUtils {

    const val SUDOKU_CELL_TYPE_GIVEN = 0
    const val SUDOKU_CELL_TYPE_SOLUTION = 1

    data class SudokuCell(var number: Int, var type: Int) : Serializable

    fun emptySudoku2DArray(): Array<Array<SudokuCell>> {
        return Array(9) {
            Array(9) {
                SudokuCell(
                    0,
                    SUDOKU_CELL_TYPE_SOLUTION
                )
            }
        }
    }

    fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        return mat
    }

    fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        return bitmap
    }

    /**
     * Remove the solution from the sudoku array
     */
    fun set0(arr: Array<Array<SudokuCell>>) {
        for(i in 0..8) {
            for(j in 0..8) {
                if(arr[i][j].type == SUDOKU_CELL_TYPE_SOLUTION)
                    arr[i][j].number = 0
            }
        }
    }

    fun printSudokuBoard(arr: Array<Array<SudokuCell>>) {
        Log.d("SudokuSolver2", "Solved")
        for (i in 0 until 9) {
            val array = CharArray(9)
            for (j in 0 until 9) {
                array[j] = arr[i][j].number.toString()[0]
            }
            Log.d("SudokuSolver2", array.joinToString(" "))
        }
    }
}


