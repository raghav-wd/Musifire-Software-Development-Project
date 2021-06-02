package com.example.musicplayer.Cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String TAG = "DatabaseHelper";
    private static String TABLE_NAME = "cache_song_table";
    private static String COL1 = "ID";
    private static String COL2 = "TITLE";
    private static String COL3 = "ALBUM";
    private static String COL4 = "ARTIST";
    private static String COL5 = "DURATION";
    private static String COL6 = "ALBUM_ART";
    private static String COL7 = "URL";

    private static String TABLE_NAME1 = "song_table";

    private static String TABLE_Name_ = "status_table";
    private static String COL_1 = "ID";
    private static String COL_2 = "CACHE";
    private static String COL_3 = "LOCKLYRICSACTIVITY";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 +
        " TEXT, " + COL3 + " TEXT, "+ COL4 + " TEXT, "+ COL5 + " TEXT, "+ COL6 + " BLOB, " + COL7 + " TEXT )";
        sqLiteDatabase.execSQL(createTable);

        createTable = "CREATE TABLE " + TABLE_NAME1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2 +
                " TEXT, " + COL3 + " TEXT, "+ COL4 + " TEXT, "+ COL5 + " TEXT, "+ COL6 + " BLOB, " + COL7 + " TEXT )";
        sqLiteDatabase.execSQL(createTable);

        createTable = "CREATE TABLE " + TABLE_Name_+ " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_2 +
        " TEXT, "+ COL_3 +" TEXT )";
        sqLiteDatabase.execSQL(createTable);
//        setCached("-1");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void deleteAll(int tb)
    {
        String TABLE;
        if(tb == 0 )TABLE = TABLE_NAME;
        else TABLE = TABLE_NAME1;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        // db.delete(TABLE_NAME,null,null);
        //db.execSQL("delete * from"+ TABLE_NAME);
        sqLiteDatabase.execSQL("DELETE FROM "+ TABLE);
        Log.e(TAG, "deleteAll: Success");
    }

    public boolean addData(int tb, String item2, String item3, String item4, String item5, byte[] item6, String item7){
        String TABLE;
        if(tb == 0 )TABLE = TABLE_NAME;
        else TABLE = TABLE_NAME1;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, item2);
        contentValues.put(COL3, item3);
        contentValues.put(COL4, item4);
        contentValues.put(COL5, item5);
        contentValues.put(COL6, item6);
        contentValues.put(COL7, item7);

        long result = sqLiteDatabase.insert(TABLE, null, contentValues);
        if(result == -1)return false;
        else return true;
    }

    public int getTotalTracks(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor data = sqLiteDatabase.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        return data.getCount();
    }

    public void updateSongs() {
//        deleteAll(TABLE_NAME);
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String tmp = TABLE_NAME;
//      sqLiteDatabase.rawQuery("ALTER TABLE "+ TABLE_NAME +" RENAME TO "+ "tmp" +";", null);
//      sqLiteDatabase.rawQuery("ALTER TABLE "+ TABLE_NAME1 +" RENAME TO "+ tmp +";", null);
//      sqLiteDatabase.rawQuery("ALTER TABLE "+ "tmp" +" RENAME TO "+ TABLE_NAME1 +";", null);
        sqLiteDatabase.execSQL("INSERT INTO cache_song_table " +
                "SELECT * " +
                "FROM song_table;");
        Log.e(TAG, "updateSongs: Swapped Successfully.");
    }

    public Cursor getData(int tb){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String TABLE;
        if(tb == 0 )TABLE = TABLE_NAME;
        else TABLE = TABLE_NAME1;
        Cursor data = sqLiteDatabase.query(TABLE, null, null, null, null, null, COL2 +" ASC");
        return data;
    }

    //Checks whether appData cached successfully or not.
    public String isCached() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor data = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_Name_, null);
////        try {
//            if(data.getString(1) == "1") return true;
//        }catch (Exception e)
//        {
//            Log.e(TAG, "isCached: ", e);
//        }
        if( data != null && data.moveToFirst() ){
            return data.getInt(1)+"";
        }
        return "-1";
    }

    public boolean setCached(String item2) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM "+ TABLE_Name_);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, item2);

        long result = sqLiteDatabase.insert(TABLE_Name_, null, contentValues);
        if(result == -1)return false;
        else return true;
    }

    public String getLockLyricsActivity() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor data = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_Name_, null);
        if(data != null && data.moveToFirst()) {
            return data.getInt(2)+"";
        }
        return "false";
    }

    public boolean setLockLyricsActivity(String item3) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("DELETE FROM " + TABLE_Name_);

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, item3);

        long result = sqLiteDatabase.insert(TABLE_Name_, null, contentValues);
        if(result == -1) return false;
        else return true;
    }
}