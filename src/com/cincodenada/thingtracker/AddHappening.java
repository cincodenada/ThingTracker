package com.cincodenada.thingtracker;

import java.util.Iterator;

import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;

import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;

public class AddHappening extends Activity {
	
	public static final String ARG_THING_ID = "thing_id";
	
    private ThingsOpenHelper dbHelper;
	private Thing targetThing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_happening);
		
		dbHelper = new ThingsOpenHelper(AddHappening.this);
		
		Long thing_id = getIntent().getLongExtra(ARG_THING_ID, 0);
		targetThing = dbHelper.getThing(thing_id);
		
		Iterator<String> keyIter = targetThing.metadef.keys();
		LinearLayout fieldBucket = (LinearLayout) findViewById(R.id.happening_fields);
		String curKey, curVal;
		TextView curLabel;
		while(keyIter.hasNext()) {
			curKey = keyIter.next();
			try {
				curVal = targetThing.metadef.getString(curKey);
			} catch (JSONException e) {
				continue;
			}
			curLabel = new TextView(AddHappening.this);
			curLabel.setText(curKey);
			fieldBucket.addView(curLabel);
			
			EditText curField = new EditText(AddHappening.this);
			fieldBucket.addView(curField);
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_happening, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
