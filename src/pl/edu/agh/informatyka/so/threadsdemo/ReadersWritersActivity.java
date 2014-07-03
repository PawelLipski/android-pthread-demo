package pl.edu.agh.informatyka.so.threadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ReadersWritersActivity extends Activity {
	
	//private ReadersWritersView view;
	
	private SeekBar seekBarReaders;
	private SeekBar seekBarWriters;
	
	private int readersCnt = 3;
	private int writersCnt = 4;
	
	private ReadersWritersView view;

	
	public ReadersWritersActivity() {
		super();
		//Log.e("RWA", "<init>");
	}

    @Override
    public void onSaveInstanceState(Bundle sis) {
        sis.putInt("readersCnt", readersCnt);
        sis.putInt("writersCnt", writersCnt);
    }

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);

		//Log.e("RWA", "onCreate");

		setContentView(R.layout.readers_writers);
		view = (ReadersWritersView)findViewById(R.id.readersWritersView1);
		
		seekBarReaders = (SeekBar) findViewById(R.id.seekBarReaders);

        if (sis != null) {
            readersCnt = sis.getInt("readersCnt");
            seekBarReaders.setProgress(readersCnt - 1);
        }

		seekBarReaders.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		    
			@Override
		    public void onProgressChanged(SeekBar seekBar, int progress,
		            boolean fromUser) {}		 
		    @Override
		    public void onStartTrackingTouch(SeekBar seekBar) {}
		    
		    @Override
		    public void onStopTrackingTouch(SeekBar seekBar) {
		    	readersCnt = 1 + seekBar.getProgress();		    	
		    	postRestartBookshop();
		    }
		});


		seekBarWriters = (SeekBar) findViewById(R.id.seekBarWriters);

        if (sis != null) {
            writersCnt = sis.getInt("writersCnt");
            seekBarWriters.setProgress(writersCnt - 1);
        }
		
		seekBarWriters.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
		    
			@Override
		    public void onProgressChanged(SeekBar seekBar, int progress,
		            boolean fromUser) {}		 
		    @Override
		    public void onStartTrackingTouch(SeekBar seekBar) {}
		    
		    @Override
		    public void onStopTrackingTouch(SeekBar seekBar) {		    	
		    	writersCnt = 1 + seekBar.getProgress();		    	
		    	postRestartBookshop();
		    }
		});
		
		Log.e("RWA", "finished onCreate");
	}
	
	
	@Override
	public void onResume() {
		super.onResume();

		Log.e("RWA", "onResume");
		
		readersCnt = 1 + seekBarReaders.getProgress();
		writersCnt = 1 + seekBarWriters.getProgress();
		
		startBookshop(readersCnt, writersCnt);
		
		Log.e("RWA", "finished onResume");
	}

	@Override
	public void onPause() {
		Log.e("RWA", "onPause");
		
		super.onPause();				

		stopBookshop();
		
		Log.e("RWA", "finished onPause");
	}

	public void onStop() {
		Log.e("RWA", "onStop");
		
		super.onStop();
		
		Log.e("RWA", "finished onStop");
	}
	
	public void bookshopStateChanged(final BookshopState newState) {
		view.bookshopStateChanged(newState);
		
		/*runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView text = (TextView) findViewById(R.id.textReadersCnt);
				if (newState.anybodyWriting())
					text.setText(newState.getCurrentWriter() + " is writing");
				else {
					StringBuilder s = new StringBuilder("Active readers: ");
					for (int r: newState.getActiveReaders()) {
						s.append(r).append(", ");
					}
					text.setText(s);
				}				
			}
		});*/
	}

	private void restartBookshop() {
		stopBookshop();
		startBookshop(readersCnt, writersCnt);
	}

	private native void startBookshop(int readersCnt, int writersCnt);
	
	private native void stopBookshop();

	private void postRestartBookshop() {
		Handler handler = new Handler();
		handler.post(new Runnable() {					
			@Override
			public void run() {
				restartBookshop();						
			}
		});
	}

	static {
		System.loadLibrary("threads-demo");
	}
}
