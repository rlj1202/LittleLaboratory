package redlaboratory.littlelaboratory;

import android.content.Intent;
import android.graphics.Region;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class OpenCVTestActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            Log.i("LittleLaboratory", "OpenCV initialize failed");
        } else {
            Log.i("LittleLaboratory", "OpenCV initialize success");
        }
    }

    public static class CircleMarker {

        private Rect outterRect;
        private Rect innerRect;
        private Rect middleRect;

        private Point center;

        public CircleMarker(Rect outterRect, Rect innerRect, Rect middleRect, Point center) {
            this.outterRect = outterRect;
            this.innerRect = innerRect;
            this.middleRect = middleRect;

            this.center = center;
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

    private boolean measuring = false;
    private List<CircleMarker> track = new ArrayList<>();
    private double calibration = -1;// unit is "diameter in pixel per meter"

    private long prevMillis;
    private ArrayList<Double> record = new ArrayList<>();

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

        final TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(R.string.not_calibrated);

        final FloatingActionButton measureToggleButton = (FloatingActionButton) findViewById(R.id.measureToggleButton);
        measureToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!measuring) {
                    if (calibration != -1) {
                        measuring = true;
                        prevMillis = System.currentTimeMillis();
                        measureToggleButton.setImageResource(R.drawable.ic_stop_white_24dp);
                    }
                } else {
                    measuring = false;
                    measureToggleButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);

                    Intent intent = new Intent();
                    intent.putExtra("distances", record);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        FloatingActionButton calibratingButton = (FloatingActionButton) findViewById(R.id.calibratingButton);
        calibratingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!measuring) {
                    if (track.size() > 0) {
                        CircleMarker lastMarker = track.get(track.size() - 1);
                        calibration = Math.max(lastMarker.outterRect.width, lastMarker.outterRect.height);

                        textView.setText(String.format(Locale.KOREA, "%f pixel area per meter", calibration));
                    }
                }
            }
        });
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
        return circleMarker(inputFrame);
    }

    private Mat circleMarker(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat grey = inputFrame.gray();
        Mat rgba = inputFrame.rgba();

        int width = rgba.width();
        int height = rgba.height();

        double screenArea = width * height;

        Mat binarization = new Mat();
        Mat binarizationInv = new Mat();

        Imgproc.threshold(grey, binarization, 127, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(grey, binarizationInv, 127, 255, Imgproc.THRESH_BINARY);

        Mat labeled = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        // int labels =
        Imgproc.connectedComponentsWithStats(binarization, labeled, stats, centroids, 4, CvType.CV_16U);

        List<CircleMarker> markers = new ArrayList<>();

        int[] outterRectInfo = new int[5];
        double[] outterRectCenterInfo = new double[2];
        for (int outterRectIndex = 1; outterRectIndex < stats.rows(); outterRectIndex++) {
            stats.row(outterRectIndex).get(0, 0, outterRectInfo);
            centroids.row(outterRectIndex).get(0, 0, outterRectCenterInfo);
            Rect outterRect = new Rect(outterRectInfo[0], outterRectInfo[1], outterRectInfo[2], outterRectInfo[3]);
            Point outterRectCenter = new Point(outterRectCenterInfo[0], outterRectCenterInfo[1]);

            if (outterRect.area() / screenArea >= 0.8) continue;
            if (outterRect.area() / screenArea <= 0.0003) continue;
            if (outterRect.area() < 100) continue;
            if (outterRect.width / width > 0.8) continue;
            if (outterRect.height / height > 0.8) continue;

            Imgproc.rectangle(rgba, outterRect.br(), outterRect.tl(), new Scalar(255, 0, 0), 1);// TODO

            Mat binaryInvCut = binarizationInv.submat(outterRect);

            Mat innerLabeled = new Mat();
            Mat innerStats = new Mat();
            Mat innerCentroids = new Mat();

            Imgproc.connectedComponentsWithStats(binaryInvCut, innerLabeled, innerStats, innerCentroids, 4, CvType.CV_16U);

            List<Rect> passedInnerRects = new ArrayList<>();
            List<Rect> passedMiddleRects = new ArrayList<>();

            int[] innerRectInfo = new int[5];
            double[] innerRectCenterInfo = new double[2];
            for (int innerRectIndex = 1; innerRectIndex < innerStats.rows(); innerRectIndex++) {
                innerStats.row(innerRectIndex).get(0, 0, innerRectInfo);
                innerCentroids.row(innerRectIndex).get(0, 0, innerRectCenterInfo);
                Rect innerRect = new Rect(innerRectInfo[0], innerRectInfo[1], innerRectInfo[2], innerRectInfo[3]);
                Rect innerRectAb = innerRect.clone(); innerRectAb.x += outterRect.x; innerRectAb.y += outterRect.y;
                Point innerRectCenter = new Point(innerRectCenterInfo[0], innerRectCenterInfo[1]);
                Point innerRectCenterAb = innerRectCenter.clone(); innerRectCenterAb.x += outterRect.x; innerRectCenterAb.y += outterRect.y;

                Imgproc.rectangle(rgba, innerRectAb.br(), innerRectAb.tl(), new Scalar(255, 0, 0), 1);

                double areaRatio = innerRect.area() / outterRect.area();
                final double areaRatioConstant = 0.25;
                final double areaRatioDelta = 0.1;

                double centerDistanceSquare = Math.pow(innerRectCenter.x + outterRect.x - outterRectCenter.x, 2) + Math.pow(innerRectCenter.y + outterRect.y - outterRectCenter.y, 2);
                double outterRectDistanceSquare = Math.pow(outterRect.tl().x - outterRect.br().x, 2) + Math.pow(outterRect.tl().y - outterRect.br().y, 2);
                double distanceRatio = centerDistanceSquare / outterRectDistanceSquare;

                boolean innerAreaCondition = (areaRatioConstant - areaRatioDelta <= areaRatio) && (areaRatio <= areaRatioConstant + areaRatioDelta);
                boolean innerDistanceCondition = distanceRatio <= 0.01;
                if (innerAreaCondition && innerDistanceCondition) {
                    int[] middleRectInfo = new int[5];
                    double[] middleRectCenterInfo = new double[2];
                    for (int middleRectIndex = 1; middleRectIndex < stats.rows(); middleRectIndex++) {
                        stats.row(middleRectIndex).get(0, 0, middleRectInfo);
                        centroids.row(middleRectIndex).get(0, 0, middleRectCenterInfo);
                        Rect middleRect = new Rect(middleRectInfo[0], middleRectInfo[1], middleRectInfo[2], middleRectInfo[3]);
                        Point middleRectCenter = new Point(middleRectCenterInfo[0], middleRectCenterInfo[1]);

                        if (innerRectAb.contains(middleRect.br()) && innerRectAb.contains(middleRect.tl())) {
                            double distanceBetweenInnerAndMiddleSquare = Math.pow(innerRectCenterAb.x - middleRectCenter.x, 2) + Math.pow(innerRectCenterAb.y - middleRectCenter.y, 2);

                            double middleAreaRatio = middleRect.area() / outterRect.area();
                            final double middleRatioConstant = 0.04;
                            final double middleRatioDelta = 0.02;

                            Imgproc.circle(rgba, innerRectCenterAb, 2, new Scalar(0, 255, 0), 4);// TODO
                            Imgproc.circle(rgba, middleRectCenter, 2, new Scalar(0, 0, 255), 4);// TODO

                            boolean middleRectDistanceCondition = distanceBetweenInnerAndMiddleSquare < 100;
                            boolean middleAreaCondition = (middleRatioConstant - middleRatioDelta <= middleAreaRatio) && (middleAreaRatio <= middleRatioConstant + middleRatioDelta);
                            if (middleRectDistanceCondition && middleAreaCondition) {
                                passedMiddleRects.add(middleRect);

                                // TODO
                                Imgproc.putText(rgba, "distanceRatio " + Math.round(distanceBetweenInnerAndMiddleSquare*1000)/1000.0, middleRectCenter, 3, 1, new Scalar(0, 0, 255));
                            }
                        }
                    }

                    if (passedMiddleRects.size() == 1) {
                        passedInnerRects.add(innerRectAb);

                        // TODO
                        Imgproc.putText(rgba, "distanceRatio " + Math.round(distanceRatio*1000)/1000.0, innerRectAb.br(), 3, 1, new Scalar(0, 255, 0));
                        Imgproc.putText(rgba, "areaRatio " + Math.round(areaRatio*1000)/1000.0, new Point(innerRectAb.br().x, innerRectAb.br().y - 20), 3, 1, new Scalar(0, 255, 0));
                    }
                }
            }

            if (passedInnerRects.size() == 1) {
                markers.add(new CircleMarker(outterRect, passedInnerRects.get(0), passedMiddleRects.get(0), outterRectCenter));
            }
        }

        for (CircleMarker marker : markers) {
            double diameter = Math.max(marker.outterRect.width, marker.outterRect.height);

            double distance = calibration / diameter;

            Imgproc.rectangle(rgba, marker.outterRect.br(), marker.outterRect.tl(), new Scalar(255, 0, 0), 4);
            Imgproc.rectangle(rgba, marker.innerRect.br(), marker.innerRect.tl(), new Scalar(0, 255, 0), 4);
            Imgproc.rectangle(rgba, marker.middleRect.br(), marker.middleRect.tl(), new Scalar(0, 0, 255), 4);

            Imgproc.putText(rgba, "distance: " + distance, marker.center, 3, 1, new Scalar(255, 255, 255));
        }

        if (track.size() == 0) {
            if (markers.size() == 1) {
                track.add(markers.get(0));
            }
        } else {
            if (markers.size() >= 1) {
                CircleMarker lastMarker = track.get(0);

                CircleMarker bestMarker = null;
                double bestAreaSimilarity = 0;

                for (CircleMarker curMarker : markers) {
                    double areaSimilarity = curMarker.outterRect.area() / lastMarker.outterRect.area();

                    if (areaSimilarity > bestAreaSimilarity) {
                        bestMarker = curMarker;
                        bestAreaSimilarity = areaSimilarity;
                    }
                }

                if (bestMarker != null) {
                    track.add(bestMarker);

                    if (measuring) {
                        double diameter = Math.max(bestMarker.outterRect.width, bestMarker.outterRect.height);
                        double distance = calibration / diameter;

                        long curMillis = System.currentTimeMillis();
                        long deltaMillis = curMillis - prevMillis;

                        record.add(deltaMillis / 1000.0);
                        record.add(distance);
                    }

                    if (!measuring && track.size() > 20) {
                        track.remove(0);
                    }
                }
            }
        }

        Point prevCenter = null;
        for (CircleMarker marker : track.subList(Math.max(0, track.size() - 50), Math.max(0, track.size() - 1))) {
            if (prevCenter != null) Imgproc.line(rgba, prevCenter, marker.center, new Scalar(255, 0, 255), 4);
            prevCenter = marker.center;
        }

        return rgba;
    }

    @Deprecated
    private List<List<CircleMarker>> proceedMarkers(List<List<CircleMarker>> tracks, List<CircleMarker> markers) {
        for (CircleMarker marker : markers) {
            List<CircleMarker> bestTrack = null;
            double bestSizeSimilarity = 0;
            double bestDistanceSquareSimilarity = 0;

            for (List<CircleMarker> track : tracks) {
                CircleMarker lastMarker = track.get(track.size() - 1);
                double sizeSimilarity = marker.outterRect.area() / lastMarker.outterRect.area();
                double distanceSquareSimilarity = Math.pow(marker.center.x - lastMarker.center.x, 2) + Math.pow(marker.center.y - lastMarker.center.y, 2);

                if (bestSizeSimilarity < sizeSimilarity) {
                    bestTrack = track;
                    bestSizeSimilarity = sizeSimilarity;
                    bestDistanceSquareSimilarity = distanceSquareSimilarity;
                }
            }

            if (bestTrack != null) {
                Log.v("LittleLaboratory", "add marker {bestSizeSimilarity: " + bestSizeSimilarity + ", bestDistanceSquareSimilarity: " + bestDistanceSquareSimilarity + "}");

                boolean sizeCondition = bestSizeSimilarity > 0.8;
                boolean distanceCondition =  bestDistanceSquareSimilarity < 4000;
                if (sizeCondition) {
                    bestTrack.add(marker);

                    return tracks;
                }
            }

            Log.v("LittleLaboratory", "new track.");

            List<CircleMarker> track = new ArrayList<>();
            track.add(marker);

            tracks.add(track);
        }

        return tracks;
    }

    @Deprecated
    public Mat newMethod(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat grey = inputFrame.gray();
        Mat rgba = inputFrame.rgba();

        int width = rgba.width();
        int height = rgba.height();

        double screenArea = width * height;

        Mat binarization = new Mat();
        Mat binarizationInv = new Mat();

        Imgproc.threshold(grey, binarization, 127, 255, Imgproc.THRESH_BINARY_INV);
        Imgproc.threshold(grey, binarizationInv, 127, 255, Imgproc.THRESH_BINARY);

        Mat labeled = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        // int labels =
        Imgproc.connectedComponentsWithStats(binarization, labeled, stats, centroids, 4, CvType.CV_16U);

        int[] outterRectInfo = new int[5];
        double[] outterRectCenterInfo = new double[2];
        for (int outterRectIndex = 1; outterRectIndex < stats.rows(); outterRectIndex++) {
            stats.row(outterRectIndex).get(0, 0, outterRectInfo);
            centroids.row(outterRectIndex).get(0, 0, outterRectCenterInfo);
            Rect outterRect = new Rect(outterRectInfo[0], outterRectInfo[1], outterRectInfo[2], outterRectInfo[3]);
            Point outterRectCenter = new Point(outterRectCenterInfo[0], outterRectCenterInfo[1]);

            if (outterRect.area() / screenArea >= 0.8) continue;
            if (outterRect.area() / screenArea <= 0.0003) continue;
            if (outterRect.area() < 100) continue;
            if (outterRect.width / width > 0.8) continue;
            if (outterRect.height / height > 0.8) continue;

            Imgproc.rectangle(rgba, outterRect.br(), outterRect.tl(), new Scalar(255, 0, 0));// TODO

            Mat binaryInvCut = binarizationInv.submat(outterRect);

            Mat innerLabeled = new Mat();
            Mat innerStats = new Mat();
            Mat innerCentroids = new Mat();

            Imgproc.connectedComponentsWithStats(binaryInvCut, innerLabeled, innerStats, innerCentroids, 4, CvType.CV_16U);

            int passedInnerRects = 0;

            int[] innerRectInfo = new int[5];
            double[] innerRectCenterInfo = new double[2];
            for (int innerRectIndex = 1; innerRectIndex < innerStats.rows(); innerRectIndex++) {
                innerStats.row(innerRectIndex).get(0, 0, innerRectInfo);
                innerCentroids.row(innerRectIndex).get(0, 0, innerRectCenterInfo);
                Rect innerRect = new Rect(innerRectInfo[0], innerRectInfo[1], innerRectInfo[2], innerRectInfo[3]);
                Point innerRectCenter = new Point(innerRectCenterInfo[0], innerRectCenterInfo[1]);

                double areaRatio = innerRect.area() / outterRect.area();
                final double areaRatioConstant = 0.03515625;

                double centerDistanceSquare = Math.pow(innerRectCenter.x + outterRect.x - outterRectCenter.x, 2) + Math.pow(innerRectCenter.y + outterRect.y - outterRectCenter.y, 2);
                double outterRectDistanceSquare = Math.pow(outterRect.tl().x - outterRect.br().x, 2) + Math.pow(outterRect.tl().y - outterRect.br().y, 2);
                double distanceRatio = centerDistanceSquare / outterRectDistanceSquare;
                final double distanceRatioConstant = 0.09765625;

                boolean areaCondition = 0.01 <= areaRatio && areaRatio <= 0.05;

                boolean distanceCondition = 0.04 <= distanceRatio && distanceRatio <= 0.15;

                if (areaCondition && distanceCondition) {
                    passedInnerRects++;

                    Rect rectToDraw = innerRect.clone();
                    rectToDraw.x += outterRect.x;
                    rectToDraw.y += outterRect.y;
                    Imgproc.rectangle(rgba, rectToDraw.br(), rectToDraw.tl(), new Scalar(0, 255, 0));
                }
            }

            if (passedInnerRects > 0) {
                Imgproc.rectangle(rgba, outterRect.br(), outterRect.tl(), new Scalar(255, 0, 0), 3);
            }
        }

        return rgba;
    }

    @Deprecated
    public Mat old(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
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
            centroids.row(i).get(0, 0, centroidInfo);
            Rect rect = new Rect(rectInfo[0], rectInfo[1], rectInfo[2], rectInfo[3]);
            Point centroid = new Point(centroidInfo[0], centroidInfo[1]);

//            double areaRatio = rect.area() / screenArea;
//            if (areaRatio > 0.8) continue;
            if ((rect.height / (float) height) > 0.8) continue;
            if ((rect.width / (float) width) > 0.8) continue;

            rects[rectsSize++] = rect;

            Mat rgbaCut = rgba.submat(rect);
            Mat binaryCut = binarizationInv.submat(rect);

            Mat labeledInv = new Mat();
            Mat statsInv = new Mat();
            Mat centroidsInv = new Mat();
            Imgproc.connectedComponentsWithStats(binaryCut, labeledInv, statsInv, centroidsInv, 4, CvType.CV_16U);
            int[] rectInvInfo = new int[5];
            double[] centroidInvInfo = new double[2];
            int checkedRectInvs = 0;
            for (int j = 1; j < statsInv.rows(); j++) {
                statsInv.row(j).get(0, 0, rectInvInfo);
                centroidsInv.row(j).get(0, 0, centroidInvInfo);
                Rect rectInv = new Rect(rectInvInfo[0], rectInvInfo[1], rectInvInfo[2], rectInvInfo[3]);
                Point centroidInv = new Point(centroidInvInfo[0], centroidInvInfo[1]);

                double centerDistanceSquare = Math.pow(centroidInv.x + rect.x - centroid.x, 2) + Math.pow(centroidInv.y + rect.y - centroid.y, 2);
                double rectDistanceSquare = Math.pow(rect.tl().x - rect.br().x, 2) + Math.pow(rect.tl().y - rect.br().y, 2);
                double distanceRatio = centerDistanceSquare / rectDistanceSquare;
                if (distanceRatio > 0.005) continue;

                double areaRatioInv = rectInv.area() / rect.area();
                if (areaRatioInv > 0.8 || areaRatioInv < 0.05) continue;
                if ((rectInv.height / (float) rect.height) > 0.8) continue;
                if ((rectInv.width / (float) rect.width) > 0.8) continue;

                checkedRectInvs++;

                List<MatOfPoint> contours = new ArrayList<>();
                Mat intermediate = new Mat();
                Mat hierarchy = new Mat();
                Imgproc.Canny(rgbaCut, intermediate, 80, 100);
                Imgproc.findContours(intermediate, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
                for (Iterator<MatOfPoint> it = contours.iterator(); it.hasNext();) {
                    MatOfPoint contour = it.next();

                    Rect ret = null;

                    MatOfPoint2f thisContour2f = new MatOfPoint2f();
                    MatOfPoint approxContour = new MatOfPoint();
                    MatOfPoint2f approxContour2f = new MatOfPoint2f();

                    contour.convertTo(thisContour2f, CvType.CV_32FC2);

                    Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

                    approxContour2f.convertTo(approxContour, CvType.CV_32S);

                    if (approxContour.size().height == 4) {
                        ret = Imgproc.boundingRect(approxContour);
                    }

                    if (ret == null) {
                        it.remove();
                    }
                }
                for (int index = 0; index < contours.size(); index++) Imgproc.drawContours(rgbaCut, contours, index, new Scalar(255, 255, 255), 2);

                rectInv.x += rect.x;
                rectInv.y += rect.y;
                centroidInv.x += rect.x;
                centroidInv.y += rect.y;
                Imgproc.rectangle(rgba, rectInv.br(), rectInv.tl(), new Scalar(0, 255, 0));
                Imgproc.circle(rgba, centroidInv, 2, new Scalar(0, 255, 0));
                Imgproc.putText(rgba, "inner: area " + Math.round(areaRatioInv*10000)/10000.0 + ", distance " + Math.round(distanceRatio*10000)/10000.0 + ", contours " + contours.size(), new Point(centroidInvInfo[0] + rect.x, centroidInvInfo[1] + rect.y), 3, 1, new Scalar(255, 0, 255));
            }

            if (checkedRectInvs != 0) {
                Imgproc.rectangle(rgba, rect.br(), rect.tl(), new Scalar(255, 0, 0));
                Imgproc.circle(rgba, centroid, 2, new Scalar(255, 0, 0));
                Imgproc.putText(rgba, "outter", rect.br(), 3, 1, new Scalar(255, 0, 255));
            }
        }

        return rgba;
    }

}
