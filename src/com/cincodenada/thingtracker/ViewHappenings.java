package com.cincodenada.thingtracker;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;

import com.cincodenada.thingtracker.AddThing.ThingListAdapter;
import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;
import com.cincodenada.thingtracker.ThingsOpenHelper.Happening;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.os.Build;

public class ViewHappenings extends ActionBarActivity {
	
	public static final String ARG_THING_ID = "thing_id";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_happenings);
		
		if (savedInstanceState == null) {
			ViewHappeningsFragment f = new ViewHappeningsFragment();
			Bundle args = new Bundle();
			Long thing_id = getIntent().getLongExtra(ARG_THING_ID, 0);
			args.putLong("thing_id", thing_id);
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

		public ViewHappeningsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_view_happenings,
					container, false);
			
			Bundle args = getArguments();
			
			dbHelper = new ThingsOpenHelper(getActivity());

			targetThing = dbHelper.getThing(args.getLong("thing_id"));
			
			ArrayList<Happening> theHaps = dbHelper.getHappenings(targetThing.id);
	        ArrayAdapter<Happening> hapsArray = new ArrayAdapter<Happening>(getActivity(),android.R.layout.simple_list_item_1,theHaps);
	        ListView hapsBin = (ListView) rootView.findViewById(R.id.happening_list);
	        hapsBin.setAdapter(hapsArray);
	        
	        hapsBin.setOnItemClickListener(showDetails);

			return rootView;
		}
		
		protected OnItemClickListener showDetails = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
				// TODO Auto-generated method stub
				Happening clickedHappening = (Happening)av.getItemAtPosition(pos);
				
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
				showSummary.setTitle("Happening Summary");
				showSummary.setMessage(summary);
				Dialog summaryDialog = showSummary.create();
				summaryDialog.show();
			}
		};
	}

}
