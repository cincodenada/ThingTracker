package com.cincodenada.thingtracker;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;
import com.cincodenada.thingtracker.ViewHappenings.ViewHappeningsFragment;

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
import android.widget.ListView;
import android.os.Build;

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
        EditText metadata;

        public EditThingFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_edit_thing,
                    container, false);

            Bundle args = getArguments();

            dbHelper = new ThingsOpenHelper(getActivity());

            targetThing = dbHelper.getThing(args.getLong("thing_id"));

            nameView = (EditText)rootView.findViewById(R.id.txtThingName);
            metadata = (EditText)rootView.findViewById(R.id.txtThingMetadata);

            nameView.setText(targetThing.data);
            metadata.setText(targetThing.metadef.toString());

            rootView.findViewById(R.id.btnSaveThing).setOnClickListener(saveButtonListener);

            return rootView;
        }

        protected OnClickListener saveButtonListener = new OnClickListener() {
            public void onClick(View v) {
                //TODO: Validate the JSON here
                dbHelper.saveThing(targetThing.id, nameView.getText().toString(), metadata.getText().toString());
                EditThingFragment.this.getActivity().finish();
            }
        };
    }

}
