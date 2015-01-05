package com.cincodenada.thingtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;
import com.cincodenada.thingtracker.ViewHappenings.ViewHappeningsFragment;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.Build;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditThing extends ActionBarActivity {

    public static final String ARG_THING_ID = "thing_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_thing);

        if (savedInstanceState == null) {
            EditThingFragment f = new EditThingFragment();
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
        getMenuInflater().inflate(R.menu.edit_thing, menu);
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
    public static class EditThingFragment extends Fragment {

        private ThingsOpenHelper dbHelper;
        private Thing targetThing;
        EditText nameView;
        EditText newField;
        ListView fieldBucket;
        ArrayList<String> fieldList;

        LayoutInflater myInflater;

        public EditThingFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_thing,
                    container, false);

            Bundle args = getArguments();

            myInflater = inflater;

            dbHelper = new ThingsOpenHelper(getActivity());

            targetThing = dbHelper.getThing(args.getLong("thing_id"));

            nameView = (EditText)rootView.findViewById(R.id.txtThingName);
            nameView.setText(targetThing.getText());


            fieldList = new ArrayList<String>();
            if(targetThing.metadef.has("_order")) {
                try {
                    JSONArray order = targetThing.metadef.getJSONArray("_order");
                    int numkeys = order.length();
                    for (int i = 0; i < numkeys; i++) {
                        fieldList.add(order.getString(i));
                    }
                } catch (JSONException e) {
                    Log.e("fucker", "Couldn't parse order!");
                    // We'll fill it up with the keys below
                }
            }
            if(fieldList.size() == 0) {
                Iterator<String> keyIter = targetThing.metadef.keys();
                while(keyIter.hasNext()) {
                    fieldList.add(keyIter.next());
                }
            }

            fieldBucket = (ListView)rootView.findViewById(R.id.thing_fields);
            fieldBucket.setAdapter(new FieldAdapter<String>(
                    getActivity(),
                    fieldList,
                    targetThing.metadef
            ));

            String curKey, curType;
            Object curVal;
            TextView curLabel;
            View curField;

            newField = (EditText)rootView.findViewById(R.id.newField);

            rootView.findViewById(R.id.btnSaveThing).setOnClickListener(saveButtonListener);
            rootView.findViewById(R.id.btnAddThing).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!fieldList.contains(newField.getText())) {
                        fieldList.add(newField.getText().toString());
                    }
                }
            });

            return rootView;
        }

        protected OnClickListener saveButtonListener = new OnClickListener() {
            public void onClick(View v) {
                JSONObject fieldData = ((FieldAdapter<String>)fieldBucket.getAdapter()).getObject();
                dbHelper.saveThing(
                    targetThing.id,
                    nameView.getText().toString(),
                    fieldData.toString()
                );
                Log.d("fucker",fieldData.toString());
                EditThingFragment.this.getActivity().finish();
            }
        };
    }

}
