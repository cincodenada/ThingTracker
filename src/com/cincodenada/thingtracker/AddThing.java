package com.cincodenada.thingtracker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import org.apache.commons.lang3.StringUtils;

public class AddThing extends Activity {

    private ThingsOpenHelper dbHelper;
    private long curThingId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_thing);
        
        Log.v("Fucker","Yep, I'm here");
        
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
        LinearLayout buttonBin = (LinearLayout) findViewById(R.id.buttonbin);
        Button newButton;
        buttonBin.removeAllViews();
        for(ThingsOpenHelper.Thing curThing: dbHelper.getSubthings(curThingId)) {
            Log.v("Fucker",curThing.getText());
            newButton = new Button(AddThing.this);
            newButton.setText(curThing.getText());
            buttonBin.addView(newButton);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_thing, menu);
        return true;
    }
    
}
