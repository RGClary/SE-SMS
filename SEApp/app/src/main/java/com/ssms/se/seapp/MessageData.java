package com.ssms.se.seapp;

public class MessageData {

    private String sender;
    private String subject;
    private String body;
    private int ttl;
    private long opened_at = -1;
    private long time_of_death;

    public MessageData(String sender, String subject, String body, int ttl){
        this.sender=sender;
        this.subject=subject;
        this.body=body;
        this.ttl=ttl;

    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public int getTtl() {
        return ttl;
    }

    public long getOpened_at() {
        return opened_at;
    }

    public long getTime_of_death(){
        return time_of_death;
    }

    public void setTtl(int ttl){
        this.ttl=ttl;
    }

    public void setOpened_at(long opened_at) {
        this.opened_at = opened_at;
        time_of_death = opened_at + (this.ttl * 1000);
    }
}