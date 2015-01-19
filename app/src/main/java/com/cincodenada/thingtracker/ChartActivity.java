package com.cincodenada.thingtracker;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.Collator;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ChartActivity extends ActionBarActivity {
    public static final String ARG_THING_ID = "thing_id";
    public static final String ARG_ALL_SUBTHINGS = "all_subthings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        if (savedInstanceState == null) {
            ChartFragment f = new ChartFragment();
            Bundle args = new Bundle();
            Long thing_id = getIntent().getLongExtra(ARG_THING_ID, 0);
            args.putLong(ARG_THING_ID, thing_id);
            boolean all_subthings = getIntent().getBooleanExtra(ARG_ALL_SUBTHINGS, true);
            args.putBoolean(ARG_ALL_SUBTHINGS, all_subthings);
            f.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .add(R.id.container, f).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ChartFragment extends Fragment {
        private XYPlot plot;
        private ThingsOpenHelper dbHelper;

        public ChartFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_chart, container, false);
            ArrayList<Happening> theHaps;

            Bundle args = getArguments();
            dbHelper = new ThingsOpenHelper(getActivity());

            theHaps = dbHelper.getHappenings(args.getLong(ARG_THING_ID), args.getBoolean(ARG_ALL_SUBTHINGS));
            HashMap<Long,ThingsOpenHelper.Thing> thingCache = new HashMap<>();

            HashMap<Long,HashMap<String,Integer>> serieses = new LinkedHashMap<Long,HashMap<String,Integer>>();
            String curCat;
            DateFormat df = new SimpleDateFormat("y-w");
            List<String> allCats = new ArrayList<String>();
            for(Happening theHap: theHaps) {
                curCat = df.format(new Date(theHap.timestamp*1000));
                Log.d("Fucker",curCat);
                if(allCats.indexOf(curCat) == -1) {
                    allCats.add(curCat);
                }

                ThingsOpenHelper.Thing curThing;
                if(thingCache.containsKey(theHap.thing_id)) {
                    curThing = thingCache.get(theHap.thing_id);
                } else {
                    curThing = dbHelper.getThing(theHap.thing_id);
                    thingCache.put(theHap.thing_id, curThing);
                }

                if(serieses.containsKey(theHap.thing_id)) {
                    if(serieses.get(theHap.thing_id).containsKey(curCat)) {
                        serieses.get(theHap.thing_id).put(
                                curCat,
                                serieses.get(theHap.thing_id).get(curCat) + 1
                        );
                    } else {
                        serieses.get(theHap.thing_id).put(curCat,1);
                    }
                } else {
                    HashMap<String,Integer> newSeries = new LinkedHashMap<String,Integer>();
                    newSeries.put(curCat,1);
                    serieses.put(theHap.thing_id, newSeries);
                }
            }
            Collections.sort(allCats, Collator.getInstance());

            GraphView graph = (GraphView)rootView.findViewById(R.id.graph);

            String[] hexColors = {
                "#a6cee3",
                "#1f78b4",
                "#b2df8a",
                "#33a02c",
                "#fb9a99",
                "#e31a1c",
                "#fdbf6f",
                "#ff7f00",
                "#cab2d6",
                "#6a3d9a",
                "#ffff99",
                "#b15928",
            };

            Integer numSeries = 0;
            for(Map.Entry<Long,HashMap<String,Integer>> entry: serieses.entrySet()) {
                DataPoint[] curList = new DataPoint[allCats.size()];
                HashMap<String,Integer> thingSeries = entry.getValue();

                Integer index = 0;
                for(String cat: allCats) {
                    if(thingSeries.containsKey(cat)) {
                        curList[index] = new DataPoint(index, thingSeries.get(cat));
                    } else {
                        curList[index] = new DataPoint(index, 0);
                    }
                    Log.d("Fucker", index.toString());
                    Log.d("Fucker", curList[index].toString());
                    index++;
                }

                LineGraphSeries<DataPoint> newSeries = new LineGraphSeries<>(curList);
                newSeries.setTitle(thingCache.get(entry.getKey()).getText());
                Integer curcol = numSeries % hexColors.length;
                newSeries.setColor(Color.parseColor(hexColors[curcol]));

                Log.d("Fucker", curList.toString());

                graph.addSeries(newSeries);

                numSeries++;
                if(numSeries > 4) {
                    break;
                }
            }

            graph.getLegendRenderer().setVisible(true);

            return rootView;
        }

        public class CustomFormat extends NumberFormat {
            List<String> valueList;
            public CustomFormat(List<String> newvalueList) {
                valueList = newvalueList;
            }
            @Override
            public StringBuffer format(double value, StringBuffer buffer, FieldPosition field) {
                return new StringBuffer(valueList.get((int)value));
            }

            @Override
            public StringBuffer format(long value, StringBuffer buffer, FieldPosition field) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }

            @Override
            public Number parse(String string, ParsePosition position) {
                throw new UnsupportedOperationException("Not yet implemented.");
            }
        }
    }
}
