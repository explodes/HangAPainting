package io.explod.hangapainting.ui;

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
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import io.explod.hangapainting.R;
import io.explod.hangapainting.lines.LineDrawer;
import io.explod.hangapainting.lines.HorizontalLineFinder;
import io.explod.hangapainting.lines.LineList;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

	private static final String TAG = "Main";

	private static final int REQUEST_CAMERA_PERMISSION = 1;

	private static final int MAX_LINES = 20;

	private static final int PROCESS_WIDTH = 200;

	private static final int PROCESS_HEIGHT = 200;

	private static final double MAX_ANGLE = 15;

	private static final int MIN_LINE_LENGTH = (int) (PROCESS_WIDTH * 0.10);

	private static final Scalar LINE_COLOR = new Scalar(255, 64, 64);

	private static final int LINE_THICKNESS = 3;

	private static final int REFRESH_FRQ = 12;

	@Nullable
	private CameraBridgeViewBase mOpenCvCameraView;

	@Nullable
	private HorizontalLineFinder mHorizontalLineFinder;

	@Nullable
	private LineList mLastLines;

	@NonNull
	private LineDrawer mLineDrawer = new LineDrawer(LINE_COLOR, LINE_THICKNESS);

	private double mScaleX;

	private double mScaleY;

	private long mFrameNumber = 0;

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
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
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
		mScaleX = width / (double) PROCESS_WIDTH;
		mScaleY = height / (double) PROCESS_HEIGHT;
		mHorizontalLineFinder = new HorizontalLineFinder(MAX_LINES, PROCESS_WIDTH, PROCESS_HEIGHT);
	}

	@Override
	public void onCameraViewStopped() {
		// cool!
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

		if (mHorizontalLineFinder == null) {
			throw new NullPointerException("lines not available");
		}

		Mat input = inputFrame.gray();
		Mat output = inputFrame.rgba();


		mFrameNumber++;
		if (mFrameNumber % REFRESH_FRQ == 0 || mLastLines == null) {
			mLastLines = mHorizontalLineFinder.findLines(input, MIN_LINE_LENGTH, MAX_ANGLE);
		}

		mLineDrawer.drawLines(output, mLastLines, mScaleX, mScaleY);

		return output;
	}


}
