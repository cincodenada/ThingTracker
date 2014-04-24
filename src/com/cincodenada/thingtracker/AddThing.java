package com.cincodenada.thingtracker;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
        thingList = new ArrayList<Thing>();
        thingArray = new ThingListAdapter(AddThing.this,R.layout.thing_button,thingList);
        buttonBin.setAdapter(thingArray);
        
        buttonBin.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View item, int pos, long id) {
				Thing curThing = (Thing)list.getItemAtPosition(pos);
				if(curThing.hasChildren()) {
					setCurThing(curThing.id);
				} else {
					getHappening(curThing.id);			
				}
			}
        });
        
        registerForContextMenu(buttonBin);        
        
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
    
    protected void getHappening(long thingId) {
		Intent happeningIntent = new Intent(this, AddHappening.class);
		happeningIntent.putExtra(AddHappening.ARG_THING_ID, thingId);
		startActivity(happeningIntent);
    }

    protected void viewHappenings(long thingId) {
		Intent happeningIntent = new Intent(this, ViewHappenings.class);
		happeningIntent.putExtra(ViewHappenings.ARG_THING_ID, thingId);
		startActivity(happeningIntent);
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
    	default:
        	return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.thing_menu, menu);

        AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
    	ListView thingMenu = (ListView) itemInfo.targetView.getParent();
    	Thing selThing = (Thing) thingMenu.getItemAtPosition(itemInfo.position);
    	
    	if(selThing.hasChildren()) {
    		menu.setGroupVisible(R.id.mnu_group_nochildren, false);
    	} else {
    		menu.setGroupVisible(R.id.mnu_group_haschildren, false);
    	}
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        ListView buttonBin = (ListView) info.targetView.getParent();
        Thing selThing = (Thing) buttonBin.getItemAtPosition(info.position);
        switch (item.getItemId()) {
            case R.id.mnu_thing_add_child:
            	setCurThing(selThing.id);
            	return true;
            case R.id.mnu_thing_add_happening:
            	getHappening(selThing.id);
            	return true;
            case R.id.mnu_thing_happenings:
            	viewHappenings(selThing.id);
            	return true;
            case R.id.mnu_thing_delete:
            default:
                return super.onContextItemSelected(item);
        }
    }    
    
    class ThingListAdapter extends ArrayAdapter<Thing> {
    	private LayoutInflater myInflater;

		public ThingListAdapter(Context context, int resource, ArrayList<Thing> thingList) {
			super(context, resource, thingList);
			myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
    	
		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			View view;
			TextView text;
			ImageView arrow;
			
			view = myInflater.inflate(R.layout.thing_button,parent,false);
			text = (TextView) view.findViewById(R.id.thingText);
			arrow = (ImageView) view.findViewById(R.id.thingArrow);
			
			Thing thing = (Thing)getItem(pos);
			text.setText(thing.getText());
			arrow.setVisibility((thing.hasChildren() ? ImageView.VISIBLE : ImageView.INVISIBLE));
			
			return view;
		}
    }
    
}
