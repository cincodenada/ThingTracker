package com.cincodenada.thingtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
        ViewGroup fieldBucket;

        ArrayList<String> keyList;
        ArrayAdapter<String> fieldList;
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

            Iterator<String> keyIter = targetThing.metadef.keys();
            fieldBucket = (ViewGroup)rootView.findViewById(R.id.thing_fields);

            String curKey, curType;
            Object curVal;
            TextView curLabel;
            View curField;

            int numTypes = ThingFields.fieldTypes.size();
            fieldList = new ArrayAdapter<String>(
                    (Context)getActivity(),
                    android.R.layout.simple_spinner_item,
                    ThingFields.fieldTypes.values().toArray(new String[numTypes])
            );
            keyList = new ArrayList<String>(ThingFields.fieldTypes.keySet());
            while(keyIter.hasNext()) {
                curKey = keyIter.next();
                try {
                    curType = targetThing.metadef.getString(curKey);
                } catch (JSONException e) {
                    continue;
                }

                fieldBucket.addView(makeFieldView(curKey, curType));
            }

            newField = (EditText)rootView.findViewById(R.id.newField);

            rootView.findViewById(R.id.btnSaveThing).setOnClickListener(saveButtonListener);
            rootView.findViewById(R.id.btnAddThing).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newThing = (String)ThingFields.fieldTypes.values().toArray()[0];
                    fieldBucket.addView(makeFieldView(newField.getText().toString(),newThing));
                    newField.setText("");
                }
            });

            return rootView;
        }

        protected View makeFieldView(String key, String val) {
            View curLine = myInflater.inflate(R.layout.field_editor, null);
            Spinner curSpin = (Spinner)curLine.findViewById(R.id.spinnerType);
            curSpin.setAdapter(fieldList);
            int typePos = keyList.indexOf(val);
            if(typePos > -1) {
                curSpin.setSelection(typePos);
            }
            ((TextView)curLine.findViewById(R.id.fieldText)).setText(key);
            ImageButton delbtn = (ImageButton)curLine.findViewById(R.id.btnDelField);
            delbtn.setOnClickListener(delFieldListener);
            delbtn.setTag(key);

            return curLine;
        }

        protected OnClickListener delFieldListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                View curLine = (View)v.getParent();
                ((ViewGroup)curLine.getParent()).removeView(curLine);
            }
        };

        protected OnClickListener saveButtonListener = new OnClickListener() {
            public void onClick(View v) {
                // Rebuild fields
                targetThing.metadef = new JSONObject();
                int numChildren = fieldBucket.getChildCount();
                for(int curChild=0; curChild < numChildren; curChild++) {
                    View curLine = fieldBucket.getChildAt(curChild);
                    Spinner type = (Spinner)curLine.findViewById(R.id.spinnerType);
                    TextView name = (TextView)curLine.findViewById(R.id.fieldText);
                    try {
                        targetThing.metadef.put(name.getText().toString(), keyList.get(type.getSelectedItemPosition()));
                    } catch(JSONException e) {
                        Log.e("fucker","Failed to save field for " + name.getText().toString());
                        Toast.makeText(getActivity(), "Failed to save field for " + name.getText().toString(),Toast.LENGTH_SHORT).show();
                    }
                }

                dbHelper.saveThing(targetThing.id, nameView.getText().toString(),targetThing.metadef.toString());
                Log.d("fucker",targetThing.metadef.toString());
                EditThingFragment.this.getActivity().finish();
            }
        };
    }

}
