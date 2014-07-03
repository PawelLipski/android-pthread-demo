package pl.edu.agh.informatyka.so.threadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PhilosophersActivity extends Activity {

	private PhilosophersView view;
	private boolean tableStarted = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("PhilosophersActivity", "onCreate");

		setContentView(R.layout.philosophers);
		view = (PhilosophersView) findViewById(R.id.philosophers_view);
	}

	@Override
	public void onResume() {
		super.onPause();

		Log.e("PhilosophersActivity", "onResume");
		view.activate();
		if (!tableStarted) {
			tableStarted = true;
			startTable();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.e("PhilosophersActivity", "onPause");
		view.deactivate();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e("PhilosophersActivity", "onStop");

		tableStarted = false;
		stopTable();

		view.disposeResources();
	}

	public void tableStateChanged(TableState newState) {
		view.tableStateChanged(newState);
	}

	private native void startTable();

	private native void stopTable();

	static {
		System.loadLibrary("threads-demo");
	}
}
