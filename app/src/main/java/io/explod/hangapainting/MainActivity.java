package io.explod.hangapainting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

	private static final String TAG = "Main";

	private static final int REQUEST_CAMERA_PERMISSION = 1;

	@Nullable
	private CameraBridgeViewBase mOpenCvCameraView;

	@NonNull
	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					if (mOpenCvCameraView != null) {
						mOpenCvCameraView.enableView();
					}
				}
				break;
				default:
					super.onManagerConnected(status);
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		initializeCameraWithPermission();
	}

	private void initializeCameraWithPermission() {
		// Here, this is the current activity
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
				// todo: explain rationale with a dialog
				Toast.makeText(this, R.string.need_camera, Toast.LENGTH_LONG).show();
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
			}
		} else {
			initializeCamera();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CAMERA_PERMISSION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initializeCamera();
				} else {
					// todo: handle this better
					Toast.makeText(this, R.string.need_camera, Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	private void initializeCamera() {
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
		if (mOpenCvCameraView == null) {
			throw new NullPointerException("Views not correctly loaded");
		}
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
			Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		} else {
			Log.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// ok!
	}

	@Override
	public void onCameraViewStopped() {
		// cool!
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

		Mat input = inputFrame.gray();
		Mat output = inputFrame.rgba();

		Mat thresholdImage = new Mat(input.size(), CvType.CV_8UC1);
		Imgproc.Canny(input, thresholdImage, 80, 100);

		Mat lines = new Mat();

		int threshold = 50;
		int minLineSize = 20;
		int lineGap = 20;

		Imgproc.HoughLinesP(thresholdImage, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

		for (int x = 0; x < lines.cols(); x++) {
			double[] vec = lines.get(0, x);
			double x1 = vec[0],
				y1 = vec[1],
				x2 = vec[2],
				y2 = vec[3];
			Point start = new Point(x1, y1);
			Point end = new Point(x2, y2);

			Imgproc.line(output, start, end, new Scalar(255, 0, 0), 3);
		}

		return output;
	}


}
