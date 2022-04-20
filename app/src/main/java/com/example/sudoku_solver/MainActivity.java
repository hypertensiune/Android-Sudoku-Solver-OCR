package com.example.sudoku_solver;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Mat mRGBA, mRGBAT;
    private Rect rect = null;
    private JavaCameraView javaCameraView;
    private Button camButton;
    private Button flashButton;
    private Button keyboardButton;
    private boolean ok = true;
    private boolean flash = false;
    public static int[][] board;
    private int minNumbersOnBoard = 7;
    private int curNumbersOnBoard = 0;
    private int SOLVE_FROM_IMAGE = 0;
    private int SOLVE_FROM_MANUAL = 1;
    private final TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.camera_surface);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(MainActivity.this);

        camButton = (Button) findViewById(R.id.camBtn);
        flashButton = (Button) findViewById(R.id.flashBtn);
        keyboardButton = (Button) findViewById(R.id.kBtn);

        board = new int[9][9];

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 112);
        }

        keyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                javaCameraView.disableView();

                Intent i = new Intent(MainActivity.this, SolverActivity.class);
                i.putExtra("type", SOLVE_FROM_MANUAL);
                startActivity(i);
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flash) {
                    javaCameraView.turnOnFlashlight();
                    flashButton.setBackgroundResource(R.drawable.flash_on);
                    flash = true;
                } else {
                    javaCameraView.turnOffFlashLight();
                    flashButton.setBackgroundResource(R.drawable.flash_off);
                    flash = false;
                }
            }
        });

        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ok = false;
                Mat mat = processImage(mRGBAT, rect);
                recognizeTextFromImage(mat);
            }
        });
    }

    public Mat processImage(Mat mat, Rect rect) {
        Mat binaryImage = new Mat();
        Mat lines = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        // transform image to binary image
        double thresh = Imgproc.threshold(mat, binaryImage, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        //Imgproc.medianBlur(mat, mat, 5);
        //Imgproc.adaptiveThreshold(mat, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);


        // remove all horizontal lines
        Mat hkernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 1));
        Imgproc.morphologyEx(binaryImage, lines, Imgproc.MORPH_OPEN, hkernel);
        Imgproc.findContours(lines, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(binaryImage, contours, -1, new Scalar(0, 0, 0), 10);

        // remove all vertical lines
        Mat vkernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 40));
        Imgproc.morphologyEx(binaryImage, lines, Imgproc.MORPH_OPEN, vkernel);
        Imgproc.findContours(lines, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(binaryImage, contours, -1, new Scalar(0, 0, 0), 10);

        //crop image
        Mat cropped = new Mat(binaryImage, rect);
        Imgproc.resize(cropped, cropped, mat.size());

        return cropped;
    }

    public Rect getSudokuBoundingRectangle(Mat mRGBA) {

        // remove noise from image, transform to grayscale
        Mat mRGBAG = new Mat();
        Mat canny = new Mat();
        Imgproc.GaussianBlur(mRGBA, mRGBAG, new Size(3, 3), 0, 0);
        Imgproc.cvtColor(mRGBAG, mRGBAG, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(mRGBAG, canny, 200, 255);

        // get all the contours
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(canny, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // if there are contours, get the biggest one
        if (contours.size() > 0) {
            double maxA = 0;
            int imax = 0;
            for (int i = 0; i < contours.size(); i++) {
                Mat contour = contours.get(i);
                if (Imgproc.contourArea(contour) > maxA) {
                    maxA = Imgproc.contourArea(contour);
                    imax = i;
                }
            }

            Imgproc.drawContours(mRGBA, contours, imax, new Scalar(0, 255, 0), 1, Imgproc.LINE_AA);

            // get the bounding box
            MatOfPoint2f c2f = new MatOfPoint2f(contours.get(imax).toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            double perimeter = Imgproc.arcLength(c2f, true);
            Imgproc.approxPolyDP(c2f, approx, 0.01 * perimeter, true);

            MatOfPoint points = new MatOfPoint(approx.toArray());
            Rect rect = Imgproc.boundingRect(points);

            return rect;
        } else {
            return null;
        }
    }

    public void recognizeTextFromImage(Mat mRGBA) {
        Bitmap bitmapImage = Bitmap.createBitmap(mRGBA.cols(), mRGBA.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRGBA, bitmapImage);
        InputImage inImg = InputImage.fromBitmap(bitmapImage, 90);
        Task<Text> result = textRecognizer.process(inImg)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        Log.e("MLKIT", "Task success");
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            for (Text.Line line : block.getLines()) {
                                for (Text.Element element : line.getElements()) {
                                    android.graphics.Rect elRect = element.getBoundingBox();

                                    int approxWidth = mRGBA.width() / 9;
                                    int approxHeight = mRGBA.height() / 9;

                                    int j = elRect.centerX() / approxWidth;
                                    int i = elRect.centerY() / approxHeight;

                                    char c = transformPossibleNumberFromChar(element.getText().charAt(0));
                                    int nr = c - '0';
                                    curNumbersOnBoard++;
                                    board[i][j] = nr > 0 && nr < 10 ? nr : 0;
                                }
                            }
                        }

                        if (check()) {
                            Intent i = new Intent(MainActivity.this, SolverActivity.class);
                            i.putExtra("type", SOLVE_FROM_IMAGE);
                            i.putExtra("board", board);
                            startActivity(i);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "PLEASE RETAKE PHOTO", Toast.LENGTH_SHORT);
                            toast.show();
                            ok = true;
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MLKIT", "Task fail");
                    }
                });
    }

    private boolean check() {
        if (curNumbersOnBoard < minNumbersOnBoard || (rect.width < mRGBA.width() / 3 && rect.height < mRGBA.height() / 3)) {
            return false;
        }
        return true;
    }

    private char transformPossibleNumberFromChar(char c) {
        switch (c) {
            case 'A':
                c = '4';
                break;
            case '|':
                c = '1';
                break;
        }
        return c;
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (ok) {
            mRGBA = inputFrame.rgba();
            mRGBAT = inputFrame.gray();
            rect = getSudokuBoundingRectangle(mRGBA);
        }
        return mRGBA;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }
}