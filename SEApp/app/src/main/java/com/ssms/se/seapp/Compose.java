package com.ssms.se.seapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Compose extends AppCompatActivity implements View.OnClickListener {

    public final static String EXTRA_MESSAGE = "com.ssms.se.seapp";

    Button send_button;
    Button ttl_button;
    PopupMenu ttl_menu;
    EditText recipient_field;
    EditText subject_field;
    EditText body_field;
    int ttl=0;


    private String reSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);


        recipient_field = (EditText) findViewById(R.id.add_recipient);
        subject_field = (EditText) findViewById(R.id.add_subject);
        body_field = (EditText) findViewById(R.id.compose_field_body);

        send_button = (Button) findViewById(R.id.action_send);
        send_button.setOnClickListener(this);

        ttl_button = (Button) findViewById(R.id.action_setTTL);
        ttl_button.setOnClickListener(this);

        ttl_menu = new PopupMenu(Compose.this,ttl_button);
        ttl_menu.getMenuInflater().inflate(R.menu.menu_ttl, ttl_menu.getMenu());
        ttl_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.value_one:
                        ttl=300;
                        break;
                    case R.id.value_two:
                        ttl=180;
                        break;
                    case R.id.value_three:
                        ttl=60;
                        break;
                    case R.id.value_four:
                        ttl=15;
                        break;
                }
                ttl_button.setText(item.getTitle());
                return true;
            }
        });

        reSender = getIntent().getStringExtra("SENDER");
        if(reSender == null){
            EditText edit_recipient_field = (EditText) findViewById(R.id.add_recipient);
            edit_recipient_field.setOnClickListener(this);
        }else{
            TextView edit = (TextView) findViewById(R.id.add_recipient);
            edit.setText(reSender);
        }


    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){

            case R.id.action_trash:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.action_setTTL:
                ttl_menu.dismiss();
                ttl_menu.show();
                break;

            case R.id.action_send:
                //Singleton.getInstance().doSendMessage(recipient_field.getText().toString(), subject_field.getText().toString(), body_field.getText().toString(), ttl);
                //startActivity(new Intent(this, Main.class));
                SharedPreferences sharedPref = getSharedPreferences(EXTRA_MESSAGE, MODE_PRIVATE);
                String usernameString = sharedPref.getString("USERNAME", null);
                SendMessageTask sendMessageTask = new SendMessageTask(usernameString, recipient_field.getText().toString(), subject_field.getText().toString(), body_field.getText().toString(), ttl);
                sendMessageTask.execute((Void) null);
                finish();
                break;

        }
    }

    public class SendMessageTask extends AsyncTask<Void, Void, Boolean> {
        String mUsername = "";
        String mRecipient = "";
        String mSubject = "";
        String mBody = "";
        int mTtl;
        String returnString = "";
        SendMessageTask(String username, String recepient, String subject, String body, int ttl) {
            mUsername = username;
            mRecipient = recepient;
            mSubject = subject;
            mBody = body;
            mTtl = ttl;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = null;
                try {
                    url = new URL("http://galadriel.cs.utsa.edu/~group6/sendmessage.php");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection client = null;
                try {
                    client = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject messageData = new JSONObject();
                messageData.put("sender", mUsername);
                messageData.put("recipient", mRecipient);
                messageData.put("subject", mSubject);
                messageData.put("body", mBody);
                messageData.put("ttl", mTtl);
                System.out.println(messageData.toString());
                String postArray = "message_data=" + messageData.toString();
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line);
                }
                returnString = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(returnString.equals("true")){
                Toast toast = Toast.makeText(getApplicationContext(), "Message sent.", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(returnString.equals("false")){
                Toast toast = Toast.makeText(getApplicationContext(), "Message not able to send.", Toast.LENGTH_LONG);
                toast.show();
            }
            else if(returnString.equals("recipient not found")) {
                Toast toast = Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}

