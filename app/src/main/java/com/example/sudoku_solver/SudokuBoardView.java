package com.example.sudoku_solver;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.datatransport.runtime.EncodedPayload;

import java.security.cert.PolicyNode;

public class SudokuBoardView extends View {

    private final Paint boardPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Rect letterPaintRect = new Rect();
    private final int thickLineColor;
    private final int thinLineColor;
    private int cellsize;
    private int[][] board = new int[9][9];
    private int[][] solvedBoard = new int[9][9];
    private int SELECTED_ROW = -1;
    private int SELECTED_COLUMN = -1;
    private boolean editing = false;

    public SudokuBoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        textPaint.setColor(Color.WHITE);
        TypedArray attrs = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.SudokuBoardView, 0, 0);
        try {
            thickLineColor = attrs.getInteger(R.styleable.SudokuBoardView_thickLineColor, 0);
            thinLineColor = attrs.getInteger(R.styleable.SudokuBoardView_thinLineColor, 0);
        } finally {
            attrs.recycle();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (editing) {
            float x = event.getX();
            float y = event.getY();

            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                SELECTED_ROW = ((int) Math.floor(y / cellsize));
                SELECTED_COLUMN = ((int) Math.floor(x / cellsize));
                invalidate();
                return true;
            } else {
                return false;
            }
        } else {
            SELECTED_ROW = SELECTED_COLUMN = -1;
            return false;
        }
    }

    private void drawBoard(Canvas canvas) {
        boardPaint.setStyle(Paint.Style.FILL);

        boardPaint.setStrokeWidth(3);
        boardPaint.setColor(thinLineColor);
        for (int i = 1; i < 9; i++) {
            if (i % 3 != 0) {
                canvas.drawLine(cellsize * i, 0, cellsize * i, getWidth(), boardPaint);
                canvas.drawLine(0, cellsize * i, getHeight(), cellsize * i, boardPaint);
            }
        }

        boardPaint.setStrokeWidth(10);
        boardPaint.setColor(thickLineColor);
        for (int i = 1; i < 9; i++) {
            if (i % 3 == 0) {
                canvas.drawLine(cellsize * i, 0, cellsize * i, getWidth(), boardPaint);
                canvas.drawLine(0, cellsize * i, getHeight(), cellsize * i, boardPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int width, int height) {
        super.onMeasure(width, height);

        int dimension = Math.min(this.getMeasuredWidth(), this.getMeasuredHeight());
        cellsize = dimension / 9;

        setMeasuredDimension(dimension, dimension);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (SELECTED_COLUMN != -1 && SELECTED_ROW != -1) {
            canvas.drawCircle(SELECTED_COLUMN * cellsize + cellsize / 2, SELECTED_ROW * cellsize + cellsize / 2, 50, boardPaint);
        }
        drawNumbers(canvas);
        drawBoard(canvas);
    }

    public void enableEditing() {
        editing = true;
    }

    public void disableEditing() {
        editing = false;
    }

    public int getSelectedRow() {
        return SELECTED_ROW;
    }

    public int getSelected_Column() {
        return SELECTED_COLUMN;
    }

    private void drawNumbers(Canvas canvas) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (solvedBoard[i][j] != 0) {
                    String text = Integer.toString(solvedBoard[i][j]);
                    boardPaint.setColor(thinLineColor);

                    if (solvedBoard[i][j] == board[i][j] && (j != SELECTED_COLUMN || i != SELECTED_ROW)) {
                        canvas.drawCircle(j * cellsize + cellsize / 2, i * cellsize + cellsize / 2, 50, boardPaint);
                    }

                    textPaint.getTextBounds(text, 0, text.length(), letterPaintRect);
                    textPaint.setTextSize(60);
                    float w = textPaint.measureText(text);
                    float h = letterPaintRect.height();

                    canvas.drawText(text, (j * cellsize) + ((cellsize - w) / 2), (i * cellsize + cellsize) - ((cellsize - h) / 2), textPaint);
                }
            }
        }
    }

    public void setBoard(int[][] BOARD, int[][] SOLVED) {
        SELECTED_ROW = SELECTED_COLUMN = -1;
        board = BOARD;
        solvedBoard = SOLVED;
        invalidate();
    }
}
