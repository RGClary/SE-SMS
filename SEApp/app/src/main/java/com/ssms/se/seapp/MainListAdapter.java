package com.ssms.se.seapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainListAdapter extends ArrayAdapter<MessageData> implements ListAdapter {

    private List<ListElement> elements;
    private LayoutInflater lf;
    MainListAdapter mainListAdapter;
    private Handler mHandler = new Handler();
    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (elements) {
                long currentTime;
                for (ListElement listElement : elements) {
                    if(listElement.messageData.getOpened_at() != -1) {
                        currentTime = System.currentTimeMillis();
                        listElement.updateTimeRemaining(currentTime);
                        if (listElement.messageData.getTtl() == -1) {
                            DBAdapter dbAdapter = new DBAdapter(getContext());
                            dbAdapter.open();
                            dbAdapter.DeleteMessage(listElement.messageData.getSender(), listElement.messageData.getSubject(), listElement.messageData.getBody());
                            dbAdapter.close();
                            mainListAdapter.remove(listElement.messageData);

                        }
                    }
                }
            }
        }
    };

    public MainListAdapter(List<MessageData> list, Context context) {
        super(context, 0, list);
        lf = LayoutInflater.from(context);
        elements = new ArrayList<>();
        mainListAdapter=this;
        startUpdateTimer();
    }

    private void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ListElement listElement;
        if (convertView == null) {
            listElement = new ListElement();
            convertView = lf.inflate(R.layout.list_item, parent, false);
            listElement.sender = (TextView) convertView.findViewById(R.id.message_list_item_sender);
            listElement.subject = (TextView) convertView.findViewById(R.id.message_list_item_subject);
            listElement.ttl = (TextView) convertView.findViewById(R.id.message_list_item_ttl);
            RelativeLayout item = (RelativeLayout) convertView.findViewById(R.id.message_list_item);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listElement.messageData.getOpened_at() == -1) {
                        listElement.messageData.setOpened_at(System.currentTimeMillis());
                        //listElement.opened_at = System.currentTimeMillis();
                    }
                    Intent intent = new Intent(getContext(), Read.class);
                    intent.putExtra("OPENED_AT", listElement.messageData.getOpened_at());
                    intent.putExtra("SENDER", ""+listElement.messageData.getSender());
                    intent.putExtra("SUBJECT", ""+listElement.messageData.getSubject());
                    intent.putExtra("BODY", ""+listElement.messageData.getBody());
                    intent.putExtra("TTL", listElement.messageData.getTtl());
                    intent.putExtra("TIME_OF_DEATH", listElement.messageData.getTime_of_death());
                    getContext().startActivity(intent);
                }
            });
            convertView.setTag(listElement);
            synchronized (elements){
                elements.add(listElement);
            }
        } else {
            listElement = (ListElement) convertView.getTag();
        }
        try {
            listElement.setMessageData(getItem(position));
        }catch(IndexOutOfBoundsException e){
            //Catches the out of bounds exception that happens when a user is viewing a message that times out
        }
        return convertView;
    }


    private class ListElement {
        TextView sender;
        TextView subject;
        TextView ttl;
        long opened_at = -1;
        MessageData messageData;

        public MessageData setMessageData(MessageData messageData){
            this.messageData=messageData;
            sender.setText(messageData.getSender());
            subject.setText(messageData.getSubject());
            opened_at = messageData.getOpened_at();
            if(messageData.getOpened_at() != -1 || opened_at != -1) {
                updateTimeRemaining(System.currentTimeMillis());
            }
            else{
                int time = messageData.getTtl();
                long timeDiff = time * 1000;
                int seconds = (int) (timeDiff / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                ttl.setText(String.format("%d:%02d:%02d",hours, minutes, seconds));
                messageData.setTtl((int) timeDiff/1000);
            }

            return messageData;
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff = messageData.getTime_of_death() - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000);
                int minutes = seconds / 60;
                int hours = minutes / 60;
                seconds = seconds % 60;
                ttl.setText(String.format("%d:%02d:%02d",hours, minutes, seconds));
                messageData.setTtl((int) timeDiff/1000);
            } else {
                ttl.setText("Expired");
                subject.setText("Expired");
                sender.setText("Expired");
                messageData.setTtl(-1);
            }
        }
    }

}