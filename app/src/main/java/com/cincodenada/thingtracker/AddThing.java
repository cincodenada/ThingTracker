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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

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
                if(actionId == EditorInfo.IME_NULL && v.getText() != null) {
                    Log.v("Fucker",v.getText().toString());
                    Log.v("Fucker","Adding text thing");
                    Log.v("Fucker",Integer.toString(actionId));
                    dbHelper.addTextThing(v.getText().toString(),curThingId);
                    loadThings();
                    
                    //Clear the text, clear focus, hide keyboard
                    v.clearFocus();
                    v.setText("");
                    InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    
                    handled = true;
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
    
    protected void deleteThing(long thingId) {
    	dbHelper.deleteThing(thingId);
    	loadThings();
    }
    
    protected void getHappening(long thingId) {
		Intent happeningIntent = new Intent(this, EditHappening.class);
		happeningIntent.putExtra(EditHappening.ARG_THING_ID, thingId);
		startActivity(happeningIntent);
    }

    protected void viewHappenings(long thingId, boolean allThings) {
		Intent happeningIntent = new Intent(this, ViewHappenings.class);
		happeningIntent.putExtra(ViewHappenings.ARG_THING_ID, thingId);
		happeningIntent.putExtra(ViewHappenings.ARG_ALL_SUBTHINGS, allThings);
		startActivity(happeningIntent);
    }

    protected void editThing(long thingId) {
		Intent editThingIntent = new Intent(this, EditThing.class);
		editThingIntent.putExtra(EditThing.ARG_THING_ID, thingId);
		startActivity(editThingIntent);
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
            	viewHappenings(selThing.id, false);
            	return true;
            case R.id.mnu_thing_all_happenings:
            	viewHappenings(selThing.id, true);
            	return true;
            case R.id.mnu_thing_delete:
            	deleteThing(selThing.id);
            	return true;
            case R.id.mnu_thing_edit_thing:
            	editThing(selThing.id);
            	return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    
    @Override
    public void onBackPressed() {
        if(curThing != null && curThing.id > 0) {
            setCurThing(curThing.parent_id);
        } else {
            super.onBackPressed();
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
