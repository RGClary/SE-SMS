package com.ssms.se.seapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class Read extends AppCompatActivity implements View.OnClickListener{

    String expired = "Expired";

    Button reply_button;
    Button trash_button;
    Context context = this;

    int iTtl;
    long opened_at;
    long time_of_death;
    String sSender;
    String sSubject;
    String sBody;

    TextView ttl;
    TextView sender;
    TextView subject;
    TextView body;
    CountDownTimer countDownTimer;
    ArrayList<MessageData> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        iTtl = getIntent().getIntExtra("TTL",0);
        opened_at = getIntent().getLongExtra("OPENED_AT",-1);
        time_of_death = getIntent().getLongExtra("TIME_OF_DEATH",-1);
        sSender = getIntent().getStringExtra("SENDER");
        sSubject = getIntent().getStringExtra("SUBJECT");
        sBody = getIntent().getStringExtra("BODY");


        //Set Sender
        sender = (TextView) findViewById(R.id.message_sender);
        sender.setText("From: "+sSender);
        //Set Subject
        subject = (TextView) findViewById(R.id.message_subject);
        subject.setText("Subject: "+sSubject);
        //Start TTL
        ttl = (TextView) findViewById(R.id.message_ttl);

        countDownTimer = new CountDownTimer(time_of_death-System.currentTimeMillis(),1000){
            @Override
            public void onTick(long l) {
                long timeDiff = time_of_death - System.currentTimeMillis();
                int seconds = (int) (timeDiff / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;

                ttl.setText(String.format("%d:%02d:%02d",hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                sender.setText(expired);
                subject.setText(expired);
                body = (TextView) findViewById(R.id.message_body);
                body.setText(expired);
                ttl.setText("00:00:00");
                DBAdapter dbAdapter = new DBAdapter(context);
                dbAdapter.open();
                dbAdapter.DeleteMessage(sSender,sSubject,sBody);
                dbAdapter.close();
                finish();
                //startActivity(new Intent(context, MessageList.class));
            }
        }.start();

        //Set Body
        body = (TextView) findViewById(R.id.message_body);
        body.setText("Message: \n\n"+sBody);

        reply_button = (Button) findViewById(R.id.action_reply);
        reply_button.setOnClickListener(this);

        trash_button = (Button) findViewById(R.id.action_delete);
        trash_button.setOnClickListener(this);
    }

    @Override
    public void onBackPressed(){
        countDownTimer.cancel();
        this.finish();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.action_delete:
                if(iTtl >=1) {
                    countDownTimer.cancel();
                    ttl.setText("00:00:00");
                    DBAdapter dbAdapter = new DBAdapter(this);
                    dbAdapter.open();
                    dbAdapter.DeleteMessage(sSender,sSubject,sBody);
                    dbAdapter.close();
                    sender.setText(expired);
                    subject.setText(expired);
                    body.setText(expired);
                }
                startActivity(new Intent(this, MessageList.class));
                break;

            case R.id.action_reply:
                Intent intent = new Intent(this, Compose.class);
                intent.putExtra("SENDER",sSender);
                startActivity(intent);
                finish();
                break;

        }
    }
}

