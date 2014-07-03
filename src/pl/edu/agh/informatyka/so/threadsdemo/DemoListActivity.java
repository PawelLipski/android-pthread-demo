package pl.edu.agh.informatyka.so.threadsdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DemoListActivity extends Activity implements OnItemClickListener { 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_list);

		ListView demoList = (ListView) findViewById(R.id.demoList);		
		
		String demoNames[] = { 			
				"Sleeping Barber", "5 Philosophers", "Readers & Writers", "Producers & Consumers" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, 
				demoNames);
		demoList.setAdapter(adapter);

		demoList.setOnItemClickListener(this);
	}
		
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long rowid) {
		Intent detailIntent;
		String id = ((TextView)view).getText().toString();
		if (id.startsWith("S"))
			detailIntent = new Intent(this, SleepingBarberActivity.class);
		else if (id.startsWith("5"))
			detailIntent = new Intent(this, PhilosophersActivity.class);
		else if (id.startsWith("R"))
			detailIntent = new Intent(this, ReadersWritersActivity.class);
		else if (id.startsWith("P"))
			detailIntent = new Intent(this, ProducersConsumersActivity.class);
		else
			return;
		
		startActivity(detailIntent);
	}
}
