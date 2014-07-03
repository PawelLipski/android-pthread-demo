package pl.edu.agh.informatyka.so.threadsdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class PlateView extends View {

	private Paint paint = new Paint();
	private String text;
	private int width, height;
	
	public PlateView(Context context, String text) {
		super(context);
		this.text = text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		width = getWidth();
		height = getHeight();
		
		int radius;
		if (width > height) {
			radius = height / 2;
		} else {
			radius = width / 2;
		}
		
		canvas.drawColor(Color.TRANSPARENT);

		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);

		canvas.drawCircle(width / 2, height / 2, radius, paint);
		updateText(canvas);	
	}
	
	public void updateText(Canvas canvas) {
		paint.setColor(Color.BLACK);

		//final float fontSize = 32.0f;

		//final float scale = getContext().getResources().getDisplayMetrics().density;
		float textSize = height / 3; //(fontSize * scale + 0.5f);

		paint.setTextSize(textSize);
		float xPos = width / 10, yPos = height / 2 + textSize / 2;    
		canvas.drawText(text, xPos, yPos, paint);
	}

}
