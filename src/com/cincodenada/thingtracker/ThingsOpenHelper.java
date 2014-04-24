package com.cincodenada.thingtracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class ThingsOpenHelper extends SQLiteOpenHelper {
    
	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "thingDB";
    private static final String THINGLIST_TABLE_NAME = "things";
    private static final String HAPPENINGLIST_TABLE_NAME = "happenings";
    private static final String THINGLIST_TABLE_CREATE =
                "CREATE TABLE " + THINGLIST_TABLE_NAME + " (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PARENT_ID INTEGER DEFAULT NULL," +
                "TYPE TEXT," +
                "METADEF TEXT," +
                "DESCRIPTION TEXT," +
                "DATA TEXT);";
    private static final String HAPPENINGLIST_TABLE_CREATE =
            "CREATE TABLE " + HAPPENINGLIST_TABLE_NAME + " (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "THING_ID INTEGER," +
            "METADATA TEXT," +
            "LAT FLOAT," +
            "LON FLOAT," +
            "TIMESTAMP INTEGER);";
    
	public ThingsOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(THINGLIST_TABLE_CREATE);
		db.execSQL(HAPPENINGLIST_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public long addTextThing(String text, long parent_id) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		String metadefval = "";
		try {
			JSONObject metadef = new JSONObject();
			metadef.put("Description", "text");
			metadefval = metadef.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
        values.put("PARENT_ID", parent_id);
		values.put("TYPE", "text");
		values.put("DATA", text);
		values.put("METADEF",metadefval);
		return db.insert(THINGLIST_TABLE_NAME, null, values);
	}
	
	public long addHappening(long thing_id, long when) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("THING_ID",thing_id);
		values.put("TIMESTAMP",when);
		return db.insert(HAPPENINGLIST_TABLE_NAME, null, values);
	}

	public long addHappening(long thing_id, long when, JSONObject metadata) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("THING_ID",thing_id);
		values.put("TIMESTAMP",when);
		values.put("METADATA",metadata.toString());
		return db.insert(HAPPENINGLIST_TABLE_NAME, null, values);
	}

	public long addThingWithHappening(String text, long parent_id, long when) {
		long thing_id = addTextThing(text, parent_id);
		if(thing_id == -1) {
			return -1;
		} else {
			return addHappening(thing_id, when);
		}
	}
	
	public ArrayList<Happening> getHappenings(long thing_id) {
		String[] params = {Long.toString(thing_id)};
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(HAPPENINGLIST_TABLE_NAME,null,"THING_ID = ?",params, null, null, null);
		ArrayList<Happening> HappeningList = new ArrayList<Happening>();
        if(cur.moveToFirst()) {
            do {
                HappeningList.add(new Happening(cur));
            } while(cur.moveToNext());
        }
		return HappeningList;
	}
	
	public ArrayList<Thing> getSubthings(long thing_id) {
		String[] params = {Long.toString(thing_id)};
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery(
				"SELECT t.*, COUNT(st.id) AS numchildren FROM " + THINGLIST_TABLE_NAME + " t " +
				"LEFT JOIN " + THINGLIST_TABLE_NAME + " st ON st.PARENT_ID = t.ID " +
				"WHERE t.PARENT_ID = ? " +
				"GROUP BY t.ID", params);
		return getThingsFromCursor(res);
	}
	
	public ArrayList<Thing> getRootThings() {
        return getSubthings(0);
	}
	
	public ArrayList<Thing> getThingsFromCursor(Cursor cur) {
		ArrayList<Thing> ThingList = new ArrayList<Thing>();
        if(cur.moveToFirst()) {
            do {
                ThingList.add(new Thing(cur));
            } while(cur.moveToNext());
        }
		return ThingList;
	}

	public Thing getThing(long thing_id) {
		if(thing_id == 0) { return null; }
		String[] params = {Long.toString(thing_id)};
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.rawQuery(
				"SELECT t.*, COUNT(st.id) AS numchildren FROM " + THINGLIST_TABLE_NAME + " t " +
				"LEFT JOIN " + THINGLIST_TABLE_NAME + " st ON st.PARENT_ID = t.ID " +
				"WHERE t.ID = ? " +
				"GROUP BY t.ID", params);
		res.moveToFirst();
		return new Thing(res);
	}

	public class Thing {
		long id;
		long parent_id;
		long num_children;
		String type;
		String data;
		String description;
		JSONObject metadef;
		
		public Thing(Cursor cur) {
			this.id = cur.getLong(cur.getColumnIndex("ID"));
			this.parent_id = cur.getLong(cur.getColumnIndex("PARENT_ID"));
			this.type = cur.getString(cur.getColumnIndex("TYPE"));
			this.data = cur.getString(cur.getColumnIndex("DATA"));
			this.num_children = cur.getInt(cur.getColumnIndex("numchildren"));
			try {
				this.metadef = new JSONObject(cur.getString(cur.getColumnIndex("METADEF")));
			} catch (JSONException e) {
				this.metadef = new JSONObject();
			}
		}
		
		public String getText() {
			return data;
		}
		
		public boolean hasChildren() {
			return (num_children > 0);
		}

		public String toString() {
			return data;
		}
	}
	
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
		
		public String toString() {
			return df.format(new Date(this.timestamp*1000));
		}
	}
}
