package pl.edu.agh.informatyka.so.threadsdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressLint("NewApi")
public class SleepingBarberActivity extends Activity {

	private int chairCnt = 4;
	private int customerCnt = 4;
	private boolean ready = true;
	private String[] customerNames;

	private GridView grid;
	private BarberWaitingRoomAdapter adapter;

	private TextView tvChairs, tvCustomers;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (icicle != null) {
			chairCnt = icicle.getInt("CHAIRS", 4);
			customerCnt = icicle.getInt("CUSTOMERS", 4);
		}

		setContentView(R.layout.sleeping_barber);

		grid = (GridView) findViewById(R.id.gridview);
		fetchCustomerNames();
		adapter = new BarberWaitingRoomAdapter(this, chairCnt, customerCnt, customerNames);
		grid.setAdapter(adapter);

		tvChairs = (TextView) findViewById(R.id.chairs_number);
		tvChairs.setText("" + chairCnt);

		tvCustomers = (TextView) findViewById(R.id.number);
		tvCustomers.setText("" + customerCnt);
	}

	@Override
	public void onSaveInstanceState(Bundle icicle) {
		icicle.putInt("CHAIRS", chairCnt);
		icicle.putInt("CUSTOMERS", customerCnt);
	}

	@Override
	public void onResume() {
		super.onPause();

		startBarbershop(chairCnt, customerCnt);
	}

	@Override
	public void onPause() {
		super.onPause();

		//view.deactivate();
		stopBarbershop();
	}

	public void onStop() {
		super.onStop();
		//view.disposeResources();
	}

	public void setChairNumber(View view) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.barber_dialog, (ViewGroup) findViewById(R.id.root));
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(layout);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

		final TextView tvNumber = (TextView) layout.findViewById(R.id.number);
		tvNumber.setText("" + chairCnt);

		TextView tvHeader = (TextView) layout.findViewById(R.id.header);
		tvHeader.setText("Number of chairs");

		SeekBar sb = (SeekBar) layout.findViewById(R.id.seek_bar);
		sb.setMax(7);
		sb.setProgress(chairCnt - 1);

		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
				ready = false;
				chairCnt = 1 + progress;

				tvNumber.setText("" + chairCnt);
				tvChairs.setText("" + chairCnt);

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (chairCnt == 1 + progress)
							postRestartBarbershop();
					}
				}, 2000);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	public void setCustomerNumber(View view) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.barber_dialog, (ViewGroup) findViewById(R.id.root));
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(layout);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();

		final TextView tvNumber = (TextView) layout.findViewById(R.id.number);
		tvNumber.setText("" + customerCnt);

		TextView tvHeader = (TextView) layout.findViewById(R.id.header);
		tvHeader.setText("Number of customers");

		SeekBar sb = (SeekBar) layout.findViewById(R.id.seek_bar);
		sb.setMax(11);
		sb.setProgress(customerCnt - 1);

		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
				ready = false;
				customerCnt = 1 + progress;

				tvNumber.setText("" + customerCnt);
				tvCustomers.setText("" + customerCnt);

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						if (customerCnt == 1 + progress)
							postRestartBarbershop();
					}
				}, 2000);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void fetchCustomerNames() {
		customerNames = new String[20];
		AssetManager am = getAssets();
		InputStream is;
		try {
			is = am.open("customer_names.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			for (int i = 0; i < customerNames.length; i++)
				customerNames[i] = br.readLine();
		} catch (IOException e) {
			Log.e("BarberWaitingRoomAdapter", "opening customer_names asset", e);
		}
	}

	public void barbershopStateChanged(final BarbershopState newState) {
		if (!ready) return;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.update(newState);
				grid.invalidateViews();
				if (newState.anybodyGettingAHaircut())
					setClientGettingHaircutImage(newState.getCurrentCustomer());
				else
					setEmptySofaImage();
			}

		});
	}

	private void setEmptySofaImage() {

		ImageView iv = new ImageView(this);

		iv.setImageResource(R.drawable.sofa);
		iv.setPadding(8, 8, 8, 8);

		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		iv.setLayoutParams(p);

		LinearLayout l = (LinearLayout) findViewById(R.id.topLayout);
		l.removeViewAt(1);

		l.addView(iv);
	}


	private void setClientGettingHaircutImage(int customerNo) {


		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View customerView = inflater.inflate(R.layout.tagged_customer, null);

		TextView textView = (TextView) customerView.findViewById(R.id.customerName);
		textView.setText(customerNames[customerNo]);
		//textView.setTextSize(28);

		//LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//customerView.setLayoutParams(p);
		customerView.setPadding(8, 8, 8, 8);

		LinearLayout l = (LinearLayout) findViewById(R.id.topLayout);
		l.removeViewAt(1);

		l.addView(customerView);
	}


	private void restartBarbershop() {
		stopBarbershop();
		setEmptySofaImage();
		adapter = new BarberWaitingRoomAdapter(this, chairCnt, customerCnt, customerNames);
		ready = true;
		grid.setAdapter(adapter);
		//Log.e("Adapter created", )
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				grid.invalidateViews();
			}
		});
		startBarbershop(chairCnt, customerCnt);
	}

	private native void startBarbershop(int chairCnt, int customerCnt);

	private native void stopBarbershop();

	synchronized private void postRestartBarbershop() {
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				restartBarbershop();
			}
		});
	}

	static {
		System.loadLibrary("threads-demo");
	}
}
