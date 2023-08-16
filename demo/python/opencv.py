import cv2
import numpy as np

camera = cv2.VideoCapture(1)

width = 330
height = 330

ih = 336
iw = 448


def getSudokuBoundingRectangle(image):
    blur = cv2.GaussianBlur(image, (3, 3), 1)
    gray = cv2.cvtColor(blur, cv2.COLOR_BGR2GRAY)
    
    canny = cv2.Canny(gray, 50, 150)

    cnts, _ = cv2.findContours(canny, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    if len(cnts) > 0:
        maxArea = 20000
        cntIndex = -1
        i = 0
        for cnt in cnts:
            cntArea = cv2.contourArea(cnt)

            if cntArea > maxArea:
                maxArea = cntArea
                cntIndex = i

            i = i + 1


        if cntIndex > -1:
            cv2.drawContours(image, cnts, cntIndex, (0, 255, 0), 1)

            perimeter = cv2.arcLength(cnts[cntIndex], True)
            approx = cv2.approxPolyDP(cnts[cntIndex], 0.04 * perimeter, True)

            if len(approx) == 4:
                return True, approx, canny
    
    return False, None, canny
    

def orderPoints(h):
    h = h.reshape((4,2))
    hnew = np.zeros((4,2),dtype = np.float32)

    add = h.sum(1)
    hnew[0] = h[np.argmin(add)]
    hnew[3] = h[np.argmax(add)]

    diff = np.diff(h,axis = 1)
    hnew[1] = h[np.argmin(diff)]
    hnew[2] = h[np.argmax(diff)]

    return hnew


def getCroppedSudokuBoard(image, pts):
    src = np.float32(pts)
    dst = np.float32([[0, 0], [width, 0], [0, height], [width, height]])

    matrix = cv2.getPerspectiveTransform(src, dst)
    warped = cv2.warpPerspective(image, matrix, (iw, ih))

    warped[0:, width:] = (0, 0, 0)
    warped[height:, 0:] = (0, 0, 0)

    return warped



while(True):
    ret, frame = camera.read()
    
    frame = cv2.resize(frame, (iw, ih))

    default = frame.copy()

    get, approx, canny = getSudokuBoundingRectangle(frame)

    warped = np.zeros([336, 448, 3], dtype=np.uint8)

    if get:
        sortedPts = orderPoints(approx)

        warped = getCroppedSudokuBoard(frame, sortedPts)

    h1 = np.hstack((default, cv2.cvtColor(canny, cv2.COLOR_GRAY2RGB)))
    h2 = np.hstack((frame, warped))

    imgs = np.vstack((h1, h2))

    cv2.imshow("frames", imgs)
    
    cv2.waitKey(1)
