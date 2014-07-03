package pl.edu.agh.informatyka.so.threadsdemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;


public class ReadersWritersView extends View {


	private Boolean active = true;
	private BookshopState currentState;

	private Drawable readerImage;
	//private Drawable writerFemaleImage;
	private Drawable writerMaleImage;
	private Drawable bookImage;
	//private Random imageChooser;
	private Paint paint;
	
	private int width, height;
	private int minDim;
	private int xMiddle, yMiddle;
	
	private int personSize;
	private int defaultPersonPadding;
	private int bookSize;	
	
	private int actualWriterSize;
	private int writerShift;
	private int writerPadding;
	
	private int actualReaderSize;
	private int readerShift;
	private int readerPadding;

	
    public ReadersWritersView(Context context) {
        super(context);
        initImages();
    }
    
    public ReadersWritersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initImages();        
    }
    
	public ReadersWritersView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initImages();
    }
	
	private void initImages() {
		Resources r = getContext().getResources();
		readerImage = r.getDrawable(R.drawable.reader);
		//writerFemaleImage = r.getDrawable(R.drawable.writer_female);
		writerMaleImage = r.getDrawable(R.drawable.writer_male);	
		bookImage = r.getDrawable(R.drawable.book);
		
		paint = new Paint();
	}


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);     
        if (!changed) return;              	    
        
		setupDimensions();
		currentState = new BookshopState();		
	}
    
    @Override
    protected void onDraw(Canvas canvas) {
    	super.onDraw(canvas);   
    	
    	putBook(canvas);
    	    	
    	putWriters(canvas);
    	
    	putReaders(canvas);    	        	

    }

	private void putReaders(Canvas canvas) {
		
    	int rc = currentState.getReaderCount();
    	readerShift = getShift(rc);
    	actualReaderSize = personSize;    	
    	
    	if (readerShift < 0) {
    		readerShift = 0;
    		
    		// old version (fixed padding): actualReaderSize = (height - (rc - 1) * personPadding) / rc;
    		
    		// new version (dynamic padding):
    		// readerPadding * (rc - 1) + actualReaderSize * rc == height
    		// readerPadding == actualReaderSize / 4
    		// actualReaderSize * (rc + (rc - 1) / 4.0) == height
    		// actualReaderSize = (int)(height / (rc + (rc - 1) / 4.0)) 
    		
    		actualReaderSize = (int)(height / (rc + (rc - 1) / 4.0));
    	}    	
    	
    	readerPadding = actualReaderSize / 4; // works for both cases    	
    	int rdist = actualReaderSize + readerPadding;
    	
    	for (int i = 0; i < rc; i++) {
    		
    		readerImage.setBounds(
    				width - rdist, readerShift + i * rdist, 
    				width - readerPadding, readerShift + i * rdist + actualReaderSize);
    		
    		readerImage.draw(canvas);
    		
    		if (currentState.isReading(i))
    			connectReader(canvas, i);    		
    	}
	}

	private void putWriters(Canvas canvas) {
		int wc = currentState.getWriterCount();
    	writerShift = getShift(wc);
    	actualWriterSize = personSize;
    	
    	if (writerShift < 0) {
    		writerShift = 0;
    		//actualWriterSize = (height - (wc - 1) * personPadding) / wc;
    		actualWriterSize = (int)(height / (wc + (wc - 1) / 4.0));
    	}
    	
    	writerPadding = actualWriterSize / 4;    	
    	int wdist = actualWriterSize + writerPadding;
    	
    	for (int i = 0; i < wc; i++) {
    		
    		writerMaleImage.setBounds(
    				writerPadding, writerShift + i * wdist, 
    				wdist, writerShift + i * wdist + actualWriterSize);
    		
    		writerMaleImage.draw(canvas);
    		
    		if (currentState.anybodyWriting() && currentState.getCurrentWriter() == i) 
    			connectWriter(canvas, i);
    	}
	}

	private void putBook(Canvas canvas) {
		int h = bookSize / 2;
    	bookImage.setBounds(
				xMiddle - h, yMiddle - h, 
				xMiddle + h, yMiddle + h);
    	
    	bookImage.draw(canvas);
	}
	
	private int getShift(int c) {	
		int totalReadersSize = personSize * c + defaultPersonPadding * (c - 1);
		return (width - totalReadersSize) / 2; 
	}

	private void connectReader(Canvas canvas, int no) {
		
		configPaint();

        /*int dist = personSize + personPadding;
        int rs = getShift(currentState.getReaderCount());
       
        int rx = width - dist;
        int ry = rs + no * dist + personSize / 2;*/
		
		int dist = actualReaderSize + readerPadding;
		
		int rx = width - dist;
		int ry = readerShift + no * dist + actualReaderSize / 2;
        int bx = xMiddle + bookSize / 2;
        int by = yMiddle;
                
        drawDottedLine(canvas, bx, by, rx, ry);
	}
	
	private void connectWriter(Canvas canvas, int no) {
		
		configPaint();

        int dist = actualWriterSize + writerPadding; 
       
        int wx = dist;
        int wy = writerShift + no * dist + actualWriterSize / 2;
        int bx = xMiddle - bookSize / 2;
        int by = yMiddle;
        
        drawDottedLine(canvas, bx, by, wx, wy);		
	}

	private void configPaint() {
		paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        //paint.setStrokeCap(Paint.Cap.ROUND);
        //paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));        
	}

	private void drawDottedLine(Canvas canvas, int x1, int y1, int x2, int y2) {
		
		double len = Math.hypot(x2-x1, y2-y1);
		float p = (float)(len / (personSize / 5));  
		float dx = (x2 - x1) / p, dy = (y2 - y1) / p;
		paint.setStyle(Paint.Style.FILL);
	
		for (int i = 0; i <= p; i++) {
			canvas.drawCircle(x1 + dx * i, y1 + dy * i, 3, paint);
		}		
	}
	
	private void setupDimensions() {
		
        width = getWidth();
        height = getHeight();
        minDim = Math.min(width, height);
        
		xMiddle = width / 2; 
		yMiddle = height / 2;
		
		personSize = minDim / 6;
		defaultPersonPadding = personSize / 4;
		bookSize = minDim / 3;		
	}

	public void bookshopStateChanged(final BookshopState newState) {

		synchronized (active) {
			if (active && currentState != null) {
				currentState = newState;
				postInvalidate();
			}			
		}
	}
	
}
