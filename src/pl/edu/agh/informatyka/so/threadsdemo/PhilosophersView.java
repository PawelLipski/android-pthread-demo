package pl.edu.agh.informatyka.so.threadsdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class PhilosophersView extends RelativeLayout {

	private static final int FORK_COUNT = 5;
	private static final int BASE_ANGLE = 360 / FORK_COUNT;
	private static final int SHIFT_ANGLE = BASE_ANGLE / 8;

	private boolean active = false;
	private TableState currentState;
	private Bitmap basicForkImage;

	private ImageView forkImages[];
	private PlateView[] plates;

	private int width, height;
	private int xMiddle, yMiddle;
	private int tableRadius;

	private int smallerDim;
	private int forkLength;
	private int plateRadius;
	private float imageToBitmapScale;

	private ImageView testFork;


	public PhilosophersView(Context context) {
		super(context);
		initImages();
	}

	public PhilosophersView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initImages();
	}

	public PhilosophersView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initImages();
	}

	public void initImages() {
		basicForkImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.fork);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		//Log.e("SWIPED", "onLayout : " + Boolean.toString(changed));
		//Log.e("SWIPED", "l=" + l + ", t=" + t + ", r=" + r + ", b=" + b);
		if (!changed) return;

		forkImages = new ImageView[FORK_COUNT];

		Log.e("PhilosophersView", "Creating PlateViews...");
		plates = new PlateView[FORK_COUNT];

		setUpDimensions();

		Log.e("PhilosophersView", "Drawing forks and plates");

		for (int i = 0; i < FORK_COUNT; i++) {
			createForkImage(i);
			adjustForkImage(i, BASE_ANGLE * i);

			createPlate(i, BASE_ANGLE * i + BASE_ANGLE / 2, 0.0);
		}

		currentState = new TableState(FORK_COUNT);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				active = true;
				//createTestFork();
			}
		}, 2000);


	}

	private void createTestFork() {
		testFork = new ImageView(getContext());
		addView(testFork);
		rotateTestFork(0);
	}

	private void rotateTestFork(final int angle) {
		Bitmap rotated = rotate(basicForkImage, angle);
		testFork.setImageBitmap(rotated);

		int bw = rotated.getWidth(), bh = rotated.getHeight();
		int iw = (int) (bw * imageToBitmapScale), ih = (int) (bh * imageToBitmapScale);
		//Log.e("rotateTestFork", "fs: " + forkLength + ", bfi-h: " + basicForkImage.getHeight());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iw, ih);

		params.leftMargin = width / 2 - iw / 2;
		params.topMargin = height / 2 - ih / 2;

		testFork.setLayoutParams(params);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				rotateTestFork(angle + 10);
			}
		}, 100);
	}

	private void setUpDimensions() {

		width = getWidth();
		height = getHeight();
		smallerDim = Math.min(width, height);

		xMiddle = width / 2;
		yMiddle = (int) (height * 0.5);

		tableRadius = (int) (smallerDim * 0.25);
		forkLength = tableRadius * 4 / 3;
		plateRadius = tableRadius * 3 / 4;

		imageToBitmapScale = (float) forkLength / basicForkImage.getHeight();
	}

	protected void adjustForkImage(final int forkNo, int angle) {

		final Bitmap rotated = rotate(basicForkImage, angle - 90);

		int bw = rotated.getWidth(), bh = rotated.getHeight();
		int iw = (int) (bw * imageToBitmapScale), ih = (int) (bh * imageToBitmapScale);

		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iw, ih);

		//int forkShift = forkLength / 2;

		double angleRad = Math.toRadians(angle);
		params.leftMargin = (int) (xMiddle + tableRadius * Math.cos(angleRad) - iw / 2);
		params.topMargin = (int) (yMiddle + tableRadius * Math.sin(angleRad) - ih / 2);


		((Activity) getContext()).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				synchronized (PhilosophersView.this) {
					if (active) {
						forkImages[forkNo].setImageBitmap(rotated);
						forkImages[forkNo].setLayoutParams(params);
					}
				}
			}
		});

	}

	private void createForkImage(int forkNo) {

		forkImages[forkNo] = new ImageView(getContext());
		//forkImages[forkNo].setImageResource(R.drawable.fork);

		//Log.e("PhilosophersView", "Created fork image #" + forkNo);
		addView(forkImages[forkNo]);
		//Log.e("PhilosophersView", "Added view for image #" + forkNo);
	}

	private synchronized void createPlate(int plateNo, int angle, double value) {
		if (!active) return;

		double cirAngleRad = Math.toRadians(angle);
		String plateText;
		if (value < 1000)
			plateText = String.format("%.1f", value);
		else if (value < 10000)
			plateText = String.format("%.0f", value);
		else {
			plateText = "long";
		}
		plates[plateNo] = new PlateView(getContext(), plateText);

		RelativeLayout.LayoutParams cirParams = new RelativeLayout.LayoutParams(plateRadius, plateRadius);
		int plateShift = plateRadius / 2;
		cirParams.leftMargin = (int) (xMiddle - plateShift + tableRadius * Math.cos(cirAngleRad));
		cirParams.topMargin = (int) (yMiddle - plateShift + tableRadius * Math.sin(cirAngleRad));
		addView(plates[plateNo], cirParams);
	}

	/*public Bitmap rotate(Bitmap bitmap, double degrees) {

		Matrix matrix = new Matrix();
		matrix.setRotate((float) degrees);

		Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

		return rotated;
	}*/

	public Bitmap rotate(Bitmap bitmap, double degrees) {

		// precompute some trig functions
		double radians = Math.toRadians(degrees);
		double sin = Math.abs(Math.sin(radians));
		double cos = Math.abs(Math.cos(radians));
		// figure out total width and height of new bitmap
		double newWidth = bitmap.getWidth() * cos + bitmap.getHeight() * sin;
		double newHeight = bitmap.getWidth() * sin + bitmap.getHeight() * cos;
		// set up matrix
		Matrix matrix = new Matrix();
		matrix.setRotate((float) degrees);

		Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

		Log.e("W/H, nW/nH", bitmap.getWidth() + " " + bitmap.getHeight() + ", " + newWidth + " " + newHeight);
		Bitmap scaled = Bitmap.createScaledBitmap(rotated, (int) newWidth, (int) newHeight, true);

		return scaled;
	}
	/*
	public Bitmap rotate(Bitmap bitmap, double degrees) {
		// precompute some trig functions
		double radians = Math.toRadians(degrees);
		double sin = Math.abs(Math.sin(radians));
		double cos = Math.abs(Math.cos(radians));
		// figure out total width and height of new bitmap
		double newWidth = bitmap.getWidth() * cos + bitmap.getHeight() * sin;
		double newHeight = bitmap.getWidth() * sin + bitmap.getHeight() * cos;
		// set up matrix
		Matrix matrix = new Matrix();
		//matrix.postTranslate((float)(newWidth - bitmap.getWidth()) / 2, (float)(newHeight - bitmap.getHeight()) / 2);
		matrix.setRotate((float) degrees);
		// create new bitmap by rotating mBitmap
		Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, (int)newWidth, (int)newHeight, matrix, true);
		//rotated = Bitmap.createBitmap(bitmap, 0, 0, newWidth, newHeight, matrix, true);

		return rotated;
	}*/

	public void activate() {
		active = true;
	}

	public void deactivate() {
		active = false;
	}

	public synchronized void disposeResources() {
		for (int i = 0; i < FORK_COUNT; i++)
			forkImages[i].setImageBitmap(null);
	}

	public synchronized void tableStateChanged(final TableState newState) {

		if (active && currentState != null) {
			//((Activity) getContext()).runOnUiThread(new Runnable() {
			Activity a = (Activity) getContext();
			for (int i = 0; i < 5; i++) {
				final int newForkOwner = newState.getForkOwner(i);
				final int finalI = i;
				if (currentState.getForkOwner(finalI) != newForkOwner) {
					// determine if fork has been left, or was taken by a philosopher
					if (newForkOwner == -1) {
							/*a.runOnUiThread(new Runnable() {
								@Override
								public void run() {*/
						adjustForkImage(finalI, BASE_ANGLE * finalI);
								/*}
							});*/
					} else if (finalI == newForkOwner) {
							/*a.runOnUiThread(new Runnable() {
								@Override
								public void run() {*/
						adjustForkImage(finalI, BASE_ANGLE * finalI + SHIFT_ANGLE);
								/*}
							});*/
					} else {
							/*a.runOnUiThread(new Runnable() {
								@Override
								public void run() {*/
						adjustForkImage(finalI, BASE_ANGLE * finalI - SHIFT_ANGLE);
								/*}
							});*/
					}
				}
				a.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						createPlate(finalI, BASE_ANGLE * finalI + BASE_ANGLE / 2, newState.getEatingTime(finalI));
					}
				});
			}
		}
		currentState = newState;
	}
}

