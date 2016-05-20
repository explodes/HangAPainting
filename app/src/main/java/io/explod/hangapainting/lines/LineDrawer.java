package io.explod.hangapainting.lines;


import android.support.annotation.NonNull;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class LineDrawer {

	@NonNull
	private final Point mStart = new Point();

	@NonNull
	private final Point mEnd = new Point();

	@NonNull
	private final double[] mPointBuffer = new double[2];

	@NonNull
	private Scalar mColor;

	private int mThickness;

	public LineDrawer(@NonNull Scalar color, int thickness) {
		mColor = color;
		mThickness = thickness;
	}

	public void setColor(@NonNull Scalar color) {
		mColor = color;
	}

	public void setThickness(int thickness) {
		mThickness = thickness;
	}

	public void drawLines(@NonNull Mat image, @NonNull LineList lines, double scaleX, double scaleY) {

		for (int index = 0, N = lines.size(); index < N; index++) {

			Line line = lines.getLine(index);

			mPointBuffer[0] = line.getX1() * scaleX;
			mPointBuffer[1] = line.getY1() * scaleY;
			mStart.set(mPointBuffer);

			mPointBuffer[0] = line.getX2() * scaleX;
			mPointBuffer[1] = line.getY2() * scaleY;
			mEnd.set(mPointBuffer);

			Imgproc.line(image, mStart, mEnd, mColor, mThickness);

		}
	}
}
