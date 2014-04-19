package com.cincodenada.thingtracker;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import org.apache.commons.lang3.StringUtils;

import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;

public class AddThing extends Activity {

    private ThingsOpenHelper dbHelper;
    private long curThingId = 0;
    private Thing curThing = null;
    private long prevThingId = 0;
    private ArrayList<ThingsOpenHelper.Thing> thingList;
    private ArrayAdapter<ThingsOpenHelper.Thing> thingArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_thing);
        
        Log.v("Fucker","Yep, I'm here");
        
        ListView buttonBin = (ListView) findViewById(R.id.button_bin);
        thingList = new ArrayList<ThingsOpenHelper.Thing>();
        thingArray = new ArrayAdapter<ThingsOpenHelper.Thing>(AddThing.this,android.R.layout.simple_list_item_1,thingList);
        buttonBin.setAdapter(thingArray);
        
        buttonBin.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View item, int pos, long id) {
				Thing curThing = (Thing)list.getItemAtPosition(pos);
				setCurThing(curThing.id);
			}
        });
        
        dbHelper = new ThingsOpenHelper(AddThing.this);
        loadThings();
        
        EditText addNew = (EditText) findViewById(R.id.button_add);
        addNew.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if(actionId == EditorInfo.IME_NULL) {
	                Log.v("Fucker",v.getText().toString());
	                dbHelper.addTextThing(v.getText().toString(),curThingId);
	                loadThings();
	
	                if(event != null) {
	                	Log.v("Fucker",event.toString());
	                }
	                Log.v("Fucker",Integer.toString(actionId));
                }
                return handled;
            }
        });
    }

    protected void loadThings() {
        thingArray.clear();
        for(ThingsOpenHelper.Thing curThing: dbHelper.getSubthings(curThingId)) {
            //Log.d("Fucker",curThing.toString());
            thingArray.add(curThing);
        }
    	curThing = dbHelper.getThing(curThingId);
        //Log.d("Fucker",Integer.toString(thingArray.getCount()));
    }
    
    protected void setCurThing(long newid) {
    	prevThingId = curThingId;
    	curThingId = newid;
    	getActionBar().setDisplayHomeAsUpEnabled((newid != 0));
		loadThings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_thing, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case android.R.id.home:
    		setCurThing(curThing.parent_id);
    		return true;
    	}
    	return false;
    }
    
}
