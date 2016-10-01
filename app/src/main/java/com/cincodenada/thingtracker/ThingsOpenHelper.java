package com.cincodenada.thingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.cincodenada.thingtracker.TwoLineArrayAdapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
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

    // Public table names so we can use them in the object classes
    public static final String THINGLIST_TABLE_NAME = "things";
    public static final String HAPPENINGLIST_TABLE_NAME = "happenings";

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

    public long saveThing(Long id, String text, String metadef) {
        String[] params = {Long.toString(id)};
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("DATA", text);
        values.put("METADEF",metadef.toString());
        return db.update(THINGLIST_TABLE_NAME, values, "ID = ?", params);
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

    public ArrayList<Happening> getHappenings(long thing_id, boolean get_all) {
        ArrayList<String> idlist = new ArrayList<String>();
        String[] thingcols = {"id"};
        SQLiteDatabase db = this.getReadableDatabase();
        idlist.add(Long.toString(thing_id));
        ArrayList<Happening> HappeningList = new ArrayList<Happening>();
        Cursor cur;
        while(!idlist.isEmpty()) {
            String[] phlist = new String[idlist.size()];
            Arrays.fill(phlist, "?");
            String placeholders = StringUtils.join(phlist,",");
            String[] params = idlist.toArray(new String[idlist.size()]);
            cur = db.query(HAPPENINGLIST_TABLE_NAME,null,"THING_ID IN(" + placeholders + ")",params, null, null, null);
            if(cur.moveToFirst()) {
                do {
                    HappeningList.add(new Happening(cur));
                } while(cur.moveToNext());
            }

            idlist.clear();
            if(get_all) {
                cur = db.query(THINGLIST_TABLE_NAME, thingcols, "PARENT_ID IN(" + placeholders + ")",params,null,null,null);
                if(cur.moveToFirst()) {
                    do {
                        idlist.add(cur.getString(0));
                    } while(cur.moveToNext());
                }
            }
        }
        return HappeningList;
    }

    public Happening getHappening(long happening_id) {
        String[] params = {Long.toString(happening_id)};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(HAPPENINGLIST_TABLE_NAME,null,"ID = ?",params, null, null, null);
        if(cur.moveToFirst()) {
            return new Happening(cur);
        }
        return null;
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
        if(res.moveToFirst()) {
            return new Thing(res);
        } else {
            return null;
        }
    }

    public boolean deleteThing(long thing_id) {
        if(thing_id == 0) { return false; }
        String[] params = {Long.toString(thing_id)};
        SQLiteDatabase db = this.getReadableDatabase();
        boolean success = true;
        success = success && (db.delete(THINGLIST_TABLE_NAME,"id = ?",params) > 0);
        //success = success && (db.delete(HAPPENINGLIST_TABLE_NAME,"thing_id = ?",params) > 0);
        return success;
    }

    public boolean deleteHappening(long happening_id) {
        if(happening_id == 0) { return false; }
        String[] params = {Long.toString(happening_id)};
        SQLiteDatabase db = this.getReadableDatabase();
        int numdel = db.delete(HAPPENINGLIST_TABLE_NAME,"id = ?",params);
        return (numdel > 0);
	}

	public void backup() {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] noparams = {};
		db.rawQuery("PRAGMA journal_mode = DELETE;",noparams);
		try {
			Log.d("Fucker", "Opening files...");
			InputStream input = new FileInputStream("/data/data/com.cincodenada.thingtracker/databases/" + DATABASE_NAME);
			OutputStream output = new FileOutputStream("/sdcard/" + DATABASE_NAME + ".db");
			byte[] buffer = new byte[1024];
			int length;
			while((length=input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			output.flush();
			output.close();
			input.close();
		} catch(IOException e) {
			Log.e("Fucker","Error:" + e.toString());
		}
		db.rawQuery("PRAGMA journal_mode = WAL;", noparams);
	}

	public void restore() {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] noparams = {};
		db.rawQuery("PRAGMA journal_mode = DELETE;",noparams);
		try {
			Log.d("Fucker", "Opening files...");
			InputStream input = new FileInputStream("/sdcard/" + DATABASE_NAME + ".db");
			OutputStream output = new FileOutputStream("/data/data/com.cincodenada.thingtracker/databases/" + DATABASE_NAME);
			byte[] buffer = new byte[1024];
			int length;
			while((length=input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			output.flush();
			output.close();
			input.close();
		} catch(IOException e) {
			Log.e("Fucker","Error:" + e.toString());
		}
		db.rawQuery("PRAGMA journal_mode = WAL;", noparams);
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



    public class HappeningListAdapter extends TwoLineArrayAdapter<Happening> {
        HashMap<Long, String> nameCache;
        ThingsOpenHelper db;

        public HappeningListAdapter(Context context,
                ArrayList<Happening> theHaps, ThingsOpenHelper db) {
            super(context, theHaps);

            nameCache = new HashMap<Long, String>();
            this.db = db;
        }

        @Override public String lineOneText(Happening h) {
            if(nameCache.containsKey(h.thing_id)) {
                return nameCache.get(h.thing_id);
            } else {
                Thing parent = db.getThing(h.thing_id);
                nameCache.put(h.thing_id, parent.data);
                return parent.data;
            }
        }

        @Override public String lineTwoText(Happening h) {
            return h.toString();
        }
    }
}
