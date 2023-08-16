package com.example.sudokusolver2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.sudokusolver2.imageproc.OpenCV
import org.opencv.android.Utils
import org.opencv.core.Mat

class SolveImageFragment(_fileName: String, _sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>) : Fragment() {

    private val fileName = _fileName
    private val sudokuBoard = _sudokuBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_solve_on_image, container, false)

        // Get the bitmap from file saved in saved in storage
        val file = context?.getFileStreamPath(fileName)
        var bitmap = BitmapFactory.decodeFile(file?.path)

        val mat = SudokuUtils.bitmapToMat(bitmap)
        OpenCV.overlaySolutionOnImage(mat, sudokuBoard)

        bitmap = SudokuUtils.matToBitmap(mat)

        view.findViewById<ImageView>(R.id.image_view).setImageBitmap(bitmap)

        return view
    }
}