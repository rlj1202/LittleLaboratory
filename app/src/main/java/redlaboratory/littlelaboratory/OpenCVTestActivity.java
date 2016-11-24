package redlaboratory.littlelaboratory;

import android.graphics.Region;
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
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;
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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.v("LittleLaboratory", "OpenCV onCameraFrame");

        Mat grey = inputFrame.gray();
        Mat rgba = inputFrame.rgba();

        int width = rgba.width();
        int height = rgba.height();

        int screenArea = width * height;

        Mat binarization = new Mat();
        Mat binarizationInv = new Mat();

        Mat labeled = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        Imgproc.threshold(grey, binarization, 127.0d, 225.0d, Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(grey, binarizationInv, 127.0d, 225.0d, Imgproc.THRESH_BINARY);
        int labels = Imgproc.connectedComponentsWithStats(binarization, labeled, stats, centroids, 4, CvType.CV_16U);

        Rect[] rects = new Rect[labels];
        int rectsSize = 0;

        int[] rectInfo = new int[5];
        double[] centroidInfo = new double[2];

        for (int i = 1; i < stats.rows(); i++) {
            stats.row(i).get(0, 0, rectInfo);
            Rect rect = new Rect(rectInfo[0], rectInfo[1], rectInfo[2], rectInfo[3]);

//            if (rect.width / (float) width > 0.8 || rect.height / (float) height > 0.8) continue;
            double areaRatio = rect.area() / screenArea;
            if (areaRatio > 0.8 || areaRatio < 0.1) continue;
            int ratio = rect.height / rect.width;
//            if (5 < ratio || ratio < 0.2) continue;

            rects[rectsSize++] = rect;

            Mat cut = binarizationInv.submat(rect);

            Mat labeledInv = new Mat();
            Mat statsInv = new Mat();
            Mat centroidsInv = new Mat();
            Imgproc.connectedComponentsWithStats(cut, labeledInv, statsInv, centroidsInv, 4, CvType.CV_16U);
            int[] rectInvInfo = new int[5];
            for (int j = 1; j < statsInv.rows(); j++) {
                statsInv.row(j).get(0, 0, rectInvInfo);
                Rect rectInv = new Rect(rectInvInfo[0], rectInvInfo[1], rectInvInfo[2], rectInvInfo[3]);

                if (rectInv.area() < 200) continue;
                int ratioInv = rectInv.height / rectInv.width;
                if (5 < ratioInv || ratioInv < 0.2) continue;

                rectInv.x += rect.x;
                rectInv.y += rect.y;

                Imgproc.rectangle(rgba, rectInv.br(), rectInv.tl(), new Scalar(0, 255, 0));
            }

//            centroids.row(i).get(0, 0, centroidInfo);
//            Point centroid = new Point(centroidInfo[0], centroidInfo[1]);

            Imgproc.rectangle(rgba, rect.br(), rect.tl(), new Scalar(255, 0, 0));
//            Imgproc.circle(rgba, centroid, 2, new Scalar(0, 255, 0));
        }

        return rgba;
    }

}
