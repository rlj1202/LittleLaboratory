package redlaboratory.littlelaboratory;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class OpenCVTestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            Log.i("LittleLaboratory", "OpenCV initialize failed");
        } else {
            Log.i("LittleLaboratory", "OpenCV initialize success");
        }
    }

    private CameraBridgeViewBase mOpenCvCameraView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("LittleLaboratory", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    Log.i("LittleLaboratory", "OpenCV default: " + status);
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_opencv_sample);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    private Mat grey;
    private Mat rgba;

    private Mat binarization;

    private int width;
    private int height;

    private int minWidth;
    private int maxWidth;
    private int minHeight;
    private int maxHeight;

    private Rect[] rectsResult;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        grey = inputFrame.gray();
        rgba = inputFrame.rgba();

        width = rgba.width();
        height = rgba.height();

        minWidth = 10;
        maxWidth = 1000;
        minHeight = 10;
        maxHeight = 1000;

        binarization = new Mat();

        Imgproc.threshold(grey, binarization, 127.0d, 225.0d, Imgproc.THRESH_BINARY_INV);

        {// labeling
            int[] labelBuffer = new int[width * height];
            for (int i = 0; i < width * height; i++) labelBuffer[i] = -1;

            int curLabelNum = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double pixel = binarization.get(y, x)[0];
                    int labelNum = labelBuffer[x + y * width];

                    if (pixel == 255.0 && labelNum != -1) {
                        labelBuffer[x + y * width] = curLabelNum++;

                        
                    }
                }
            }
        }
//        {
//            int[] imgBuffer = new int[width * height];
//            int[] labelBuffer = new int[width * height];
//
//            rectsResult = new Rect[width * height];
//
//            for (int h = 0; h < height; h++) {
//                for (int w = 0; w < width; w++) {
//                    imgBuffer[w + h * width] = (int) binarization.get(h, w)[0];// get(y, x);
//                    labelBuffer[w + h * width] = -1;
//                }
//            }
//
//            int labelCount = 0;
//
//            for (int h = 0; h < height; h++) {
//                for (int w = 0; w < width; w++) {
//                    if (imgBuffer[w + h * width] == 255 && labelBuffer[w + h * width] == -1) {
//                        Rect temp = new Rect();
//                        temp.x = w;
//                        temp.y = h;
//                        temp.width = 0;
//                        temp.height = 0;
//                        labelCount++;
//
//                        labeling(imgBuffer, labelBuffer, width, height, w, h, temp, labelCount);
//
//                        if (temp.width < minWidth || temp.width > maxWidth || temp.height < minHeight || temp.height > maxHeight) labelCount--;
//                        else rectsResult[labelCount - 1] = temp;
//                    }
//                }
//            }
//        }
//
//        for (Rect rect : rectsResult) {
//            if (rect == null) continue;
//            Imgproc.rectangle(rgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(255, 0, 0));
//        }

//        Imgproc.threshold(mGrey, binarization, 100, 255, Imgproc.THRESH_BINARY);
//        Imgproc.rectangle(mRgba, new Point(100, 200), new Point(100, 100), new Scalar(256, 0, 0), 3);

//        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        Mat intermediate = new Mat();
//        Imgproc.Canny(mRgba, intermediate, 80, 100);
//        Imgproc.findContours(intermediate, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//        Imgproc.drawContours(mRgba, contours, -1, new Scalar(255, 0, 0));

        return binarization;
    }

    private void labeling(int[] imgBuffer, int[] labelBuffer, int width, int height, int x, int y, Rect label, int labelNum) {
        labelBuffer[x + y * width] = labelNum;
        Point[] stack = new Point[width * height];
        stack[0].x = x;
        stack[0].y = y;
        int stackCount = 1;

        while (stackCount > 0) {
            stackCount--;

            x = (int) stack[stackCount].x;
            y = (int) stack[stackCount].y;

            if (label.x > x) {
                label.x = x;
            } else if (label.x + label.width < x) {
                label.width = x - label.x;
            }
            if (label.y > y) {
                label.y = y;
            } else if (label.y + label.height < y) {
                label.height = y - label.y;
            }

            int nX = x - 1;
            int pX = x + 1;
            int nY = y - 1;
            int pY = y + 1;

            if (nX >= 0) {
                if (imgBuffer[nX + y * width] == 255 & labelBuffer[nX + y * width] == -1) {
                    stack[stackCount].x = nX;
                    stack[stackCount].y = y;

                    labelBuffer[nX + y * width] = labelNum;
                    stackCount++;
                }
            }
            if (nY >= 0) {
                if (imgBuffer[x + nY * width] == 255 && labelBuffer[x + nY * width] == -1) {
                    stack[stackCount].x = x;
                    stack[stackCount].y = nY;

                    labelBuffer[x + nY * width] = labelNum;
                    stackCount++;
                }
            }
            if (pX < width) {
                if (imgBuffer[pX + y * width] == 255 && labelBuffer[pX + y * width] == -1) {
                    stack[stackCount].x = pX;
                    stack[stackCount].y = y;

                    labelBuffer[pX + y * width] = labelNum;
                    stackCount++;
                }
            }
            if (pY < height) {
                if (imgBuffer[x + pY * width] == 255 && labelBuffer[x + pY * width] == -1) {
                    stack[stackCount].x = x;
                    stack[stackCount].y = pY;

                    labelBuffer[x + pY * width] = labelNum;
                    stackCount++;
                }
            }
        }
    }

}
