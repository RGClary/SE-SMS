package com.ssms.se.seapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myDB";
    private static final int DATABASE_VERSION = 1;

    public static final String Messages = "CREATE TABLE messages (_id integer primary key autoincrement, "
            + "sender text not null," + "subject text not null," + "body text not null," + "ttl integer not null);";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(Messages);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS messages");

        onCreate(database);
    }

    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(DATABASE_NAME);
    }
}
