package com.cincodenada.thingtracker;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;

import com.cincodenada.thingtracker.AddThing.ThingListAdapter;
import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.os.Build;

public class ViewHappenings extends ActionBarActivity {
	
	public static final String ARG_THING_ID = "thing_id";
	public static final String ARG_ALL_SUBTHINGS = "all_subthings";

    private static final int EDIT_HAPP = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_happenings);
		
		if (savedInstanceState == null) {
			ViewHappeningsFragment f = new ViewHappeningsFragment();
			Bundle args = new Bundle();
			Long thing_id = getIntent().getLongExtra(ARG_THING_ID, 0);
			args.putLong(ARG_THING_ID, thing_id);
			boolean all_subthings = getIntent().getBooleanExtra(ARG_ALL_SUBTHINGS, false);
			args.putBoolean(ARG_ALL_SUBTHINGS, all_subthings);
			f.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, f).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_happenings, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ViewHappeningsFragment extends Fragment {

	    private ThingsOpenHelper dbHelper;
		private Thing targetThing;
		private ArrayList<Happening> theHaps;
		private ArrayAdapter<Happening> hapsArray;
		private boolean curAllThings;

		public ViewHappeningsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_happenings,
					container, false);
			
			Bundle args = getArguments();
			
			dbHelper = new ThingsOpenHelper(getActivity());

			theHaps = new ArrayList<Happening>();
			hapsArray = dbHelper.new HappeningListAdapter(getActivity(), theHaps, dbHelper);
	        ListView hapsBin = (ListView) rootView.findViewById(R.id.happening_list);
	        hapsBin.setAdapter(hapsArray);
	        
	        registerForContextMenu(hapsBin);        
	        
	        hapsBin.setOnItemClickListener(showDetails);

	        getTheHaps(args.getLong(ARG_THING_ID), args.getBoolean(ARG_ALL_SUBTHINGS));
	        
			return rootView;
		}
		
		public void getTheHaps(Long thing_id, boolean getAll) {
			hapsArray.clear();
			for(Happening curHap: dbHelper.getHappenings(thing_id, getAll)) {
	            Log.d("Fucker",curHap.toString());
				hapsArray.add(curHap);
			}
			targetThing = dbHelper.getThing(thing_id);
			curAllThings = getAll;
		}
		
	    @Override
	    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    	super.onCreateContextMenu(menu, v, menuInfo);
	    	
	        MenuInflater inflater = getActivity().getMenuInflater();
	        inflater.inflate(R.menu.view_happenings, menu);
	    }
		
	    @Override
	    public boolean onContextItemSelected(MenuItem item) {
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        ListView buttonBin = (ListView) info.targetView.getParent();
	        Happening selThing = (Happening) buttonBin.getItemAtPosition(info.position);
	        switch (item.getItemId()) {
	            case R.id.mnu_happenings_delete:
	            	dbHelper.deleteHappening(selThing.id);
	            	getTheHaps(targetThing.id, curAllThings);
                case R.id.mnu_happenings_edit:
                    Intent happeningIntent = new Intent(getActivity(), EditHappening.class);
                    happeningIntent.putExtra(EditHappening.ARG_HAPP_ID, selThing.id);
                    startActivityForResult(happeningIntent, EDIT_HAPP);
	            default:
	                return super.onContextItemSelected(item);
	        }
	    }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(requestCode == EDIT_HAPP) {
                getTheHaps(targetThing.id, curAllThings);
            }
        }

		
		protected OnItemClickListener showDetails = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				// TODO Auto-generated method stub
				Happening clickedHappening = (Happening)av.getItemAtPosition(pos);
				Thing clickedThing = (Thing)dbHelper.getThing(clickedHappening.thing_id);
				
				Iterator<String> keyIter = clickedHappening.metadata.keys();
				String curKey;
				String summary = "";
				while(keyIter.hasNext()) {
					curKey = keyIter.next();
					try {
						summary = summary.concat(curKey + ": " + clickedHappening.metadata.getString(curKey) + "\n");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				summary = summary.trim();
				
				AlertDialog.Builder showSummary = new AlertDialog.Builder(getActivity());
				showSummary.setTitle(clickedThing.data + " Happening");
				showSummary.setMessage(summary);
				Dialog summaryDialog = showSummary.create();
				summaryDialog.show();
			}
		};
	}

}
