package com.cincodenada.thingtracker;

import java.util.Iterator;
import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.widget.Toast;

import com.cincodenada.thingtracker.ThingsOpenHelper.Thing;

public class EditHappening extends Activity {

    public static final String ARG_THING_ID = "thing_id";
    public static final String ARG_HAPP_ID = "happening_id";

    private ThingsOpenHelper dbHelper;
    private Thing targetThing;
    private Happening curHapp;
    private TimeButtons timeSel;

    HashMap<String,View> fieldMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_happening);

        dbHelper = new ThingsOpenHelper(EditHappening.this);

        findViewById(R.id.happening_save).setOnClickListener(saveButtonListener);

        Long thing_id = getIntent().getLongExtra(ARG_THING_ID, 0);
        Long happ_id = getIntent().getLongExtra(ARG_HAPP_ID, 0);
        if(happ_id > 0) {
            curHapp = dbHelper.getHappening(happ_id);
        }

        if(curHapp == null) {
            targetThing = dbHelper.getThing(thing_id);
            curHapp = new Happening(thing_id);
        } else {
            targetThing = dbHelper.getThing(curHapp.thing_id);
        }

        if(targetThing == null || curHapp == null) {
            Toast.makeText(EditHappening.this, "Parent thing not found!", Toast.LENGTH_SHORT).show();
            EditHappening.this.finish();
            return;
        }

        Iterator<String> keyIter = targetThing.metadef.keys();
        LinearLayout fieldBucket = (LinearLayout) findViewById(R.id.happening_fields);

        Calendar curTime = Calendar.getInstance();
        if(curHapp.timestamp > 0) {
            curTime.setTimeInMillis(curHapp.timestamp*1000);
        }

        timeSel = new TimeButtons(
            (Button)findViewById(R.id.btnDate),
            (Button)findViewById(R.id.btnHour),
            (Button)findViewById(R.id.btnMinute),
            curTime
        );

        String curKey, curType;
        Object curVal;
        TextView curLabel;
        View curField;
        fieldMap = new HashMap<String,View>();
        while(keyIter.hasNext()) {
            curKey = keyIter.next();
            try {
                curType = targetThing.metadef.getString(curKey);
            } catch (JSONException e) {
                continue;
            }

            try {
                curVal = curHapp.metadata.get(curKey);
            } catch (JSONException e) {
                curVal = null;
            }

            switch(curType) {
            case "yesno":
                curField = new CheckBox(EditHappening.this);
                ((CheckBox)curField).setText(curKey);
                if(curVal != null) { ((CheckBox)curField).setChecked((Boolean)curVal); }
                fieldBucket.addView(curField);
                fieldMap.put(curKey, curField);
                break;
            case "text":
            default:
                curLabel = new TextView(EditHappening.this);
                curLabel.setText(curKey);
                fieldBucket.addView(curLabel);

                curField = new EditText(EditHappening.this);
                if(curVal != null) { ((EditText)curField).setText((String)curVal); }
                fieldBucket.addView(curField);
                fieldMap.put(curKey, curField);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_happening, menu);
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


    protected OnClickListener saveButtonListener = new OnClickListener() {
        public void onClick(View v) {
            ThingsOpenHelper dbHelper = new ThingsOpenHelper(EditHappening.this);
            View curField;
            for(String curKey: fieldMap.keySet()) {
                curField = fieldMap.get(curKey);
                Log.d("ThingTracker","Saving field: " + curKey);
                try {
                    if(curField instanceof EditText)
                        curHapp.metadata.put(curKey, ((EditText)curField).getText());
                    else if(curField instanceof CheckBox) {
                        curHapp.metadata.put(curKey, ((CheckBox)curField).isChecked());
                    }
                } catch (JSONException e) {
                    Log.e("ThingTracker","Failed to parse field!");
                }
            }
            curHapp.timestamp = timeSel.getTime();
            curHapp.save(dbHelper);
            EditHappening.this.finish();
        }
    };

    public class TimeProvider {
        TextView timeButton;
        TextView dateButton;

        protected Calendar curTime;

        public long getTime() {
            return curTime.getTimeInMillis()/1000;
        }

        public void updateText(Calendar time) {
            this.timeButton.setText(DateFormat.format("HH:mm", time));
            this.dateButton.setText(DateFormat.format("MMMM d, yyyy",time));
        }
    }

    public class TimeSlider extends TimeProvider {
        protected SeekBar slider;

        protected Calendar baseTime;
        protected long seekCenter;

        protected double scaleunit = 60*60*24;

        public TimeSlider(SeekBar slider) {
            this.slider = slider;
            //Set up time/date
            this.baseTime = Calendar.getInstance();
            Calendar startTime = (Calendar)this.baseTime.clone();
            Calendar endTime = (Calendar)this.baseTime.clone();
            startTime.add(Calendar.YEAR, -1);
            endTime.add(Calendar.YEAR, 1);

            this.timeButton = (TextView) findViewById(R.id.happening_time);
            this.dateButton = (TextView) findViewById(R.id.happening_date);

            //Why doesn't compareTo work like I want it to?  int limits, maybe?
            long timespan = (endTime.getTimeInMillis() - startTime.getTimeInMillis())/1000;

            this.slider.setMax((int)(Math.sqrt(timespan/scaleunit/2)*scaleunit));
            this.slider.setProgress(slider.getMax()/2);
            this.slider.setOnSeekBarChangeListener(seekBarChanged);

            this.curTime = (Calendar) this.baseTime.clone();

            updateText(curTime);
        }

        private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                curTime.setTime(baseTime.getTime());
                int seekCenter = slider.getMax()/2;
                int sign = (seekBar.getProgress() < seekCenter) ? -1 : 1;
                curTime.add(Calendar.SECOND, sign*(int)(Math.pow((seekBar.getProgress() - seekCenter)/scaleunit,2)*scaleunit));

                updateText(curTime);
            }
        };
    }

    public class TimeButtons extends TimeProvider {
        Button dateBtn, hourBtn, minBtn;

        public TimeButtons(Button dateBtn, Button hourBtn, Button minBtn, Calendar curTime) {
            this.dateBtn = dateBtn;
            this.hourBtn = hourBtn;
            this.minBtn = minBtn;

            if(curTime == null) {
                this.curTime = Calendar.getInstance();
            } else {
                this.curTime = curTime;
            }
            this.timeButton = (TextView) findViewById(R.id.happening_time);
            this.dateButton = (TextView) findViewById(R.id.happening_date);

            this.dateBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View btn) {
                    DialogFragment datePicker = new DatePickerFragment();
                    datePicker.show(getFragmentManager(), "datePicker");
                }

            });

            this.minBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View btn) {
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getFragmentManager(), "timePicker");
                }

            });

            updateText(curTime);
        }

        @SuppressLint("ValidFragment")
        public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the current time as the default values for the picker
                int hour = curTime.get(Calendar.HOUR_OF_DAY);
                int minute = curTime.get(Calendar.MINUTE);

                Log.d("Fucker",String.format("Initiating dialog with hour %d and minute %d (%d)",hour,minute,curTime.getTimeInMillis()));

                // Create a new instance of TimePickerDialog and return it
                return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
            }

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.d("Fucker",String.format("Setting time to hour %d and minute %d (%d)",hourOfDay,minute,curTime.getTimeInMillis()));

                curTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                curTime.set(Calendar.MINUTE,minute);
                Log.d("Fucker",String.format("Time is now %d",curTime.getTimeInMillis()));
                updateText(curTime);
            }
        }

        @SuppressLint("ValidFragment")
        public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Use the current date as the default date in the picker
                int year = curTime.get(Calendar.YEAR);
                int month = curTime.get(Calendar.MONTH);
                int day = curTime.get(Calendar.DAY_OF_MONTH);

                // Create a new instance of DatePickerDialog and return it
                return new DatePickerDialog(getActivity(), this, year, month, day);
            }

            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                curTime.set(Calendar.YEAR,year);
                curTime.set(Calendar.MONTH,month);
                curTime.set(Calendar.DAY_OF_MONTH,day);
                updateText(curTime);
            }
        }
    }
}

