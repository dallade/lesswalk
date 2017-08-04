package com.lesswalk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elad on 21/10/16.
 */
public class LocalDB extends SQLiteOpenHelper {
    private static final String TAG = LocalDB.class.getSimpleName();
    private String currentUser   = null;

    public LocalDB(Context context) {
        super(context, "androidsqlite.db", null, 1);
    }

    /*
select 'table user';
create table user (key varchar(20) unique, name varchar(20));
insert into user values('a0','user0');
insert into user values('a1','user1');
insert into user values('a2','user2');
select * from user;
select '--------------------------';

select 'table sig';
create table sig (key varchar(20) unique, owner varchar(20), name varchar(20));
insert into sig values('b0','a0', 'first');
insert into sig values('b1','a1', 'second');
insert into sig values('b2','a0', 'third');
select * from sig;
select '--------------------------';

select 'table userSig';
create table userSig (sig varchar(20), user varchar(20));
insert into userSig values('b0','a1');
select * from userSig;
select '--------------------------';

select 'signatures a1 can only view:';
select * from sig as s left join userSig as us on us.sig==s.key where us.user=='a1';
select '--------------------------';

select 'signatures a1 can view:';
select * from sig as s left join userSig as us on us.sig==s.key where us.user=='a1' OR s.owner=='a1' order by us.user desc;
select '--------------------------';

select '$$$$$$$$$$$$$$$$$$$$$$$$$$';


    * */
    protected static abstract class DbTable {
        final static String T_KEY    = "TEXT UNIQUE";
        final static String T_STRING = "TEXT";
        final static String T_INT    = "INT";
        final static String T_FLOAT  = "FLOAT";
        String NAME;
        ArrayList<Pair<String, String>> FIELDS = new ArrayList<>();
    }

    private final class User extends DbTable {
        final String NAME      = "user";
        final String key       = "key";
        final String phone     = "phone";
        final String firstName = "firstName";
        final String lastName  = "lastName";
        User(){
            FIELDS.add(new Pair<>(key, T_KEY));
            FIELDS.add(new Pair<>(phone, T_STRING));
            FIELDS.add(new Pair<>(firstName, T_STRING));
            FIELDS.add(new Pair<>(lastName, T_STRING));
        }
    }

    private final class Signature extends DbTable {
        final String NAME  = "sig";
        final String key   = "key";
        final String type  = "type";
        final String eTag  = "eTag";
        final String owner = "owner";
        Signature(){
            FIELDS.add(new Pair<>(key, T_KEY));
            FIELDS.add(new Pair<>(type, T_STRING));
            FIELDS.add(new Pair<>(eTag, T_STRING));
            FIELDS.add(new Pair<>(owner, T_STRING));
        }
    }

    public enum SignaturePermission {Owner, Editor, Viewer}

    private final class UserSignature extends DbTable {
        final String NAME         = "userSig";
        final String user         = "user";
        final String sig          = "sig";
        final String permission   = "permission";
        final String offsetOfPage = "offsetOfPage";
        final String offsetInPage = "offsetInPage";
        ArrayList<Pair<String, String>> FIELDS = new ArrayList<>();
        private UserSignature() {
            FIELDS.add(new Pair<>(user, T_STRING));
            FIELDS.add(new Pair<>(sig, T_STRING));
            FIELDS.add(new Pair<>(permission, T_STRING));
            FIELDS.add(new Pair<>(offsetOfPage, T_INT));
            FIELDS.add(new Pair<>(offsetInPage, T_INT));
        }
    }

