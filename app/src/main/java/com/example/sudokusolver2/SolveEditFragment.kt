package com.example.sudokusolver2

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import kotlin.math.floor
import kotlin.math.min

class SolveEditFragment(_sudokuBoard: Array<Array<SudokuUtils.SudokuCell>>) : Fragment() {

    private val sudokuBoard = _sudokuBoard

    private var EDIT = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_solve_edit, container, false)

        val sbv = view.findViewById<SudokuBoardView>(R.id.sudokuBoard)
        sbv.setSudokuBoard(sudokuBoard)

        (activity as SolverActivity).bindListenerToView<Button>(R.id.editBtn) {
            EDIT = !EDIT
            if(EDIT) {
                sbv.isEditingMode(true)

                SudokuUtils.set0(sudokuBoard)
                sbv.setSudokuBoard(sudokuBoard)

                it.setBackgroundResource(R.drawable.baseline_done_24)
            } else {
                sbv.isEditingMode(false)

                Solver.getInstance().solveSudoku(sudokuBoard)
                sbv.setSudokuBoard(sudokuBoard)

                it.setBackgroundResource(R.drawable.baseline_edit_24)
            }
        }

        // Add listeners to all 1..9 buttons
        for(i in 1..9) {
            val id = resources.getIdentifier("b${i}", "id", (activity as SolverActivity).packageName)
            view.findViewById<Button>(id).setOnClickListener {
                if(sbv.SELECTED_ROW != -1 && sbv.SELECTED_COLUMN != -1) {
                    sudokuBoard[sbv.SELECTED_ROW][sbv.SELECTED_COLUMN].let {
                        it.number = i
                        it.type = SudokuUtils.SUDOKU_CELL_TYPE_GIVEN
                    }
                    sbv.setSudokuBoard(sudokuBoard)

                    sbv.SELECTED_COLUMN = -1
                    sbv.SELECTED_ROW = -1
                }
            }
        }

        view.findViewById<Button>(R.id.bX).setOnClickListener {
            if(sbv.SELECTED_ROW != -1 && sbv.SELECTED_COLUMN != -1) {
                sudokuBoard[sbv.SELECTED_ROW][sbv.SELECTED_COLUMN].let {
                    it.number = 0
                    it.type = SudokuUtils.SUDOKU_CELL_TYPE_SOLUTION
                }
                sbv.setSudokuBoard(sudokuBoard)

                sbv.SELECTED_COLUMN = -1
                sbv.SELECTED_ROW = -1
            }
        }

        return view
    }

    class SudokuBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

        private val boardPaint = Paint()
        private val textPaint = Paint()

        private val characterPaintRect = Rect()

        private var thickColor: Int = 0
        private var thinColor: Int = 0

        private var cellsize: Float = 0.0F

        var SELECTED_ROW = -1
        var SELECTED_COLUMN = -1

        private var editing = true

        private var sudokuBoard = SudokuUtils.emptySudoku2DArray()

        init {
            textPaint.color = Color.WHITE
            val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.SudokuBoardView, 0, 0)
            try {
                thinColor = attrs.getInteger(R.styleable.SudokuBoardView_thinColor, 0)
                thickColor = attrs.getInteger(R.styleable.SudokuBoardView_thickColor, 0)
            } finally {
                attrs.recycle()
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (SELECTED_COLUMN != -1 && SELECTED_ROW != -1) {
                boardPaint.color = thickColor
                canvas.drawCircle(SELECTED_COLUMN * cellsize + cellsize / 2, SELECTED_ROW * cellsize + cellsize / 2, 50F, boardPaint);
            }

            drawBoard(canvas)
            drawNumbers(canvas)
        }

        /**
         * We want the sudoku view to be a square so the sides should
         * be the minimum of the view's width and height
         */
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            val dim = min(measuredWidth, measuredHeight)
            cellsize = dim / 9.0F

            setMeasuredDimension(dim, dim)
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            if(editing) {
                val x = event!!.x
                val y = event!!.y

                if(event.action == MotionEvent.ACTION_DOWN) {
                    SELECTED_ROW = floor(y / cellsize).toInt()
                    SELECTED_COLUMN = floor(x / cellsize).toInt()

                    invalidate()

                    return true
                }
            } else {
                SELECTED_ROW = -1
                SELECTED_COLUMN = -1
            }
            return false
        }

        private fun drawBoard(canvas: Canvas) {
            boardPaint.style = Paint.Style.FILL

            // Lines to separate all cells
            boardPaint.strokeWidth = 3f
            boardPaint.color = thinColor
            for (i in 1..8) {
                if (i % 3 != 0) {
                    canvas.drawLine(cellsize * i, 0.0F, cellsize * i, width.toFloat(), boardPaint)
                    canvas.drawLine(0.0F, cellsize * i, width.toFloat(), cellsize * i, boardPaint)
                }
            }

            // Lines to separate the 9 sections
            boardPaint.strokeWidth = 10f
            boardPaint.color = thickColor
            for (i in 1..8) {
                if (i % 3 == 0) {
                    canvas.drawLine(cellsize * i, 0F, cellsize * i, width.toFloat(), boardPaint)
                    canvas.drawLine(0F, cellsize * i, height.toFloat(), cellsize * i, boardPaint)
                }
            }
        }

        private fun drawNumbers(canvas: Canvas) {
            boardPaint.color = thinColor
            textPaint.textSize = 60.0F

            for(i in 0..8) {
                for(j in 0..8) {
                    if(sudokuBoard[i][j].number != 0) {
                        val text = sudokuBoard[i][j].number.toString()

                        // Highlight the numbers detected from the sudoku
                        if(sudokuBoard[i][j].type == SudokuUtils.SUDOKU_CELL_TYPE_GIVEN && (j != SELECTED_COLUMN || i != SELECTED_ROW)) {
                            canvas.drawCircle(j * cellsize + cellsize / 2, i * cellsize + cellsize / 2, 50F, boardPaint)
                        }

                        textPaint.getTextBounds(text, 0, text.length, characterPaintRect)

                        val w = textPaint.measureText(text)
                        val h = characterPaintRect.height()

                        canvas.drawText(text, (j * cellsize) + ((cellsize - w) / 2), (i * cellsize + cellsize) - ((cellsize - h) / 2), textPaint)
                    }
                }
            }
        }

        fun setSudokuBoard(board: Array<Array<SudokuUtils.SudokuCell>>) {
            sudokuBoard = board
            invalidate()
        }

        fun isEditingMode(b: Boolean) {
            editing = b
        }
    }
}