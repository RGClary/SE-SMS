package com.ssms.se.seapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 *
 * Runs database functions of Inserting and Receiving
 */
public class DBAdapter {

    private Context context;
    private SQLiteDatabase myDB;
    private DBHelper dbHelper;

    public DBAdapter(Context context) {
        this.context=context;
    }

    public DBAdapter open() throws SQLException {
        dbHelper = new DBHelper(context);
        myDB = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        dbHelper.close();
    }

    public ArrayList<MessageData> fetchMessages() {
        ArrayList<MessageData> messages = new ArrayList<>();
        Cursor query = myDB.query("messages", new String[] { "sender", "subject",
                "body", "ttl" }, null, null, null, null, null);
        while (query.moveToNext()){
            String sender = query.getString(0);
            String subject = query.getString(1);
            String body = query.getString(2);
            int ttl = query.getInt(3);

            messages.add(new MessageData(sender, subject, body, ttl));
        }
        query.close();
        return messages;
    }

    public void deleteTable(String tablename){
        myDB.execSQL("DROP TABLE IF EXISTS "+tablename);
    }

    public void InsertMessage(String sender, String subject, String body, int ttl) {
        ContentValues values = new ContentValues();
        values.put("sender", sender);
        values.put("subject", subject);
        values.put("body", body);
        values.put("ttl", ttl);

        myDB.insert("messages", null, values);
    }

    public void DeleteMessage(String sender, String subject, String body){
        myDB.execSQL("DELETE FROM messages WHERE sender=\'"+sender+"\' AND subject=\'"+ subject +"\' AND body=\'"+ body +"\'");
        Log.d("DBAdapter", "Sender: "+sender+" Subject: "+subject+" Body: "+body);
    }

}
