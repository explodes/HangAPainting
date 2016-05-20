package io.explod.hangapainting;


import android.support.annotation.NonNull;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

class Lines {

	private static final int CANNY_THRESHOLD_1 = 80;
	private static final int CANNY_THRESHOLD_2 = 100;

	private static final double HOUGH_RHO = 1;
	private static final double HOUGH_THETA = Math.PI / 180;
	private static final int HOUGH_THRESHOLD = 0;
	private static final double HOUGH_MIN_LINE_SIZE = 20;
	private static final double HOUGH_LINE_GAP = 1;

	private static final int LINE_THICKNESS = 1;

	private static final double MAX_ANGLE = 15;

	@NonNull
	private final Mat mLines = new Mat();

	@NonNull
	private final Point mStart = new Point();

	@NonNull
	private final Point mEnd = new Point();

	@NonNull
	private final Scalar mColor = new Scalar(255, 0, 0);

	@NonNull
	private final double[] mPointBuffer = new double[2];

	@NonNull
	private final Mat mThresholdImage;

	Lines(int width, int height) {
		mThresholdImage = new Mat(width, height, CvType.CV_8UC1);
	}

	Mat addLines(@NonNull Mat input, @NonNull Mat output) {
		Imgproc.Canny(input, mThresholdImage, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2);
		Imgproc.HoughLinesP(mThresholdImage, mLines, HOUGH_RHO, HOUGH_THETA, HOUGH_THRESHOLD, HOUGH_MIN_LINE_SIZE, HOUGH_LINE_GAP);

		for (int x = 0; x < mLines.rows(); x++) {
			double[] vec = mLines.get(x, 0);

			mPointBuffer[0] = vec[0];
			mPointBuffer[1] = vec[1];
			mStart.set(mPointBuffer);

			mPointBuffer[0] = vec[2];
			mPointBuffer[1] = vec[3];
			mEnd.set(mPointBuffer);

			if (isHorizontalEnough(mStart, mEnd)) {
				Imgproc.line(output, mStart, mEnd, mColor, LINE_THICKNESS);
			}
		}

		return mThresholdImage;
	}

	private boolean isHorizontalEnough(@NonNull Point start, @NonNull Point end) {

		// get the angle 0 < theta < 2 * pi
		double angle = Math.atan2(end.y - start.y, end.x - start.x) + Math.PI;

		// convert to degrees
		angle = angle * 180 / Math.PI;

		// rotate the angle 90 to the right so that 0 degrees is the horizontal right
		angle -= 90;


		if (angle <= MAX_ANGLE) {
			// just above the right horizontal line
			return true;
		}

		if (angle >= (360 - MAX_ANGLE)) {
			// just below the right horizontal line
			return true;
		}

		if ((180 - MAX_ANGLE) <= angle && (angle <= 180 + MAX_ANGLE)) {
			// between above and below the left horizontal line and
			return true;
		}

		return false;
	}

}
