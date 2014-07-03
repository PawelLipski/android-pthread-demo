package pl.edu.agh.informatyka.so.threadsdemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class BarberWaitingRoomAdapter extends BaseAdapter {
	
    private Context context;
    
    private BarbershopState currentState; 
    private int chairCnt;
    private int customerCnt;
    private int whoOccupiesChair[];
    private int chairOccupiedBy[]; 
    private String[] customerNames;
    
    //private ImageView emptyImage;
    private View customerImages[];

    public BarberWaitingRoomAdapter(Context c, int chairCnt, int customerCnt, String[] customerNames) {
        context = c;
        currentState = new BarbershopState(chairCnt, customerCnt);
        this.chairCnt = chairCnt;
        this.customerCnt = customerCnt;
        whoOccupiesChair = new int[chairCnt];
        for (int i = 0; i < chairCnt; i++)
        	whoOccupiesChair[i] = -1;
        chairOccupiedBy = new int[customerCnt];        
        this.customerNames = customerNames;        
        
        this.customerImages = new View[customerCnt];
    }

	public int getCount() {
        return chairCnt;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
    	Log.e("BarberWaitingRoomAdapter", "getView");
    	int c = whoOccupiesChair[position]; 
        if (c != -1) {
        	return getCustomerImage(c);
    	} else {
    		return getEmptyImage();        
    	}    
    }
    
    private View getCustomerImage(int customerNo) {
    	
    	if (customerImages[customerNo] == null) {
	    	LayoutInflater inflater = (LayoutInflater) context
	    			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	     
	    	View customerView = inflater.inflate(R.layout.tagged_customer, null);
	         
	    	TextView textView = (TextView) customerView.findViewById(R.id.customerName);
	    	textView.setText(customerNames[customerNo]);
	    			    			
	    	//customerView.setLayoutParams(new GridView.LayoutParams(180, 180));
			customerView.setLayoutParams(new GridView.LayoutParams(120, 120));
	        //customerView.setScaleType(ImageView.ScaleType.FIT_CENTER);
	        customerView.setPadding(4, 4, 4, 4);
	        customerImages[customerNo] = customerView; 
    	}
        
        return customerImages[customerNo];
	}

	private View getEmptyImage() {
		//if (emptyImage == null) {
    	ImageView emptyImage = new ImageView(context);
    	//emptyImage.setLayoutParams(new GridView.LayoutParams(180, 180));
        //emptyImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		//GridView.LayoutParams p = new GridView.LayoutParams(
		//		ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		//emptyImage.setLayoutParams(p);
		emptyImage.setLayoutParams(new GridView.LayoutParams(120, 120));

        emptyImage.setPadding(20, 20, 20, 20);
        emptyImage.setImageResource(R.drawable.chair);
		//}
        return emptyImage;
	}

	synchronized public void update(BarbershopState newState) {
    	for (int i = 0; i < customerCnt; i++)
    		if (currentState.isSitting(i) && !newState.isSitting(i)) {
    			int pos = chairOccupiedBy[i];
    			if (pos >= 0)
    				whoOccupiesChair[pos] = -1;
    			chairOccupiedBy[i] = -1; 
    		}
    	for (int i = 0; i < customerCnt; i++)
    		if (!currentState.isSitting(i) && newState.isSitting(i)) {
    			int pos = getUnoccupiedChair();
    			if (pos >= 0)
    				whoOccupiesChair[pos] = i;    			    				
    			chairOccupiedBy[i] = pos;
    		}    	
    	currentState = newState;
    	
    }


	private int getUnoccupiedChair() {
		for (int i = 0; i < chairCnt; i++) {
			if (whoOccupiesChair[i] == -1)
				return i;
		}		
		return -1;
	}
}
