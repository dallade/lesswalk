package com.lesswalk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.lesswalk.MoreViewsActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elad on 21/10/16.
 */
public class DBController extends SQLiteOpenHelper {
    public DBController(Context context) {
        super(context, "androidsqlite.db", null, 1);
        //todo: com.amazonaws.auth..
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query;
        query = "CREATE TABLE users ( userId INTEGER PRIMARY KEY, userName TEXT, udpateStatus TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query;
        query = "DROP TABLE IF EXISTS users";
        db.execSQL(query);
        onCreate(db);
    }

    public void insertUser(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userName", queryValues.get("userName"));
        values.put("udpateStatus", "no");
        database.insert("users", null, values);
        database.close();
    }

    public ArrayList<HashMap<String, String>> getAllUsers() {
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT  * FROM users";
        SQLiteDatabase database = this.getWritableDatabase();
        try (Cursor cursor = database.rawQuery(selectQuery, null)) {
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("userId", cursor.getString(0));
                    map.put("userName", cursor.getString(1));
                    wordList.add(map);
                } while (cursor.moveToNext());
            }
        } finally {
            database.close();
        }
        return wordList;
    }


}
