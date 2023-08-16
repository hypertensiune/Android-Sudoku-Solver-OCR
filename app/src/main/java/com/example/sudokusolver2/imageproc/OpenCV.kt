package com.example.sudokusolver2.imageproc

import android.provider.ContactsContract.CommonDataKinds.Im
import com.example.sudokusolver2.SudokuUtils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object OpenCV {

    /**
     * Detect the contour of a sudoku board from the input image and return it.
     */
    fun getSudokuBoundingRectangle(image: Mat, drawContour: Boolean): MatOfPoint2f? {

        // Blur the image to remove noise and transform it to grayscale
        val image2 = Mat()
        Imgproc.GaussianBlur(image, image, Size(3.0, 3.0), 0.0, 0.0)
        Imgproc.cvtColor(image, image2, Imgproc.COLOR_RGB2GRAY)

        // Transform the image using the Canny algorithm and find the contours based on the new processed image
        val canny = Mat()
        Imgproc.Canny(image2, canny, 200.0, 255.0)

        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(canny, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        //If there are any contours, find the biggest one
        if (contours.size > 0) {
            var maxArea = 0.0
            var cntIndex = 0
            for (cnt in contours) {
                val cntArea = Imgproc.contourArea(cnt)

                if (cntArea > maxArea) {
                    maxArea = cntArea
                    cntIndex = contours.indexOf(cnt)
                }
            }

            // Approximate the biggest contour to a polygon
            val curve = MatOfPoint2f(*contours[cntIndex].toArray())
            val approx = MatOfPoint2f()
            val perimeter = Imgproc.arcLength(curve, true)
            Imgproc.approxPolyDP(curve, approx, 0.04 * perimeter, true)

            // If the polygon is a rectangle / has 4 points return it (and draw)
            if (approx.toList().size == 4) {

                if (drawContour) {
                    Imgproc.drawContours(image, contours, cntIndex, Scalar(0.0, 255.0, 0.0), 2)
                }

                return approx
            }
        }
        return null
    }


    /**
     * Crop the sudoku board out of the image and get it as a top down perspective.
     *
     * The 4 contour points must be ordered clockwise starting from the top left corner
     * for the perspective warp.
     *
     * NOTE: The JavaCameraView is by default in landscape mode, to use it in portrait the canvas is rotated by 90deg
     * in CameraBridgeViewBase.deliverAndDrawFrame therefor we can shift the destination points to have the
     * perspective warped image in the right orientation for further use
     */
    fun getCroppedSudokuBoard(image: Mat, contourPts: Array<Point>): Mat {
        val src = Mat(4, 1, CvType.CV_32FC2)
        val dst = Mat(4, 1, CvType.CV_32FC2)

        val imageWidth = image.size().width
        val imageHeight = image.size().height

        src.put(
            0, 0,
            contourPts[0].x, contourPts[0].y,
            contourPts[1].x, contourPts[1].y,
            contourPts[2].x, contourPts[2].y,
            contourPts[3].x, contourPts[3].y,
        )

        dst.put(
            0, 0,
            imageWidth, 0.0,
            imageWidth, imageHeight,
            0.0, 0.0,
            0.0, imageHeight
        )

        // Get the perspective transform matrix needed and warp the image
        val matrix = Imgproc.getPerspectiveTransform(src, dst)

        val output = Mat()
        Imgproc.warpPerspective(image, output, matrix, image.size())

        return output
    }


    /**
     * Order the 4 corner points of a retrieved contour.
     *
     * OpenCV's contour detection algorithm may get the points in a random order, so to warp the perspective
     * these points need to be ordered clockwise starting from the top left point.
     *
     * https://stackoverflow.com/questions/40688491/opencv-getperspectivetransform-and-warpperspective-java
     */
    fun orderContourPoints(approx: MatOfPoint2f): Array<Point> {
        val moment = Imgproc.moments(approx)
        val x = (moment._m10 / moment._m00).toInt()
        val y = (moment._m01 / moment._m00).toInt()

        val sortedPoints =
            arrayOf(Point(0.0, 0.0), Point(0.0, 0.0), Point(0.0, 0.0), Point(0.0, 0.0))

        var data: DoubleArray
        var count = 0
        for (i in 0 until approx.rows()) {
            data = approx.get(i, 0)
            val datax = data[0]
            val datay = data[1]
            if (datax < x && datay < y) {
                sortedPoints[0] = Point(datax, datay)
                count++
            } else if (datax > x && datay < y) {
                sortedPoints[1] = Point(datax, datay)
                count++
            } else if (datax < x && datay > y) {
                sortedPoints[2] = Point(datax, datay)
                count++
            } else if (datax > x && datay > y) {
                sortedPoints[3] = Point(datax, datay)
                count++
            }
        }

        return sortedPoints
    }

    fun overlaySolutionOnImage(image: Mat, solution: Array<Array<SudokuUtils.SudokuCell>>) {

        val d = image.width() / 9 * 1.0

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (solution[i][j].type == SudokuUtils.SUDOKU_CELL_TYPE_SOLUTION) {
                    Imgproc.putText(
                        image,
                        solution[i][j].number.toString(),
                        Point(j * d + d / 2 - 30, (i + 1) * d - d / 2 + 40),
                        Imgproc.FONT_HERSHEY_COMPLEX,
                        3.5,
                        Scalar(255.0, 0.0, 0.0),
                        5,
                        Imgproc.LINE_AA
                    )
                }
            }
        }
    }
}