package com.cincodenada.thingtracker;

import java.text.DateFormat;
import java.util.ArrayList;
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
    private static final String HAPPENEDLIST_TABLE_NAME = "happenings";
    private static final String THINGLIST_TABLE_CREATE =
                "CREATE TABLE " + THINGLIST_TABLE_NAME + " (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "PARENT_ID INTEGER DEFAULT NULL," +
                "TYPE TEXT," +
                "DATA TEXT);";
    private static final String HAPPENEDLIST_TABLE_CREATE =
            "CREATE TABLE " + HAPPENEDLIST_TABLE_NAME + " (" +
            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
            "THING_ID INTEGER," +
            "TIMESTAMP INTEGER);";
    
	public ThingsOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(THINGLIST_TABLE_CREATE);
		db.execSQL(HAPPENEDLIST_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	public long addTextThing(String text, long parent_id) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
        values.put("PARENT_ID", parent_id);
		values.put("TYPE", "text");
		values.put("DATA", text);
		return db.insert(THINGLIST_TABLE_NAME, null, values);
	}
	
	public long addHappening(long thing_id, long when) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("THING_ID",thing_id);
		values.put("TIMESTAMP",when);
		return db.insert(HAPPENEDLIST_TABLE_NAME, null, values);
	}

	public long addThingWithHappening(String text, long parent_id, long when) {
		long thing_id = addTextThing(text, parent_id);
		if(thing_id == -1) {
			return -1;
		} else {
			return addHappening(thing_id, when);
		}
	}
	
	public ArrayList<Thing> getSubthings(long thing_id) {
		String[] cols = {"ID","TYPE","DATA"};
		String[] params = {Long.toString(thing_id)};
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor res = db.query(THINGLIST_TABLE_NAME,cols,"PARENT_ID = ?",params,null,null,null,null);
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

	public class Thing {
		long id;
		String type;
		String data;
		String description;
		
		public Thing(Cursor cur) {
			this.id = cur.getLong(cur.getColumnIndex("ID"));
			this.type = cur.getString(cur.getColumnIndex("TYPE"));
			this.data = cur.getString(cur.getColumnIndex("DATA"));
		}
		
		public String getText() {
			return data;
		}

		public String toString() {
			return data;
		}
	}
}
