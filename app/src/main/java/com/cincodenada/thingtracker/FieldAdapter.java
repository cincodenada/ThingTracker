package com.cincodenada.thingtracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class FieldAdapter<T> extends ArrayAdapter<T> {
    public static final LinkedHashMap<String, String> fieldTypes = new LinkedHashMap<String, String>();
    static {
        fieldTypes.put("text","Text");
        fieldTypes.put("yesno","Checkbox");
        fieldTypes.put("list","List");
    }

    private int mListItemLayoutResId;
    private JSONObject data;
    public ArrayList<T> keys;

    public ArrayList<String> typeKeyList;
    private ArrayAdapter<String> typeList;

    public FieldAdapter(Context context, ArrayList<T> keys, JSONObject data) {
        this(context, R.layout.field_editor, keys, data);
    }

    public FieldAdapter(
            Context context,
            int listItemLayoutResourceId,
            ArrayList<T> inKeys,
            JSONObject inData) {
        super(context, listItemLayoutResourceId, inKeys);
        data = inData;
        keys = inKeys;
        mListItemLayoutResId = listItemLayoutResourceId;

        int numTypes = fieldTypes.size();
        typeList = new ArrayAdapter<String>(
                context,
                android.R.layout.simple_spinner_item,
                fieldTypes.values().toArray(new String[numTypes])
        );
        typeKeyList = new ArrayList<String>(fieldTypes.keySet());
    }

    @Override
    public android.view.View getView(
            int position,
            View convertView,
            ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater)getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View listItemView = convertView;
        if (null == convertView) {
            listItemView = inflater.inflate(
                    mListItemLayoutResId,
                    parent,
                    false);
        }

        // The ListItemLayout must use the standard text item IDs.
        TextView name = (TextView)listItemView.findViewById(R.id.fieldText);
        Spinner type = (Spinner)listItemView.findViewById(R.id.spinnerType);

        String key = ((T)getItem(position)).toString();
        name.setText(key);

        int typePos;
        try {
            String value = data.getString(key);
            typePos = typeKeyList.indexOf(value);
        } catch(JSONException e) {
            typePos = 0;
        }
        type.setAdapter(typeList);
        type.setTag(key);
        if(typePos > -1) {
            type.setSelection(typePos);
        }

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String typeKey = typeKeyList.get(i);
                try {
                    data.put(adapterView.getTag().toString(), typeKey);
                } catch(JSONException e) {
                    Log.e("fucker", "Failed to update type!");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        //ImageButton delbtn = (ImageButton)listItemView.findViewById(R.id.btnDelField);
        //delbtn.setOnClickListener(delFieldListener);
        //delbtn.setTag(key);

        return listItemView;
    }

    public JSONObject getObject() {
        Iterator<String> keyIter = data.keys();
        while(keyIter.hasNext()) {
            String curKey = keyIter.next();
            if(!keys.contains(curKey)) {
                data.remove(curKey);
            }
        }

        return data;
    }
}
