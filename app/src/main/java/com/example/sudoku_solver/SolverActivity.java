package com.example.sudoku_solver;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Arrays;


public class SolverActivity extends AppCompatActivity {

    private SudokuBoardView sudokuBoardView;
    private int[][] board = new int[9][9];
    private int[][] solvedBoard = new int[9][9];
    private int SOLVE_FROM_IMAGE = 0;
    private int SOLVE_FROM_MANUAL = 1;
    private Button backButton;
    private Button editButton;
    private ConstraintLayout constraintLayout;
    private ConstraintSet constraintSet = new ConstraintSet();
    private boolean isEditing = false;
    private boolean timeout = false;
    private long time = 0;
    private final long TIMEOUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_solve);

        backButton = (Button) findViewById(R.id.backBtn);
        editButton = (Button) findViewById(R.id.editBtn);
        sudokuBoardView = (SudokuBoardView) findViewById(R.id.sudokuboard);
        constraintLayout = (ConstraintLayout) findViewById(R.id.clayout);
        constraintSet.clone(constraintLayout);

        int type = getIntent().getIntExtra("type", -1);
        if (type == SOLVE_FROM_MANUAL) {
            isEditing = true;
            editButton.setBackgroundResource(R.drawable.done_button);
            sudokuBoardView.enableEditing();
            moveBoardViewUp();
        } else if (type == SOLVE_FROM_IMAGE) {
            board = (int[][]) getIntent().getSerializableExtra("board");
            solvedBoard = copyFrom(board);

            time = System.currentTimeMillis();
            timeout = false;
            Solve();

            sudokuBoardView.setBoard(board, solvedBoard);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SolverActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

    }

    public void solveButtonClickHandle(View view) {
        if (!isEditing) {
            moveBoardViewUp();

            sudokuBoardView.enableEditing();
            sudokuBoardView.setBoard(board, board);

            editButton.setBackgroundResource(R.drawable.done_button);
            isEditing = true;
        } else {
            moveBoardViewDown();

            solvedBoard = copyFrom(board);
            time = System.currentTimeMillis();
            timeout = false;
            Solve();

            sudokuBoardView.disableEditing();
            sudokuBoardView.setBoard(board, solvedBoard);
            editButton.setBackgroundResource(R.drawable.edit_button);
            isEditing = false;
        }
    }

    private void moveBoardViewUp() {
        constraintSet.setVerticalBias(R.id.sudokuboard, 0.284f);
        constraintSet.applyTo(constraintLayout);

        for (int i = 1; i <= 10; i++) {
            String name = i == 10 ? "btnDel" : "btn" + i;
            int ID = getResources().getIdentifier(name, "id", getPackageName());
            findViewById(ID).setEnabled(true);
            findViewById(ID).setVisibility(View.VISIBLE);
        }
    }

    private void moveBoardViewDown() {
        constraintSet.setVerticalBias(R.id.sudokuboard, 0.5f);
        constraintSet.applyTo(constraintLayout);

        for (int i = 1; i <= 10; i++) {
            String name = i == 10 ? "btnDel" : "btn" + i;
            int ID = getResources().getIdentifier(name, "id", getPackageName());
            findViewById(ID).setEnabled(false);
            findViewById(ID).setVisibility(View.INVISIBLE);
        }
    }

    public void handleClick(View view) {
        Button b = findViewById(view.getId());
        int r = sudokuBoardView.getSelectedRow();
        int c = sudokuBoardView.getSelected_Column();
        if (r != -1 && c != -1) {
            board[r][c] = b.getText().charAt(0) != 'X' ? Integer.parseInt((String) b.getText()) : 0;
            sudokuBoardView.setBoard(board, board);
        }
    }

    private int[][] copyFrom(int[][] board) {
        int[][] b = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++)
                b[i][j] = board[i][j];
        }
        return b;
    }

    private void Solve() {
        boolean solved = false;
        boolean prechecked = precheck();
        if (prechecked) {
            solved = solve();
        }
        if (timeout || !solved || !prechecked) {
            Toast toast = Toast.makeText(getApplicationContext(), "UNSOLVABLE", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private boolean precheck() {
        for (int i = 0; i < 9; i++) {
            int[] v1 = new int[10];
            int[] v2 = new int[10];
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    if (v1[board[i][j]] == 0) v1[board[i][j]]++;
                    else return false;
                }
                if (board[j][i] != 0) {
                    if (v2[board[j][i]] == 0 && board[j][i] != 0) v2[board[j][i]]++;
                    else return false;
                }
                if (i % 3 == 0 && j % 3 == 0) {
                    int[] v = new int[10];
                    int R = i / 3;
                    int C = j / 3;
                    for (int r = R * 3; r < R * 3 + 3; r++) {
                        for (int c = C * 3; c < C * 3 + 3; c++) {
                            if (board[r][c] != 0) {
                                if (v[board[r][c]] == 0) v[board[r][c]]++;
                                else return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isSafe(int row, int col) {
        if (solvedBoard[row][col] > 0) {
            for (int i = 0; i < 9; i++) {
                if (solvedBoard[i][col] == solvedBoard[row][col] && row != i) {
                    return false;
                }
                if (solvedBoard[row][i] == solvedBoard[row][col] && col != i) {
                    return false;
                }
            }

            int ROW = row / 3;
            int COL = col / 3;
            for (int r = ROW * 3; r < ROW * 3 + 3; r++) {
                for (int c = COL * 3; c < COL * 3 + 3; c++) {
                    if (solvedBoard[r][c] == solvedBoard[row][col] && row != r && col != c) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean solve() {
        if (System.currentTimeMillis() - time > TIMEOUT) {
            timeout = true;
            return false;
        } else {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (solvedBoard[r][c] == 0) {
                        for (int i = 1; i < 10; i++) {
                            solvedBoard[r][c] = i;
                            if (isSafe(r, c) && solve()) {
                                return true;
                            }
                            solvedBoard[r][c] = 0;
                        }
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
