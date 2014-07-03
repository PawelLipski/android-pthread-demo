package pl.edu.agh.informatyka.so.threadsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class ProducersConsumersActivity extends Activity {

    private ProducersConsumersView view;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new ProducersConsumersView(this);
        setContentView(view);
    }

    @Override
    public void onResume() {
        super.onPause();

        postStartFactory();
    }

    @Override
    public void onPause() {
        super.onPause();

        //view.deactivate();
        stopFactory();
    }


    private void restartFactory() {
        stopFactory();
        startFactory(ProducersConsumersView.PRODUCERS_CNT, ProducersConsumersView.CONSUMERS_CNT, ProducersConsumersView.PRODUCTS_CNT);
    }

    private void postStartFactory() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startFactory(ProducersConsumersView.PRODUCERS_CNT, ProducersConsumersView.CONSUMERS_CNT, ProducersConsumersView.PRODUCTS_CNT);
            }
        }, 2000);
    }

    private void postRestartFactory() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                restartFactory();
            }
        });
    }


    private native void startFactory(int producersCnt, int consumersCnt, int productsCnt);
    private native void stopFactory();

    public native void notifyProductIssued(int productNo);
    public native void notifyProductCollected(int productNo);

    private void productIssued(int producerNo, int productNo) {
        view.animateProductIssued(producerNo, productNo);
    }

    private void productCollected(int consumerNo, int productNo) {
        view.animateProductCollected(consumerNo, productNo);
    }

    static {
		System.loadLibrary("threads-demo");
	}

}
