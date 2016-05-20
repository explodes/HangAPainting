package io.explod.hangapainting.lines;


import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.explod.hangapainting.image.ImageScaler;

public class HorizontalLineFinder {

	private static final int CANNY_THRESHOLD_1 = 80;
	private static final int CANNY_THRESHOLD_2 = 100;

	private static final double HOUGH_RHO = 100;
	private static final double HOUGH_THETA = Math.PI;
	private static final int HOUGH_THRESHOLD = 0;
	private static final double HOUGH_LINE_GAP = 10;

	@NonNull
	private final Mat mLines = new Mat();

	@NonNull
	private final ImageScaler mImageScaler = new ImageScaler();

	@NonNull
	private final Mat mThresholdImage;

	@NonNull
	private final LineListImpl mLineList;

	public HorizontalLineFinder(int maxLines, int processWidth, int processHeight) {
		mLineList = new LineListImpl(maxLines);
		mThresholdImage = new Mat(processWidth, processHeight, CvType.CV_8UC1);
	}

	@NonNull
	public LineList findLines(@NonNull Mat input, int minLength, double toleranceDegrees) {

		// find all of the lines, put them in mLines
		//// scale down for performance
		Mat scaledImage = mImageScaler.getScaledImage(input, mThresholdImage.width(), mThresholdImage.height());
		//// edge detection
		Imgproc.Canny(scaledImage, mThresholdImage, CANNY_THRESHOLD_1, CANNY_THRESHOLD_2);
		//// straight-line detection
		Imgproc.HoughLinesP(mThresholdImage, mLines, HOUGH_RHO, HOUGH_THETA, HOUGH_THRESHOLD, minLength, HOUGH_LINE_GAP);

		// add all the lines to our list, filling only up to our capacity
		mLineList.clear();
		for (int x = 0; x < mLines.rows() && x < mLineList.capacity(); x++) {
			double[] vec = mLines.get(x, 0);
			if (isHorizontalEnough(toleranceDegrees, vec[0], vec[1], vec[2], vec[3])) {
				mLineList.addLine(vec[0], vec[1], vec[2], vec[3]);
			}
		}

		return mLineList;
	}

	@NonNull
	@VisibleForTesting
	public Mat getThresholdImage() {
		return mThresholdImage;
	}

	private boolean isHorizontalEnough(double tolerance, double x1, double y1, double x2, double y2) {

		// get the angle 0 < theta < 2 * pi
		double angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;

		// convert to degrees
		angle = angle * 180 / Math.PI;

		// rotate the angle 90 to the right so that 0 degrees is the horizontal right
		angle -= 90;

		if (angle <= tolerance) {
			// just above the right horizontal line
			return true;
		}

		if (angle >= (360 - tolerance)) {
			// just below the right horizontal line
			return true;
		}

		if ((180 - tolerance) <= angle && (angle <= 180 + tolerance)) {
			// between above and below the left horizontal line and
			return true;
		}

		return false;
	}

	private static class LineListImpl implements LineList {

		@NonNull
		private final LineImpl[] mLines;

		private int mSize;

		LineListImpl(int capacity) {
			mLines = new LineImpl[capacity];
			for (int index = 0; index < capacity; index++) {
				mLines[index] = new LineImpl();
			}
		}

		int capacity() {
			return mLines.length;
		}

		void clear() {
			mSize = 0;
		}

		void addLine(double x1, double y1, double x2, double y2) {
			mLines[mSize].set(x1, y1, x2, y2);
			mSize++;

		}

		@Override

		public int size() {
			return mSize;
		}

		@Override
		public Line getLine(int index) {
			return mLines[index];
		}
	}

	private static class LineImpl implements Line {

		private double x1, x2, y1, y2;

		void set(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		@Override
		public double getX1() {
			return x1;
		}

		@Override
		public double getX2() {
			return x2;
		}

		@Override
		public double getY1() {
			return y1;
		}

		@Override
		public double getY2() {
			return y2;
		}
	}

}