    private final User          _user    = new User();
    private final Signature     _sig     = new Signature();
    private final UserSignature _userSig = new UserSignature();
    private final DbTable[]     dbTables = new DbTable[]{_user, _sig, _userSig};

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "";
        for (DbTable table : dbTables) {
            String fields = "";
            for (Pair<String,String> f : table.FIELDS) {
                fields += String.format("%s %s, ", f.first, f.second);
            }
            fields = fields.substring(0, fields.length()-2);//removes the extra ", "
            query += String.format("CREATE TABLE %s (%s);", table.NAME, fields);
        }
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "";
        for (DbTable table : dbTables) {
            query += String.format("DROP TABLE IF EXISTS %s;", table.NAME);
        }
        db.execSQL(query);
        onCreate(db);
    }

    public void addCurrentUser(
            @NonNull String uuid
            , @NonNull String phone
            , @NonNull String firstName
            , @NonNull String lastName
    ){
        if (currentUser != null) return;//TODO handle wrong situation
        currentUser = uuid;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_user.key, currentUser);
        values.put(_user.phone, phone);
        values.put(_user.firstName, firstName);
        values.put(_user.lastName, lastName);
        db.insert(_user.NAME, null, values);
        db.close();
    }

    public void addContactUser(
            @NonNull String uuid
            , @NonNull String phone
            , @NonNull String firstName
            , @NonNull String lastName
    ){
        if (uuid.equals(currentUser)) return;//TODO handle wrong situation
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_user.key, uuid);
        values.put(_user.phone, phone);
        values.put(_user.firstName, firstName);
        values.put(_user.lastName, lastName);
        db.insert(_user.NAME, null, values);
        db.close();
    }

    public void addSignatureOfContact(
            @NonNull String uuid
            , @NonNull String type
            , @NonNull String eTag
            , @NonNull String owner
            , @NonNull SignaturePermission permission
            , int offsetOfPage
            , int offsetInPage
    ){
        if (owner.equals(currentUser)) return;//TODO handle wrong situation
        if (permission.equals(SignaturePermission.Owner)) return;//TODO handle wrong situation
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_sig.key, uuid);
        values.put(_sig.type, type);
        values.put(_sig.eTag, eTag);
        values.put(_sig.owner, owner);
        db.insert(_sig.NAME, null, values);
        //
        values.clear();
        values.put(_userSig.sig, uuid);
        values.put(_userSig.user, currentUser);
        values.put(_userSig.permission, permission.name());
        values.put(_userSig.offsetOfPage, offsetOfPage);
        values.put(_userSig.offsetInPage, offsetInPage);
        db.insert(_userSig.NAME, null, values);
        //
        db.close();
    }

    public void addSignatureOfCurrentUser(
            @NonNull String uuid
            , @NonNull String type
            , @NonNull String eTag
            , int offsetOfPage
            , int offsetInPage
    ){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(_sig.key, uuid);
        values.put(_sig.type, type);
        values.put(_sig.eTag, eTag);
        values.put(_sig.owner, currentUser);
        db.insert(_sig.NAME, null, values);
        //
        values.clear();
        values.put(_userSig.sig, uuid);
        values.put(_userSig.user, currentUser);
        values.put(_userSig.permission, SignaturePermission.Owner.name());
        values.put(_userSig.offsetOfPage, offsetOfPage);
        values.put(_userSig.offsetInPage, offsetInPage);
        db.insert(_userSig.NAME, null, values);
        //
        db.close();
    }

    private Object getFieldValue(Cursor c, Pair<String, String> field){
        int columnIndex = c.getColumnIndexOrThrow(field.first);
        switch (field.second){
            case DbTable.T_KEY:
            case DbTable.T_STRING:
                return c.getString(columnIndex);
            case DbTable.T_INT:
                return c.getInt(columnIndex);
            case DbTable.T_FLOAT:
                return c.getFloat(columnIndex);
            default:
                Log.e(TAG, "getFieldValue -- failed to parse the field type!");
                return null;
        }
    }

    public ArrayList<HashMap<String, Object>> getContactUsers() {
        ArrayList<HashMap<String, Object>> wordList;
        wordList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        //String selectQuery = String.format("SELECT * FROM %s WHERE %s!=%s", _user.NAME, _user.key, currentUser);
        //try (Cursor cursor = db.rawQuery(selectQuery, null)) {
        try (Cursor cursor = db.query(
                _user.NAME
                , null
                , String.format("%s!=?", _user.key)
                , new String[]{currentUser}
                , null, null
                , _user.firstName +","+_user.lastName
        )) {
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, Object> map = new HashMap<>();
                    for (Pair<String,String> f : _user.FIELDS) {
                        map.put(f.first, getFieldValue(cursor, f));
                    }
                    wordList.add(map);
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }
        return wordList;
    }


}
