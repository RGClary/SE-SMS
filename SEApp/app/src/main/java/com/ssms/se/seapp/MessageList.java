package com.ssms.se.seapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MessageList extends AppCompatActivity implements View.OnClickListener {

    private ListView lv;

    public final static String EXTRA_MESSAGE = "com.ssms.se.seapp";

    // Listview Adapter
    ArrayAdapter<String> adapter;
    MainListAdapter mainListAdapter;
    DBAdapter dbAdapter;
    Button reset;
    Context context;
    String accountType;
    ArrayList<MessageData> temp;
    String usernameString;
    String readmessageReturn;
    MessagePoller messagePollerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_message_list);

        SharedPreferences sharedPref = getSharedPreferences(EXTRA_MESSAGE, MODE_PRIVATE);
        accountType = sharedPref.getString("ACCOUNT_TYPE", null);
        usernameString = sharedPref.getString("USERNAME", null);
        Log.d("MessageList", "accountType: "+accountType+" username: "+usernameString);


        accountType = getIntent().getStringExtra("ACCOUNT_TYPE");
        usernameString = getIntent().getStringExtra("USERNAME");
        messagePollerTask = new MessagePoller(usernameString);
        temp = new ArrayList<>();

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        temp = dbAdapter.fetchMessages();
        dbAdapter.close();
        mainListAdapter = new MainListAdapter(temp,this);
        lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(mainListAdapter);

        reset = (Button) findViewById(R.id.reset_list);
        reset.setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        SharedPreferences sharedPref = getSharedPreferences(EXTRA_MESSAGE, MODE_PRIVATE);
        accountType = sharedPref.getString("ACCOUNT_TYPE", null);
        usernameString = sharedPref.getString("USERNAME", null);
        if(accountType.equals("admin"))
            getMenuInflater().inflate(R.menu.menu_message_list_admin, menu);
        else
            getMenuInflater().inflate(R.menu.menu_message_list_user, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            case R.id.action_admin_controls:
                startActivity(new Intent(this, AdminControl.class));
                return true;

            case R.id.action_compose:
                startActivity(new Intent(this, Compose.class));
                return true;

            case R.id.action_sign_out:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;


        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.reset_list:
                ArrayList<MessageData> temp;
                MessagePoller messagePoller = new MessagePoller(usernameString);
                messagePoller.execute((Void) null);
                dbAdapter.open();
                dbAdapter.InsertMessage("AAAAA", "Meeting", "Meeting today at 4:30", 15);
                dbAdapter.InsertMessage("BBBBB", "Hey", "Meeting today at 4:30", 30);
                dbAdapter.InsertMessage("CCCCC", "Program", "Meeting today at 4:30", 60);
                temp = dbAdapter.fetchMessages();
                dbAdapter.close();
                mainListAdapter = new MainListAdapter(temp,this);
                lv.setAdapter(mainListAdapter);
                break;
        }
    }

    public class MessagePoller extends AsyncTask<Void, Void, Boolean> {

        String mUsername = "";
        String returnString = "";
        MessagePoller(String username) {
            mUsername = username;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/readmessage.php");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection client = null;
                try {
                    client = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject userCredentials = new JSONObject();
                userCredentials.put("username", usernameString);
                System.out.println(userCredentials.toString());
                String postArray = "user_credentials=" + userCredentials.toString();
                try {
                    client.setRequestMethod("POST");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setRequestProperty("charset", "utf-8");
                    client.setRequestProperty("Content-Length", Integer.toString(postArray.length()));
                    client.setUseCaches(false);
                    client.setDoOutput(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OutputStreamWriter out = null;
                try {
                    out = new OutputStreamWriter(client.getOutputStream());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out.write(postArray);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //TODO: might need to alter to handle JSON stuff
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line);
                }
                readmessageReturn = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(readmessageReturn.equals("no new messages")){
                Log.d("MessageList", "No new messages");
            } else {
                try{
                    Log.d("MessageList", "ReturnString: "+readmessageReturn);
                    JSONObject msg = new JSONObject(readmessageReturn);
                    String id = msg.getString("id");
                    String sender = msg.getString("sender");
                    String recipient = msg.getString("recipient");
                    String subject = msg.getString("subject");
                    long created_at = msg.getLong("created_at");
                    String body = msg.getString("body");
                    int ttl = msg.getInt("ttl");

                    Log.d("MessageListOnPostEx", "sender: "+sender+" subject: "+subject+" body: "+body);

                    dbAdapter.open();
                    dbAdapter.InsertMessage(sender, subject, body, ttl);
                    dbAdapter.close();
                }catch (JSONException e){
                    //TODO
                    e.printStackTrace();
                }

                //TODO: Handle JSON stuff here
            }
        }
    }

}
