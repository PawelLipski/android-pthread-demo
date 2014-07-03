package pl.edu.agh.informatyka.so.threadsdemo;

import android.content.Context;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class ProducersConsumersView extends RelativeLayout implements Animation.AnimationListener {

    public static final int PRODUCERS_CNT = 3;
    public static final int CONSUMERS_CNT = 4;
    public static final int PRODUCTS_CNT = 5;

    private ProducersConsumersActivity activity;
    private ImageView productImages[];
    private ImageView producerImages[];
    private ImageView consumerImages[];

    private int width;
    private int height;
    private int xMiddle;

    private int imageSize;
    private final int imagePadding = 20;
    private int producersShift;
    private int consumersShift;

    private Animation productAnims[];
    private boolean beingIssued[];


    public ProducersConsumersView(Context context) {
        super(context);
        activity = (ProducersConsumersActivity)context;
        beingIssued = new boolean[PRODUCTS_CNT];
        productAnims = new Animation[PRODUCTS_CNT];
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!changed) return;

        setupDimensions();

        productImages = new ImageView[PRODUCTS_CNT];
        for (int i = 0; i < PRODUCTS_CNT; i++)
            putProductImage(i);

        producerImages = new ImageView[PRODUCERS_CNT];
        for (int i = 0; i < PRODUCERS_CNT; i++)
            putProducerImage(i);

        consumerImages = new ImageView[CONSUMERS_CNT];
        for (int i = 0; i < CONSUMERS_CNT; i++)
            putConsumerImage(i);
    }

    private void setupDimensions() {
        width = getWidth();
        height = getHeight();
        xMiddle = width / 2;
        imageSize = (height - imagePadding * (PRODUCTS_CNT - 1)) / PRODUCTS_CNT;
        producersShift = (height - (imageSize * PRODUCERS_CNT + imagePadding * (PRODUCERS_CNT - 1))) / 2;
        consumersShift = (height - (imageSize * CONSUMERS_CNT + imagePadding * (PRODUCERS_CNT - 1))) / 2;
    }

    private void putProducerImage(int no) {
        producerImages[no] = new ImageView(getContext());
        producerImages[no].setImageResource(R.drawable.producer);

        LayoutParams params = new LayoutParams(imageSize, imageSize);
        params.leftMargin = getProducerX();
        params.topMargin = getProducerY(no);
        producerImages[no].setLayoutParams(params);

        addView(producerImages[no]);
    }

	private int getProducerX() {
		return 0;
	}

	private int getProducerY(int no) {
		return producersShift + (imageSize + imagePadding) * no;
	}

    private void putConsumerImage(int no) {
        consumerImages[no] = new ImageView(getContext());
        consumerImages[no].setImageResource(R.drawable.consumer);

        LayoutParams params = new LayoutParams(imageSize, imageSize);
        params.leftMargin = getConsumerX();
        params.topMargin = getConsumerY(no);
        consumerImages[no].setLayoutParams(params);

        addView(consumerImages[no]);
    }

	private int getConsumerX() {
		return width - imageSize;
	}

	private int getConsumerY(int no) {
		return consumersShift + (imageSize + imagePadding) * no;
	}

    private void putProductImage(int no) {
        productImages[no] = new ImageView(getContext());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(imageSize, imageSize);
        params.leftMargin = getProductX();
        params.topMargin = getProductY(no);
        productImages[no].setLayoutParams(params);

        addView(productImages[no]);
    }

	private int getProductX() {
		return xMiddle - imageSize / 2;
	}

	private int getProductY(int no) {
		return (imageSize + imagePadding) * no;
	}

    public void animateProductIssued(int producerNo, final int productNo) {

        productAnims[productNo] = AnimationUtils.loadAnimation(
                getContext(), R.anim.product_issued_anim);

		final AnimationSet set = (AnimationSet) productAnims[productNo];
		int a = Animation.ABSOLUTE;
		TranslateAnimation translation = new TranslateAnimation(
				a, getProducerX() + imageSize / 3 - getProductX(), a, 0,
				a, getProducerY(producerNo) - getProductY(productNo) + imageSize / 3, a, 0);
		translation.setDuration(5000);
		set.addAnimation(translation);
		set.setAnimationListener(this);

		beingIssued[productNo] = true;

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
				ImageView productImage = productImages[productNo];
				productImage.setImageResource(R.drawable.product);
                productImage.startAnimation(set);
            }
        });
    }

    public void animateProductCollected(int consumerNo, int productNo) {

        productAnims[productNo] = AnimationUtils.loadAnimation(
                getContext(), R.anim.product_collected_anim);

		final ImageView productImage = productImages[productNo];

		final AnimationSet set = (AnimationSet) productAnims[productNo];
		int a = Animation.ABSOLUTE;
		TranslateAnimation translation = new TranslateAnimation(
				a, 0, a, getConsumerX() - getProductX() - imageSize / 2,
				a, 0, a, getConsumerY(consumerNo) - getProductY(productNo) + imageSize / 3);
		translation.setDuration(5000);
		set.addAnimation(translation);
		set.setAnimationListener(this);

		activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                productImage.startAnimation(set);
            }
        });
    }

    @Override
    public void onAnimationEnd(Animation animation) {

        int productNo = getAnimationIndex(animation);

        if (beingIssued[productNo]) {
            Log.e("animEnd", "issue anim ended for " + productNo);
            beingIssued[productNo] = false;
            activity.notifyProductIssued(productNo);

        } else {
            Log.e("animEnd", "collect anim ended for " + productNo);
            makeProductTransparent(productNo);
            activity.notifyProductCollected(productNo);
        }
    }

    private void makeProductTransparent(final int productNo) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productImages[productNo].setImageResource(android.R.color.transparent);
            }
        });
    }

    private int getAnimationIndex(Animation animation) {
        for (int i = 0; i < PRODUCTS_CNT; i++)
            if (productAnims[i] == animation)
                return i;
        return -1;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

}
