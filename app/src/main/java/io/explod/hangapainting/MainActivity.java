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
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

	private static final String TAG = "Main";

	private static final int REQUEST_CAMERA_PERMISSION = 1;

	@Nullable
	private CameraBridgeViewBase mOpenCvCameraView;

	@Nullable
	private Lines mLines;

	@NonNull
	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i(TAG, "OpenCV loaded successfully");
					if (mOpenCvCameraView != null) {
						mOpenCvCameraView.enableView();
						mOpenCvCameraView.enableFpsMeter();
						mOpenCvCameraView.setMaxFrameSize(240, 240);
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
		mLines = new Lines(width, height);
	}

	@Override
	public void onCameraViewStopped() {
		// cool!
	}

	private int ct = 0;

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

		if (mLines == null) {
			throw new NullPointerException("lines not available");
		}

		Mat input = inputFrame.gray();
		Mat output = inputFrame.rgba();


		Mat thresh = mLines.addLines(input, output);

		ct++;

		if (ct > 10) {
			if (ct > 20) {
				ct = 0;
			}
			return thresh;
		} else {
			return output;
		}
	}


}
