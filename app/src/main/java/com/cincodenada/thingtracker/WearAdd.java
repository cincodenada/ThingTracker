package com.cincodenada.thingtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WearAdd extends Activity {
    private ThingsOpenHelper dbHelper;
    private long curThingId = 0;
    private ThingsOpenHelper.Thing curThing = null;
    private long prevThingId = 0;
    private ArrayList<ThingsOpenHelper.Thing> thingList;
    private ArrayAdapter<ThingsOpenHelper.Thing> thingArray;

    private static final int REQUEST_ADD_THING = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_add);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ListView buttonBin = (ListView) findViewById(R.id.button_bin);
                thingList = new ArrayList<ThingsOpenHelper.Thing>();
                thingArray = new ThingListAdapter(WearAdd.this,R.layout.thing_button,thingList);
                buttonBin.setAdapter(thingArray);

                buttonBin.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> list, View item, int pos, long id) {
                        ThingsOpenHelper.Thing curThing = (ThingsOpenHelper.Thing)list.getItemAtPosition(pos);
                        if(curThing.hasChildren()) {
                            setCurThing(curThing.id);
                        } else {
                            getHappening(curThing.id);
                        }
                    }
                });

                Button addBtn = (Button) findViewById(R.id.btnAddThing);
                addBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Thing Name");
                        // Start the activity, the intent will be populated with the speech text
                        startActivityForResult(intent, REQUEST_ADD_THING);
                  }
                });

                Button backBtn = (Button) findViewById(R.id.btnBack);
                backBtn.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) { LoadParentThing(); }
                });

                registerForContextMenu(buttonBin);

                dbHelper = new ThingsOpenHelper(WearAdd.this);
                loadThings();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_THING && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            Log.v("Fucker",spokenText);
            Log.v("Fucker","Adding text thing");
            dbHelper.addTextThing(spokenText,curThingId);
            loadThings();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void loadThings() {
        thingArray.clear();
        for(ThingsOpenHelper.Thing curThing: dbHelper.getSubthings(curThingId)) {
            //Log.d("Fucker",curThing.toString());
            thingArray.add(curThing);
        }
        curThing = dbHelper.getThing(curThingId);

        //Disable the Back button if we're at the root activity
        Button backBtn = (Button) findViewById(R.id.btnBack);
        backBtn.setEnabled(curThing != null);
        //Log.d("Fucker",Integer.toString(thingArray.getCount()));
    }

    protected void setCurThing(long newid) {
        prevThingId = curThingId;
        curThingId = newid;
        loadThings();
    }

    protected void deleteThing(long thingId) {
        dbHelper.deleteThing(thingId);
        loadThings();
    }

    protected void getHappening(long thingId) {
        Intent happeningIntent = new Intent(this, AddHappening.class);
        happeningIntent.putExtra(AddHappening.ARG_THING_ID, thingId);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.thing_menu, menu);

        AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView thingMenu = (ListView) itemInfo.targetView.getParent();
        ThingsOpenHelper.Thing selThing = (ThingsOpenHelper.Thing) thingMenu.getItemAtPosition(itemInfo.position);

        if(selThing.hasChildren()) {
            menu.setGroupVisible(R.id.mnu_group_nochildren, false);
        } else {
            menu.setGroupVisible(R.id.mnu_group_haschildren, false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView buttonBin = (ListView) info.targetView.getParent();
        ThingsOpenHelper.Thing selThing = (ThingsOpenHelper.Thing) buttonBin.getItemAtPosition(info.position);
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
            case R.id.mnu_backup:
                backupDb();
                return true;
            case R.id.mnu_restore:
                restoreDb();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void backupDb() {
        File file = new File("/data/data/com.cincodenada.thingtracker/databases/");
        try {
            File[] list = file.listFiles();
            Log.d("Fucker", file.toString());
            Log.d("Fucker", String.valueOf(list.length) + " files");
            if (list != null)
                for (int i=0; i<list.length; ++i) {
                    Log.d("Fucker", list[i].getName());
                }
        } catch(Exception e) {
            Log.e("Fucker", "Can't list " + file.toString() + ": " + e.toString());
        }
        dbHelper.backup();
    }

    public void restoreDb() {
        dbHelper.restore();
    }

    @Override
    public void onBackPressed() {
        boolean didSomething = LoadParentThing();
        if(!didSomething) { super.onBackPressed(); }
    }

    public boolean LoadParentThing() {
        if(curThing != null && curThing.id > 0) {
            setCurThing(curThing.parent_id);
            return true;
        } else {
            return false;
        }
    }

    class ThingListAdapter extends ArrayAdapter<ThingsOpenHelper.Thing> {
        private LayoutInflater myInflater;

        public ThingListAdapter(Context context, int resource, ArrayList<ThingsOpenHelper.Thing> thingList) {
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

            ThingsOpenHelper.Thing thing = (ThingsOpenHelper.Thing)getItem(pos);
            text.setText(thing.getText());
            arrow.setVisibility((thing.hasChildren() ? ImageView.VISIBLE : ImageView.INVISIBLE));

            return view;
        }
    }

}
