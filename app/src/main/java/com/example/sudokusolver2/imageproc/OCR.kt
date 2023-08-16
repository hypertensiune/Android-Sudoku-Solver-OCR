package com.example.sudokusolver2.imageproc

import android.util.Log
import com.example.sudokusolver2.SudokuUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.lang.Exception


object OCR {

    private const val TAG = "SudokuSolver2"

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Parse the resulted text from text recognition into a 2d int array
     * @param cWidth Width of a sudoku cell
     * @param cHeight Height of a sudoku cell
     */
    private fun getArray(
        result: com.google.mlkit.vision.text.Text,
        cWidth: Int,
        cHeight: Int
    ): Array<Array<SudokuUtils.SudokuCell>> {

        val sudokuBoard = SudokuUtils.emptySudoku2DArray()

        // Loop trough all blocks, lines and text elements and calculate their corresponding position indexes
        // in the 2d array using the element's bounding box center coordinates on the original image
        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {

                    val rect = element.boundingBox
                    val j = rect!!.centerX() / cWidth
                    val i = rect!!.centerY() / cHeight

                    sudokuBoard[i][j].let {
                        it.number = element.text.toInt()
                        it.type = SudokuUtils.SUDOKU_CELL_TYPE_GIVEN
                    }
                }
            }
        }

        return sudokuBoard
    }

    /**
     * Process the image with MLKit Text Recognition and return the parsed sudoku board as int 2d array
     */
    suspend fun getSudokuFromImage(image: InputImage): Array<Array<SudokuUtils.SudokuCell>> {
        return try {
            // textRecognizer.process is asynchronous so wait for it
            val result = textRecognizer.process(image).await()
            getArray(result, image.width / 9, image.height / 9)

        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            SudokuUtils.emptySudoku2DArray()
        }
    }
}