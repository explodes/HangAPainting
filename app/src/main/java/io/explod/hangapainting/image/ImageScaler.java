package io.explod.hangapainting.image;


import android.support.annotation.NonNull;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageScaler {

	@NonNull
	private final Mat mScaledImage = new Mat();

	@NonNull
	private final Size mScaleSize = new Size();

	@NonNull
	private final double[] mScaleBuffer = new double[2];

	@NonNull
	public Mat getScaledImage(@NonNull Mat image, int width, int height) {
		mScaleBuffer[0] = width;
		mScaleBuffer[1] = height;
		mScaleSize.set(mScaleBuffer);
		Imgproc.resize(image, mScaledImage, mScaleSize);
		return mScaledImage;
	}

}
