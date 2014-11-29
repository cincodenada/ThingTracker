package com.cincodenada.thingtracker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by joel on 11/29/14.
 */
public class Happening {
    public long id;
    public long thing_id;
    public long timestamp;
    public JSONObject metadata;
    public DateFormat df;

    public Happening(Cursor cur) {
        this.id = cur.getLong(cur.getColumnIndex("ID"));
        this.thing_id = cur.getLong(cur.getColumnIndex("THING_ID"));
        this.timestamp = cur.getInt(cur.getColumnIndex("TIMESTAMP"));
        try {
            this.metadata = new JSONObject(cur.getString(cur.getColumnIndex("METADATA")));
        } catch (JSONException e) {
            this.metadata = new JSONObject();
        }

        df = DateFormat.getDateTimeInstance();
    }

    public Happening(long thing_id) {
        this.thing_id = thing_id;
        this.metadata = new JSONObject();

        df = DateFormat.getDateTimeInstance();
    }

    public String toString() {
        return df.format(new Date(this.timestamp*1000));
    }

    public long save(ThingsOpenHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TIMESTAMP",this.timestamp);
        values.put("METADATA",this.metadata.toString());
        values.put("THING_ID",this.thing_id);

        if(this.id == 0) {
            this.id = db.insert(dbHelper.HAPPENINGLIST_TABLE_NAME, null, values);
            return this.id;
        } else {
            String[] params = {Long.toString(id)};
            long numrows = db.update(dbHelper.HAPPENINGLIST_TABLE_NAME, values, "ID = ?", params);
            if(numrows > 0) {
                return this.id;
            } else {
                return -1;
            }
        }
    }
}
